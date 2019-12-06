package eu.bcvsolutions.idm.core.model.event.processor.codelist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.CodeListProcessor;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmCodeListService;
import eu.bcvsolutions.idm.core.model.event.CodeListEvent.CodeListEventType;

/**
 * Persists code lists
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
@Component(CodeListSaveProcessor.PROCESSOR_NAME)
@Description("Persists code lists.")
public class CodeListSaveProcessor
		extends CoreEventProcessor<IdmCodeListDto> 
		implements CodeListProcessor {
	
	public static final String PROCESSOR_NAME = "code-list-save-processor";
	//
	@Autowired private IdmCodeListService service;
	
	public CodeListSaveProcessor() {
		super(CodeListEventType.UPDATE, CodeListEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmCodeListDto> process(EntityEvent<IdmCodeListDto> event) {
		IdmCodeListDto codeList = event.getContent();
		//
		// prevent to change underlying form definition
		IdmCodeListDto originalSource = event.getOriginalSource();
		if (originalSource == null) {
			if (codeList.getFormDefinition() != null) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "formDefinition", "class", codeList.getClass().getSimpleName()));
			}
		} else {
			if (!codeList.getFormDefinition().equals(originalSource.getFormDefinition())) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "formDefinition", "class", codeList.getClass().getSimpleName()));
			}
		}
		codeList = service.saveInternal(codeList);
		event.setContent(codeList);
		//
		return new DefaultEventResult<>(event, this);
	}
}
