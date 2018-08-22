package eu.bcvsolutions.idm.core.model.event.processor.policy;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.event.AuthorizationPolicyEvent.AuthorizationPolicyEventType;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;

/**
 * Handles authentication policy when authorization policy
 * is deleted and permissions removed from role.
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 */
@Component(AuthorizationPolicyDeletePermissionsChangeProcessor.PROCESSOR_NAME)
@Description("Handles authentication policy when authorization policy is deleted and permissions removed from role.")
public class AuthorizationPolicyDeletePermissionsChangeProcessor extends CoreEventProcessor<IdmAuthorizationPolicyDto> {

	public static final String PROCESSOR_NAME = "authorization-policy-delete-permissions-change-processor";
	//
	@Autowired private IdmAuthorizationPolicyService service;
	@Autowired private IdmIdentityService identityService;
	@Autowired private TokenManager tokenManager;

	public AuthorizationPolicyDeletePermissionsChangeProcessor() {
		super(AuthorizationPolicyEventType.UPDATE, AuthorizationPolicyEventType.DELETE);
	}

	@Override
	public int getOrder() {
		// runs last .. ugh
		return Integer.MAX_VALUE;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmAuthorizationPolicyDto> process(EntityEvent<IdmAuthorizationPolicyDto> event) {
		IdmAuthorizationPolicyDto entity = event.getContent();
		Set<GrantedAuthority> currentRolePermissions = service.getEnabledRoleAuthorities(null, entity.getRole());
		Set<GrantedAuthority> persistedRolePermissions = service.getEnabledPersistedRoleAuthorities(null, entity.getRole());
		//
		if (!currentRolePermissions.equals(persistedRolePermissions)) {
			updateIdentitiesAuthChangeInRole(entity.getRole());
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Disable tokens for identities with changed role assigned.
	 * 
	 * @param role
	 */
	private void updateIdentitiesAuthChangeInRole(UUID role) {
		identityService.findAllByRole(role).forEach(identity -> {
			tokenManager.disableTokens(identity);
		});
	}


}
