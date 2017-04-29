package eu.bcvsolutions.idm.core.model.event.processor.policy;

import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.AuthorizationPolicyEvent.AuthorizationPolicyEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;

/**
 * Handles authentication policy when authorization policy
 * is deleted and permissions removed from role.
 * 
 * @author Jan Helbich
 */
@Component
@Description("Handles authentication policy when authorization policy is deleted and permissions removed from role.")
public class AuthorizationPolicyDeletePermissionsChangeProcessor extends CoreEventProcessor<IdmAuthorizationPolicy> {

	private static final String PROCESSOR_NAME = "authorization-policy-delete-permissions-change-processor";

	private final IdmAuthorizationPolicyRepository repository;
	private final IdmAuthorizationPolicyService service;
	private final IdmIdentityService identityService;

	@Autowired
	public AuthorizationPolicyDeletePermissionsChangeProcessor(
			IdmAuthorizationPolicyRepository repository,
			IdmAuthorizationPolicyService service,
			IdmIdentityService identityService) {
		super(AuthorizationPolicyEventType.values());
		//
		Assert.notNull(repository);
		Assert.notNull(service);
		Assert.notNull(identityService);
		//
		this.repository = repository;
		this.service = service;
		this.identityService = identityService;
	}

	@Override
	public int getOrder() {
		// runs last
		return Integer.MAX_VALUE;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmAuthorizationPolicy> process(EntityEvent<IdmAuthorizationPolicy> event) {
		IdmAuthorizationPolicy entity = event.getContent();
		Set<GrantedAuthority> currentRolePermissions = service.getEnabledRoleAuthorities(entity.getRole().getId());
		Set<GrantedAuthority> persistedRolePermissions = service.getEnabledPersistedRoleAuthorities(entity.getRole().getId());
		//
		if (!currentRolePermissions.equals(persistedRolePermissions)) {
			updateIdentitiesAuthChangeInRole(entity.getRole());
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Update authority change timestamp on identities in current role.
	 * 
	 * @param role
	 */
	private void updateIdentitiesAuthChangeInRole(IdmRole role) {
		List<IdmIdentity> usersInRole = identityService.findAllByRole(role);
		identityService.updateAuthorityChange(usersInRole, DateTime.now());
	}


}
