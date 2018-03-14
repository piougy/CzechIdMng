package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.model.event.processor.AbstractPublishEntityChangeProcessor;

/**
 * Publish identity role change event
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Publish identity role change event.")
public class IdentityRolePublishChangeProcessor 
		extends AbstractPublishEntityChangeProcessor<IdmIdentityRoleDto> {

	public static final String PROCESSOR_NAME = "identity-role-publish-change-processor";
	
	public IdentityRolePublishChangeProcessor() {
		super(IdentityRoleEventType.CREATE, IdentityRoleEventType.UPDATE, CoreEventType.EAV_SAVE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}
