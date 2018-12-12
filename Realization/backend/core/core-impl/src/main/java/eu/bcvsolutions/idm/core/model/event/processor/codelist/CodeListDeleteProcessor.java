package eu.bcvsolutions.idm.core.model.event.processor.codelist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.CodeListProcessor;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmCodeListService;
import eu.bcvsolutions.idm.core.model.event.CodeListEvent.CodeListEventType;

/**
 * Deletes code list - ensures referential integrity.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
@Component(CodeListDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes code list - ensures referential integrity.")
public class CodeListDeleteProcessor
		extends CoreEventProcessor<IdmCodeListDto>
		implements CodeListProcessor{
	
	public static final String PROCESSOR_NAME = "code-list-delete-processor";
	@Autowired private IdmCodeListService service;
	
	public CodeListDeleteProcessor() {
		super(CodeListEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmCodeListDto> process(EntityEvent<IdmCodeListDto> event) {
		IdmCodeListDto codeList = event.getContent();
		//		
		service.deleteInternal(codeList);
		//
		return new DefaultEventResult<>(event, this);
	}
}