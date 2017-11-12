package eu.bcvsolutions.idm.acc.event.processor;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccContractAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccRoleAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccRoleCatalogueAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccTreeAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccContractAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccRoleAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccRoleCatalogueAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccTreeAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.event.IdentityAccountEvent.IdentityAccountEventType;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccContractAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccRoleAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccRoleCatalogueAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccTreeAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Deletes identity account
 * 
 * @author Svanda
 */
@Component("accAccountDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class AccountDeleteProcessor extends CoreEventProcessor<AccAccountDto> implements AccountProcessor {

	private static final String PROCESSOR_NAME = "account-delete-processor";
	private final AccAccountService accountService;
	private final AccIdentityAccountService identityAccountService;
	private final AccRoleAccountService roleAccountService;
	private final AccTreeAccountService treeAccountService;
	private final AccContractAccountService contractAccountService;
	private final AccRoleCatalogueAccountService roleCatalogueAccountService;
	private final ProvisioningService provisioningService;

	private static final Logger LOG = LoggerFactory.getLogger(AccountDeleteProcessor.class);

	@Autowired
	public AccountDeleteProcessor(AccAccountService accountService, EntityEventManager entityEventManager,
			AccRoleAccountService roleAccountService, AccTreeAccountService treeAccountService,
			AccContractAccountService contractAccountService,
			AccRoleCatalogueAccountService roleCatalogueAccountService,
			AccIdentityAccountService identityAccountService, ProvisioningService provisioningService) {
		super(IdentityAccountEventType.DELETE);
		//
		Assert.notNull(accountService);
		Assert.notNull(entityEventManager);
		Assert.notNull(roleAccountService);
		Assert.notNull(roleCatalogueAccountService);
		Assert.notNull(treeAccountService);
		Assert.notNull(contractAccountService);
		Assert.notNull(identityAccountService);
		Assert.notNull(provisioningService);
		//
		this.accountService = accountService;
		this.roleAccountService = roleAccountService;
		this.roleCatalogueAccountService = roleCatalogueAccountService;
		this.treeAccountService = treeAccountService;
		this.contractAccountService = contractAccountService;
		this.identityAccountService = identityAccountService;
		this.provisioningService = provisioningService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccAccountDto> process(EntityEvent<AccAccountDto> event) {
		AccAccountDto account = event.getContent();
		UUID entityId = null;
		Object entityIdObj = event.getProperties().get(AccAccountService.ENTITY_ID_PROPERTY);
		if (entityIdObj instanceof UUID) {
			entityId = (UUID) entityIdObj;
		}
		boolean deleteTargetAccount = false;
		Object deleteTargetAccountObj = event.getProperties().get(AccAccountService.DELETE_TARGET_ACCOUNT_PROPERTY);
		if (deleteTargetAccountObj instanceof Boolean) {
			deleteTargetAccount = (boolean) deleteTargetAccountObj;
		}

		Assert.notNull(account, "Account cannot be null!");
		// We do not allow delete account in protection
		if (account.isAccountProtectedAndValid()) {
			throw new ResultCodeException(AccResultCode.ACCOUNT_CANNOT_BE_DELETED_IS_PROTECTED,
					ImmutableMap.of("uid", account.getUid()));
		}

		// delete all identity accounts
		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setAccountId(account.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(identityAccountFilter, null)
				.getContent();
		identityAccounts.forEach(identityAccount -> {
			identityAccountService.delete(identityAccount);
		});

		// delete all role accounts
		AccRoleAccountFilter roleAccountFilter = new AccRoleAccountFilter();
		roleAccountFilter.setAccountId(account.getId());
		List<AccRoleAccountDto> roleAccounts = roleAccountService.find(roleAccountFilter, null).getContent();
		roleAccounts.forEach(roleAccount -> {
			roleAccountService.delete(roleAccount);
		});

		// delete all roleCatalogue accounts
		AccRoleCatalogueAccountFilter roleCatalogueAccountFilter = new AccRoleCatalogueAccountFilter();
		roleCatalogueAccountFilter.setAccountId(account.getId());
		List<AccRoleCatalogueAccountDto> roleCatalogueAccounts = roleCatalogueAccountService
				.find(roleCatalogueAccountFilter, null).getContent();
		roleCatalogueAccounts.forEach(roleCatalogueAccount -> {
			roleCatalogueAccountService.delete(roleCatalogueAccount);
		});

		// delete all tree accounts
		AccTreeAccountFilter treeAccountFilter = new AccTreeAccountFilter();
		treeAccountFilter.setAccountId(account.getId());
		List<AccTreeAccountDto> treeAccounts = treeAccountService.find(treeAccountFilter, null).getContent();
		treeAccounts.forEach(treeAccount -> {
			treeAccountService.delete(treeAccount);
		});

		// delete all contract accounts
		AccContractAccountFilter contractAccountFilter = new AccContractAccountFilter();
		contractAccountFilter.setAccountId(account.getId());
		List<AccContractAccountDto> contractAccounts = contractAccountService.find(contractAccountFilter, null)
				.getContent();
		contractAccounts.forEach(contractAccount -> {
			contractAccountService.delete(contractAccount);
		});

		//
		accountService.deleteInternal(account);
		if (deleteTargetAccount) {
			SysSystemEntityDto systemEntity = DtoUtils.getEmbedded(account, AccAccount_.systemEntity, SysSystemEntityDto.class);
			if (SystemEntityType.CONTRACT == systemEntity.getEntityType()) {
				LOG.warn(MessageFormat.format("Provisioning is not supported for contract now [{0}]!",
						account.getUid()));
				return new DefaultEventResult<>(event, this);
			}
			this.provisioningService.doDeleteProvisioning(account, systemEntity.getEntityType(), entityId);
		}

		return new DefaultEventResult<>(event, this);
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
