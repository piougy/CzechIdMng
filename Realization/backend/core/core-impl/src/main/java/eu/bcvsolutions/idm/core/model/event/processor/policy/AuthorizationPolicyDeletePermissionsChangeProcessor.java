package eu.bcvsolutions.idm.core.model.event.processor.policy;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
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
	@Autowired private IdmIdentityService identityService;
	@Autowired private TokenManager tokenManager;

	public AuthorizationPolicyDeletePermissionsChangeProcessor() {
		super(AuthorizationPolicyEventType.UPDATE, AuthorizationPolicyEventType.DELETE);
	}

	@Override
	public int getOrder() {
		// after delete
		return 5000;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmAuthorizationPolicyDto> process(EntityEvent<IdmAuthorizationPolicyDto> event) {
		IdmAuthorizationPolicyDto entity = event.getContent();
		//
		if (event.getOriginalSource() == null 
				|| event.hasType(AuthorizationPolicyEventType.DELETE)
				|| !Objects.equals(entity.getGroupPermission(), event.getOriginalSource().getGroupPermission())
				|| !entity.getPermissions().containsAll(event.getOriginalSource().getPermissions())) {
			//
			// Disable tokens for identities with changed role assigned.
			identityService.findAllByRole(entity.getRole()).forEach(identity -> {
				tokenManager.disableTokens(identity);
			});
		}
		//
		return new DefaultEventResult<>(event, this);
	}
}
