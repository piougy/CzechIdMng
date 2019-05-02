package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleRequestProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;

/**
 * Realization of request in ACC module - ensure account management and
 * provisioning.
 * 
 * @author Vít Švanda
 *
 */
@Component("accRoleRequestRealizationProcessor")
@Description("Realization of request in ACC module - ensure account management and provisioning.")
public class RoleRequestRealizationProcessor extends CoreEventProcessor<IdmRoleRequestDto>
		implements RoleRequestProcessor {

	public static final String PROCESSOR_NAME = "acc-role-request-realization-processor";
	private static final Logger LOG = LoggerFactory.getLogger(RoleRequestRealizationProcessor.class);

	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private AccAccountManagementService accountManagementService;
	@Autowired
	private ProvisioningService provisioningService;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private AccIdentityAccountService identityAccountService;

	@Autowired
	public RoleRequestRealizationProcessor(IdmRoleRequestService service) {
		super(RoleRequestEventType.NOTIFY);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleRequestDto> process(EntityEvent<IdmRoleRequestDto> event) {
		IdmRoleRequestDto request = event.getContent();
		IdmIdentityDto identity = identityService.get(request.getApplicant());

		Set<IdmIdentityRoleDto> addedIdentityRoles = this
				.getSetProperty(IdentityRoleEvent.PROPERTY_ASSIGNED_NEW_ROLES, event, IdmIdentityRoleDto.class);
		Set<IdmIdentityRoleDto> updatedIdentityRoles = this.getSetProperty(
				IdentityRoleEvent.PROPERTY_ASSIGNED_UPDATED_ROLES, event, IdmIdentityRoleDto.class);
		Set<UUID> removedIdentityAccounts = this.getSetProperty(IdmAccountDto.IDENTITY_ACCOUNT_FOR_DELAYED_ACM,
				event, UUID.class);

		Set<UUID> accountsForProvisioning = Sets.newHashSet();

		if (addedIdentityRoles.size() > 0) {
			LOG.debug("Call account management for identity [{}] and new identity-roles [{}]",
					identity.getUsername(), addedIdentityRoles);
			List<UUID> accounts = accountManagementService.resolveNewIdentityRoles(identity,
					addedIdentityRoles.toArray(new IdmIdentityRoleDto[0]));
			addAccounts(accountsForProvisioning, accounts);
		}

		if (updatedIdentityRoles.size() > 0) {
			LOG.debug("Call account management for identity [{}] and updated identity-roles [{}]",
					identity.getUsername(), updatedIdentityRoles);
			List<UUID> accounts = accountManagementService.resolveUpdatedIdentityRoles(identity,
					updatedIdentityRoles.toArray(new IdmIdentityRoleDto[0]));
			addAccounts(accountsForProvisioning, accounts);
		}

		// Remove delayed identity-accounts (includes provisioning)
		if (removedIdentityAccounts.size() > 0) {
			LOG.debug("Call account management for identity [{}] - remove identity-accounts [{}]",
					identity.getUsername(), removedIdentityAccounts);
			removedIdentityAccounts.stream().distinct().forEach(identityAccountId -> {
				AccIdentityAccountDto identityAccountDto = identityAccountService.get(identityAccountId);
				if (identityAccountDto != null) {
					identityAccountService.delete(identityAccountDto);
					accountsForProvisioning.add(identityAccountDto.getAccount());
				}
			});
		}

		// Provisioning for modified account
		accountsForProvisioning.forEach(accountId -> {
			AccAccountDto account = accountService.get(accountId);
			if (account != null) { // Account could be null (was deleted).
				LOG.debug("Call provisioning for identity [{}] and account [{}]", identity.getUsername(),
						account.getUid());
				provisioningService.doProvisioning(account, identity);
			}
		});
		
		return new DefaultEventResult<>(event, this);
	}


	private void addAccounts(Set<UUID> accountsForProvisioning, List<UUID> accounts) {
		if (accounts != null) {
			accounts.forEach(accountId -> {
				accountsForProvisioning.add(accountId);
			});
		}
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
}
