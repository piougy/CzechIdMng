package eu.bcvsolutions.idm.core.eav.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.event.FormAttributeEvent.FormAttributeEventType;
import eu.bcvsolutions.idm.core.eav.api.event.processor.FormAttributeProcessor;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;

/**
 * Clear form definition cache, when form attribute is changed (CUD).
 * 
 * @author Radek Tomiška
 * @since 10.4.2
 */
@Component(FormAttributeEvictCacheProcessor.PROCESSOR_NAME)
@Description("Clear form definition cache, when form definition is changed (UD).")
public class FormAttributeEvictCacheProcessor 
		extends CoreEventProcessor<IdmFormAttributeDto> 
		implements FormAttributeProcessor {

	public static final String PROCESSOR_NAME = "core-form-attribute-evict-cache-processor";
	//
	@Autowired private FormService formService;

	public FormAttributeEvictCacheProcessor() {
		super(FormAttributeEventType.CREATE, FormAttributeEventType.UPDATE, FormAttributeEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmFormAttributeDto> process(EntityEvent<IdmFormAttributeDto> event) {
		IdmFormDefinitionDto definition = formService.getDefinition(event.getContent().getFormDefinition());
		// evict cached form definition
		formService.evictCache(definition);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 10;
	}
}
