package eu.bcvsolutions.idm.acc.event.processor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.event.AccountEvent;
import eu.bcvsolutions.idm.acc.event.AccountEvent.AccountEventType;
import eu.bcvsolutions.idm.acc.event.IdentityAccountEvent.IdentityAccountEventType;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;

/**
 * Deletes identity account
 * 
 * @author Svanda
 */
@Component("accIdentityAccountDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class IdentityAccountDeleteProcessor extends CoreEventProcessor<AccIdentityAccountDto> {

	private static final String PROCESSOR_NAME = "identity-account-delete-processor";
	private final AccIdentityAccountService service;
	private final AccAccountService accountService;
	private final SysSystemMappingService systemMappingService;
	private final EntityEventManager entityEventManager;
	private final IdmIdentityService identityService;

	@Autowired
	public IdentityAccountDeleteProcessor(AccIdentityAccountService service, AccAccountService accountService,
			SysSystemMappingService systemMappingService, EntityEventManager entityEventManager,
			IdmIdentityService identityService) {
		super(IdentityAccountEventType.DELETE);
		//
		Assert.notNull(service);
		Assert.notNull(accountService);
		Assert.notNull(systemMappingService);
		Assert.notNull(entityEventManager);
		Assert.notNull(identityService);
		//
		this.service = service;
		this.accountService = accountService;
		this.systemMappingService = systemMappingService;
		this.entityEventManager = entityEventManager;
		this.identityService = identityService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccIdentityAccountDto> process(EntityEvent<AccIdentityAccountDto> event) {
		AccIdentityAccountDto entity = event.getContent();
		UUID account = entity.getAccount();
		AccAccountDto accountDto = accountService.get(account);
		Assert.notNull(accountDto, "Account cannot be null!");

		// We check if exists another (ownership) identity-accounts, if not
		// then we will delete account
		List<AccIdentityAccountDto> identityAccounts = findIdentityAccounts(account);
		boolean moreIdentityAccounts = identityAccounts.stream().filter(identityAccount -> {
			return identityAccount.isOwnership() && !identityAccount.equals(entity);
		}).findAny().isPresent();

		boolean deleteTargetAccount = (boolean) event.getProperties()
				.get(AccIdentityAccountService.DELETE_TARGET_ACCOUNT_KEY);
		boolean deleteAccAccount = true;

		// If is account in protection, then we will not delete
		// identity-account
		// But is here exception from this. When is presented
		// attribute FORCE_DELETE_OF_IDENTITY_ACCOUNT_KEY, then
		// we will do delete of identity-account (it is important
		// for integrity ... for example during delete of whole
		// identity).
		boolean forceDeleteIdentityAccount = isForceDeleteAttributePresent(event.getProperties());

		if (!moreIdentityAccounts && entity.isOwnership()) {
			if (accountDto.isAccountProtectedAndValid()) {
				if (forceDeleteIdentityAccount) {
					// Target account and AccAccount will NOT be deleted!
					deleteAccAccount = false;
				} else {
					throw new ResultCodeException(AccResultCode.ACCOUNT_CANNOT_BE_DELETED_IS_PROTECTED,
							ImmutableMap.of("uid", accountDto.getUid()));
				}
				// Is account protection activated on system mapping?
				// Set account as protected we can only on account without protection (event has already invalid protection)!
			} else if (!accountDto.isInProtection() && systemMappingService.isEnabledProtection(accountDto)) {
				// This identity account is last ... protection will be
				// activated
				activateProtection(accountDto);
				accountDto = accountService.save(accountDto);
				entity.setRoleSystem(null);
				entity.setIdentityRole(null);
				service.save(entity);
				doProvisioningSkipAccountProtection(accountDto, entity.getEntity());

				// If is account in protection, then we will not delete
				// identity-account
				if (forceDeleteIdentityAccount) {
					// Target account and AccAccount will NOT be deleted!
					deleteAccAccount = false;
				} else {
					return new DefaultEventResult<>(event, this);
				}
			}
		}

		service.deleteInternal(entity);
		// Finally we can delete AccAccount
		if (!moreIdentityAccounts && entity.isOwnership() && deleteAccAccount) {
			// We delete all NOT ownership identity accounts first
			identityAccounts.stream()
					.filter(identityAccount -> !identityAccount.isOwnership() && !identityAccount.equals(entity))
					.forEach(identityAccount -> {
						service.delete(identityAccount);
					});
			accountService.publish(new AccountEvent(AccountEventType.DELETE, accountDto,
					ImmutableMap.of(AccAccountService.DELETE_TARGET_ACCOUNT_PROPERTY, deleteTargetAccount,
							AccAccountService.ENTITY_ID_PROPERTY, entity.getEntity())));

		}

		return new DefaultEventResult<>(event, this);
	}

	/**
	 * We need do provisioning (for example move account to archive)
	 * 
	 * @param account
	 * @param entity
	 */
	private void doProvisioningSkipAccountProtection(AccAccountDto account, UUID entity) {
		entityEventManager.process(new ProvisioningEvent(ProvisioningEventType.START, account,
				ImmutableMap.of(ProvisioningService.DTO_PROPERTY_NAME, identityService.get(entity),
						ProvisioningService.CANCEL_PROVISIONING_BREAK_IN_PROTECTION, Boolean.TRUE)));

	}

	private List<AccIdentityAccountDto> findIdentityAccounts(UUID account) {
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setAccountId(account);
		filter.setOwnership(Boolean.TRUE);

		return service.find(filter, null).getContent();
	}

	private void activateProtection(AccAccountDto accountEntity) {
		Integer daysInterval = systemMappingService.getProtectionInterval(accountEntity);
		accountEntity.setInProtection(true);
		if (daysInterval == null) {
			// Interval is null, protection is infinite
			accountEntity.setEndOfProtection(null);
		} else {
			accountEntity.setEndOfProtection(DateTime.now().plusDays(daysInterval));
		}
	}

	private boolean isForceDeleteAttributePresent(Map<String, Serializable> properties) {
		Object value = properties.get(AccIdentityAccountService.FORCE_DELETE_OF_IDENTITY_ACCOUNT_KEY);
		if (value instanceof Boolean && (Boolean) value) {
			return true;
		}
		return false;
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}

	@Override
	public boolean isDisableable() {
		return false;
	}

}
