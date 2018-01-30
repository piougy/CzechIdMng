package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.model.event.AutomaticRoleAttributeEvent.AutomaticRoleAttributeEventType;

/**
 * Create automatic role by attribute
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Create automatic role by attribute.")
public class AutomaticRoleAttributeCreateProcessor extends CoreEventProcessor<IdmAutomaticRoleAttributeDto> {

	public static final String PROCESSOR_NAME = "automatic-role-attribute-create-processor";
	
	@Autowired
	private IdmAutomaticRoleAttributeService service;
	
	public AutomaticRoleAttributeCreateProcessor() {
		super(AutomaticRoleAttributeEventType.CREATE);
	}
	
	@Override
	public EventResult<IdmAutomaticRoleAttributeDto> process(EntityEvent<IdmAutomaticRoleAttributeDto> event) {
		IdmAutomaticRoleAttributeDto automaticRoleAttributeDto = event.getContent();
		//
		automaticRoleAttributeDto = service.saveInternal(automaticRoleAttributeDto);
		event.setContent(automaticRoleAttributeDto);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}
}
