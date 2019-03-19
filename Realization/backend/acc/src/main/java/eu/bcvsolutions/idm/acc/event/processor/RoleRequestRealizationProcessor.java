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
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
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
public class RoleRequestRealizationProcessor extends CoreEventProcessor<IdmRoleRequestDto> {

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
	public RoleRequestRealizationProcessor(IdmRoleRequestService service) {
		super(RoleRequestEventType.EXCECUTE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleRequestDto> process(EntityEvent<IdmRoleRequestDto> event) {
		IdmRoleRequestDto request = event.getContent();
		IdmIdentityDto identity = identityService.get(request.getApplicant());

		List<IdmIdentityRoleDto> addedIdentityRoles = this.getListProperty(IdentityRoleEvent.PROPERTY_ASSIGNED_NEW_ROLES, event,
				IdmIdentityRoleDto.class);
		List<IdmIdentityRoleDto> updatedIdentityRoles = this.getListProperty(IdentityRoleEvent.PROPERTY_ASSIGNED_UPDATED_ROLES,
				event, IdmIdentityRoleDto.class);
		List<UUID> removedIdentityRoles = this.getListProperty(IdentityRoleEvent.PROPERTY_ASSIGNED_REMOVED_ROLES, event, UUID.class);

		Set<UUID> accountsForProvisioning = Sets.newHashSet();
		
		if (addedIdentityRoles.size() > 0) {
			LOG.debug("Call account management for identity [{}] and new identity-roles [{}]", identity.getUsername(), addedIdentityRoles);
			List<UUID> accounts = accountManagementService.resolveNewIdentityRoles(identity, addedIdentityRoles.toArray(new IdmIdentityRoleDto[0]));
			addAccounts(accountsForProvisioning, accounts);
		}
		
		if (updatedIdentityRoles.size() > 0) {
			LOG.debug("Call account management for identity [{}] and updated identity-roles [{}]", identity.getUsername(), updatedIdentityRoles);
			List<UUID> accounts = accountManagementService.resolveUpdatedIdentityRoles(identity, updatedIdentityRoles.toArray(new IdmIdentityRoleDto[0]));
			addAccounts(accountsForProvisioning, accounts);
		}
		
		if (addedIdentityRoles.size() > 0 || updatedIdentityRoles.size() > 0 || removedIdentityRoles.size() > 0) {
			List<UUID> accounts = this.getListProperty(AccAccountManagementService.ACCOUNT_IDS_FOR_DELETED_IDENTITY_ROLE, event, UUID.class);
			addAccounts(accountsForProvisioning, accounts);
			
//			if (accountsForProvisioning == null) {
//				// We don't know about specific accounts, so we will execute provisioning for all accounts.
//				LOG.debug("Call provisioning for identity [{}]", identity.getUsername());
//				provisioningService.doProvisioning(identity);
//				
//				return new DefaultEventResult<>(event, this);
//			}
			
			accountsForProvisioning.forEach(accountId -> {
				AccAccountDto account = accountService.get(accountId);
				if (account != null) { // Account could be null (was deleted).
					LOG.debug("Call provisioning for identity [{}] and account [{}]", identity.getUsername(), account.getUid());
					provisioningService.doProvisioning(account, identity);
				}
			});
		}

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
