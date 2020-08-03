package eu.bcvsolutions.idm.core.eav.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.event.FormDefinitionEvent.FormDefinitionEventType;
import eu.bcvsolutions.idm.core.eav.api.event.processor.FormDefinitionProcessor;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;

/**
 * Clear form definition cache, when form definition is changed (UD).
 * 
 * @author Radek Tomiška
 * @since 10.4.2
 */
@Component(FormDefinitionEvictCacheProcessor.PROCESSOR_NAME)
@Description("Clear form definition cache, when form definition is changed (UD).")
public class FormDefinitionEvictCacheProcessor 
		extends CoreEventProcessor<IdmFormDefinitionDto> 
		implements FormDefinitionProcessor {

	public static final String PROCESSOR_NAME = "core-form-definition-evict-cache-processor";
	//
	@Autowired private FormService formService;

	public FormDefinitionEvictCacheProcessor() {
		super(FormDefinitionEventType.CREATE, FormDefinitionEventType.UPDATE, FormDefinitionEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmFormDefinitionDto> process(EntityEvent<IdmFormDefinitionDto> event) {
		// evict cached form definition
		formService.evictCache(event.getContent());
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 10;
	}
}
