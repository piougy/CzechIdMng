package eu.bcvsolutions.idm.core.model.event.processor.codelist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.CodeListItemProcessor;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListItemDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmCodeListItemService;
import eu.bcvsolutions.idm.core.model.event.CodeListItemEvent.CodeListItemEventType;

/**
 * Deletes code list items - ensures referential integrity.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
@Component(CodeListItemDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes code list items - ensures referential integrity.")
public class CodeListItemDeleteProcessor
		extends CoreEventProcessor<IdmCodeListItemDto>
		implements CodeListItemProcessor{
	
	public static final String PROCESSOR_NAME = "code-list-item-delete-processor";
	@Autowired private IdmCodeListItemService service;
	
	public CodeListItemDeleteProcessor() {
		super(CodeListItemEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmCodeListItemDto> process(EntityEvent<IdmCodeListItemDto> event) {
		IdmCodeListItemDto item = event.getContent();
		//		
		service.deleteInternal(item);
		//
		return new DefaultEventResult<>(event, this);
	}
}