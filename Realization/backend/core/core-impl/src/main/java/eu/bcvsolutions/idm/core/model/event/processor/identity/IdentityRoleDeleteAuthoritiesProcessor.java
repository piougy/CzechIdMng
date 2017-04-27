package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorityChange;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorityChangeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.security.service.GrantedAuthoritiesFactory;

/**
 * Event processor to check if authorities were removed from identity.
 * 
 * @author Jan Helbich
 *
 */
@Component
@Description("Checks modifications in identity authorities after role removal.")
public class IdentityRoleDeleteAuthoritiesProcessor extends CoreEventProcessor<IdmIdentityRole> {

	public static final String PROCESSOR_NAME = "identity-role-delete-authorities-processor";

	private final IdmAuthorityChangeRepository repository;
	private final IdmIdentityRoleService identityRoleService;
	private final GrantedAuthoritiesFactory authoritiesFactory;

	
	@Autowired
	public IdentityRoleDeleteAuthoritiesProcessor(
			IdmAuthorityChangeRepository repository,
			IdmIdentityRoleService identityRoleService,
			GrantedAuthoritiesFactory authoritiesFactory) {
		super(IdentityRoleEventType.DELETE);
		//
		Assert.notNull(repository);
		Assert.notNull(identityRoleService);
		Assert.notNull(authoritiesFactory);
		//
		this.repository = repository;
		this.authoritiesFactory = authoritiesFactory;
		this.identityRoleService = identityRoleService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public int getOrder() {
		return Integer.MAX_VALUE;
	}

	@Override
	public EventResult<IdmIdentityRole> process(EntityEvent<IdmIdentityRole> event) {
		checkRevokedPermissions(event.getContent());
		return new DefaultEventResult<>(event, this);
	}

	private void checkRevokedPermissions(IdmIdentityRole identityRole) {
		List<IdmIdentityRole> roles = getIdentityRoles(identityRole);
		roles.remove(identityRole);

		// represents the final authorities set after role removal
		Collection<GrantedAuthority> withoutDeleted = authoritiesFactory
				.getGrantedAuthoritiesForValidRoles(roles);
		Collection<GrantedAuthority> deletedAuthorities = authoritiesFactory
				.getGrantedAuthoritiesForValidRoles(Collections.singletonList(identityRole));
		
		if (!authoritiesFactory.containsAllAuthorities(withoutDeleted, deletedAuthorities)) {
			// authorities were changed, update identity flag
			IdmIdentity identity = identityRole.getIdentityContract().getIdentity();
			IdmAuthorityChange ac = repository.findByIdentity(identity);
			if (ac == null) {
				ac = new IdmAuthorityChange();
				ac.setIdentity(identity);
			}
			ac.authoritiesChanged();
			repository.save(ac);
		}
	}

	private List<IdmIdentityRole> getIdentityRoles(IdmIdentityRole identityRole) {
		return identityRoleService.getRoles(identityRole.getIdentityContract().getIdentity());
	}
	
}