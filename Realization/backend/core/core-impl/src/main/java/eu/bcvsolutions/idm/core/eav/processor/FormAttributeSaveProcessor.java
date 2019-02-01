package eu.bcvsolutions.idm.core.eav.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.event.FormAttributeEvent.FormAttributeEventType;
import eu.bcvsolutions.idm.core.eav.api.event.processor.FormAttributeProcessor;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;

/**
 * Persists form attribute.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
@Component
@Description("Persists form attribute.")
public class FormAttributeSaveProcessor
		extends CoreEventProcessor<IdmFormAttributeDto> 
		implements FormAttributeProcessor {
	
	public static final String PROCESSOR_NAME = "core-form-attribute-save-processor";
	//
	@Autowired private IdmFormAttributeService service;
	
	public FormAttributeSaveProcessor() {
		super(FormAttributeEventType.UPDATE, FormAttributeEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmFormAttributeDto> process(EntityEvent<IdmFormAttributeDto> event) {
		IdmFormAttributeDto entity = event.getContent();
		entity = service.saveInternal(entity);
		event.setContent(entity);
		//
		return new DefaultEventResult<>(event, this);
	}

}
