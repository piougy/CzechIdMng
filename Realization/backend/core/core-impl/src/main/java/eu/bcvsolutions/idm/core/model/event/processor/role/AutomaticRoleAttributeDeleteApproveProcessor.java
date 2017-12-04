package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.model.event.AutomaticRoleAttributeEvent.AutomaticRoleAttributeEventType;
import eu.bcvsolutions.idm.core.model.event.processor.AbstractApprovableEventProcessor;

/**
 * Approve delete automatic roles by attribute
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Approve detele automatic role by attribute.")
public class AutomaticRoleAttributeDeleteApproveProcessor extends AbstractApprovableEventProcessor<IdmAutomaticRoleAttributeDto> {

	public static final String PROCESSOR_NAME = "automatic-role-attribute-delete-approve-processor";

	public AutomaticRoleAttributeDeleteApproveProcessor() {
		super(AutomaticRoleAttributeEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	/**
	 * Before standard save
	 * 
	 * @return
	 */
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER - 1000;
	}
}
