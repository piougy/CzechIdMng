package eu.bcvsolutions.idm.core.model.event.processor.script;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ScriptProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.model.event.ScriptEvent.ScriptEventType;

/**
 * Deletes notification template - ensures referential integrity.
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
@Component(ScriptDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes notification template.")
public class ScriptDeleteProcessor 
		extends CoreEventProcessor<IdmScriptDto>
		implements ScriptProcessor {

	public static final String PROCESSOR_NAME = "core-script-delete-processor";

	@Autowired private IdmScriptService scriptService;

	public ScriptDeleteProcessor() {
		super(ScriptEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmScriptDto> process(EntityEvent<IdmScriptDto> event) {
		IdmScriptDto script = event.getContent();
		//		
		scriptService.deleteInternal(script);
		//
		return new DefaultEventResult<>(event, this);
	}
}
