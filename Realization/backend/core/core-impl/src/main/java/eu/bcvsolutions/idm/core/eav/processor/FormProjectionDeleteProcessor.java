package eu.bcvsolutions.idm.core.eav.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.event.FormProjectionEvent.FormProjectionEventType;
import eu.bcvsolutions.idm.core.eav.api.event.processor.FormProjectionProcessor;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormProjectionService;

/**
 * Deletes form projection - ensures referential integrity.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
@Component(FormProjectionDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes form projection from repository.")
public class FormProjectionDeleteProcessor
		extends CoreEventProcessor<IdmFormProjectionDto> 
		implements FormProjectionProcessor {
	
	public static final String PROCESSOR_NAME = "core-form-projection-delete-processor";
	@Autowired private IdmFormProjectionService service;
	
	public FormProjectionDeleteProcessor() {
		super(FormProjectionEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmFormProjectionDto> process(EntityEvent<IdmFormProjectionDto> event) {
		IdmFormProjectionDto projection = event.getContent();
		//		
		service.deleteInternal(projection);
		//
		return new DefaultEventResult<>(event, this);
	}
}