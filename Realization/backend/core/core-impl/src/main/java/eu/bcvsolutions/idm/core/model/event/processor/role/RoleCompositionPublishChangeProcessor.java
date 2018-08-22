package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractPublishEntityChangeProcessor;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;

/**
 * Publish role composition change event
 * 
 * @author Radek Tomiška
 *
 */
@Component(RoleCompositionPublishChangeProcessor.PROCESSOR_NAME)
@Description("Publish role composition change event.")
public class RoleCompositionPublishChangeProcessor 
		extends AbstractPublishEntityChangeProcessor<IdmRoleCompositionDto> {

	public static final String PROCESSOR_NAME = "core-role-composition-publish-change-processor";
	
	public RoleCompositionPublishChangeProcessor() {
		super(RoleEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}
