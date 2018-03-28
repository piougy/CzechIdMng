package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractPublishEntityChangeProcessor;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;

/**
 * Publish role change event
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Publish role change event.")
public class RolePublishChangeProcessor 
		extends AbstractPublishEntityChangeProcessor<IdmRoleDto> {

	public static final String PROCESSOR_NAME = "role-publish-change-processor";
	
	public RolePublishChangeProcessor() {
		super(RoleEventType.CREATE, RoleEventType.UPDATE, RoleEventType.EAV_SAVE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}
