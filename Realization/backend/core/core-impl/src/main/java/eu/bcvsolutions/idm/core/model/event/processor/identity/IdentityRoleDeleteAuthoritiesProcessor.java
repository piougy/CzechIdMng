package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.security.api.service.GrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.core.security.service.impl.IdmAuthorityHierarchy;

/**
 * Event processor to check if authorities were removed from identity.
 * 
 * TODO: asynchronous or execute without granted authorities is changed
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 */
@Component
@Description("Checks modifications in identity authorities after role removal and disable authentication tokens.")
public class IdentityRoleDeleteAuthoritiesProcessor extends CoreEventProcessor<IdmIdentityRoleDto> {

	public static final String PROCESSOR_NAME = "identity-role-delete-authorities-processor";

	@Autowired private TokenManager tokenManager;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private GrantedAuthoritiesFactory authoritiesFactory;
	@Autowired private IdmAuthorityHierarchy authorityHierarchy;
	
	public IdentityRoleDeleteAuthoritiesProcessor() {
		super(IdentityRoleEventType.DELETE);
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
	public boolean conditional(EntityEvent<IdmIdentityRoleDto> event) {
		// check authorities may be skipped
		return super.conditional(event)
				&& !getBooleanProperty(IdmIdentityRoleService.SKIP_CHECK_AUTHORITIES, event.getProperties());
	}

	@Override
	public EventResult<IdmIdentityRoleDto> process(EntityEvent<IdmIdentityRoleDto> event) {
		IdmIdentityRoleDto identityRole = event.getContent();
		//
		IdmIdentityContractDto contract = DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.identityContract);
		UUID identityId = contract.getIdentity(); 
		
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identityId);
		roles.remove(identityRole);

		// represents the final authorities set after role removal
		Collection<? extends GrantedAuthority> withoutDeleted = authorityHierarchy.getReachableGrantedAuthorities(
				authoritiesFactory.getGrantedAuthoritiesForValidRoles(identityId, roles));
		Collection<? extends GrantedAuthority> deletedAuthorities = authorityHierarchy.getReachableGrantedAuthorities(
				authoritiesFactory.getGrantedAuthoritiesForValidRoles(identityId,
						Collections.singletonList(identityRole)));

		if (!authoritiesFactory.containsAllAuthorities(withoutDeleted, deletedAuthorities)) {
			// authorities were changed, disable active identity tokens
			tokenManager.disableTokens(new IdmIdentityDto(identityId));
		}
		//
		return new DefaultEventResult<>(event, this);
	}

}