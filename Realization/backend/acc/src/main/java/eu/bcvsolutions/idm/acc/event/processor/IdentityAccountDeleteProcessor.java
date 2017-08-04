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

import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
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
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

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
	private final IdmIdentityRepository identityRepository;

	@Autowired
	public IdentityAccountDeleteProcessor(AccIdentityAccountService service, AccAccountService accountService,
			SysSystemMappingService systemMappingService, EntityEventManager entityEventManager,
			IdmIdentityRepository identityRepository) {
		super(IdentityAccountEventType.DELETE);
		//
		Assert.notNull(service);
		Assert.notNull(accountService);
		Assert.notNull(systemMappingService);
		Assert.notNull(entityEventManager);
		Assert.notNull(identityRepository);
		//
		this.service = service;
		this.accountService = accountService;
		this.systemMappingService = systemMappingService;
		this.entityEventManager = entityEventManager;
		this.identityRepository = identityRepository;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccIdentityAccountDto> process(EntityEvent<AccIdentityAccountDto> event) {
		AccIdentityAccountDto entity = event.getContent();
		UUID account = entity.getAccount();
		AccAccount accountEntity = accountService.get(account);
		Assert.notNull(account, "Account cannot be null!");

		// We check if exists another (ownership) identity-accounts, if not
		// then we will delete account
		List<AccIdentityAccountDto> identityAccounts = findIdentityAccounts(account);
		boolean moreIdentityAccounts = identityAccounts.stream().filter(identityAccount -> {
			return identityAccount.isOwnership() && !identityAccount.equals(entity);
		}).findAny().isPresent();

		boolean deleteTargetAccount = (boolean) event.getProperties()
				.get(AccIdentityAccountService.DELETE_TARGET_ACCOUNT_KEY);

		// Is account protection activated on system mapping?
		if (systemMappingService.isEnabledProtection(accountEntity)) {
			if (!moreIdentityAccounts && entity.isOwnership()) {
				// This identity account is last ... protection will be
				// activated
				activateProtection(accountEntity);
				accountService.save(accountEntity);
				entity.setRoleSystem(null);
				entity.setIdentityRole(null);
				service.save(entity);
				doProvisioningSkipAccountProtection(accountEntity, entity.getEntity());

				// If is account in protection, then we will not delete
				// identity-account
				if (isForceDeleteAttributePresent(event.getProperties())) {
					// But is here exception from this. When is presented
					// attribute FORCE_DELETE_OF_IDENTITY_ACCOUNT_KEY, then
					// we will do delete of identity-account (it is important
					// for integrity ... for example during delete of whole
					// identity).

					// Target AccAccount will be not deleted!
					deleteTargetAccount = false;
				} else {
					return new DefaultEventResult<>(event, this);
				}
			}
		}

		service.deleteInternal(entity);
		if (!moreIdentityAccounts && entity.isOwnership()) {
			// We delete all identity accounts first
			identityAccounts.stream()
					.filter(identityAccount -> identityAccount.isOwnership() && !identityAccount.equals(entity))
					.forEach(identityAccount -> {
						service.delete(identityAccount);
					});
			// Finally we can delete account
			accountService.delete(accountService.get(account), deleteTargetAccount);
		}

		return new DefaultEventResult<>(event, this);
	}

	/**
	 * We need do provisioning (for example move account to archive)
	 * 
	 * @param account
	 * @param entity
	 */
	private void doProvisioningSkipAccountProtection(AccAccount account, UUID entity) {
		entityEventManager.process(new ProvisioningEvent(ProvisioningEventType.START, account,
				ImmutableMap.of(ProvisioningService.ENTITY_PROPERTY_NAME, identityRepository.findOne(entity),
						ProvisioningService.CANCEL_PROVISIONING_BREAK_IN_PROTECTION, Boolean.TRUE)));

	}

	private List<AccIdentityAccountDto> findIdentityAccounts(UUID account) {
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setAccountId(account);
		filter.setOwnership(Boolean.TRUE);

		return service.find(filter, null).getContent();
	}

	private void activateProtection(AccAccount accountEntity) {
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
