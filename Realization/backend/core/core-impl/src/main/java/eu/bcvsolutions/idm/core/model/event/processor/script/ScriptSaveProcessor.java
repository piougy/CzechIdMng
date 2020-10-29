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
 * Saves, updates script.
 * 
 * @author Radek Tomiška
 *@since 10.6.0
 */
@Component(ScriptSaveProcessor.PROCESSOR_NAME)
@Description("Saves and updates srcipt.")
public class ScriptSaveProcessor 
		extends CoreEventProcessor<IdmScriptDto>
		implements ScriptProcessor {

	public static final String PROCESSOR_NAME = "core-script-save-processor";
	//
	@Autowired private IdmScriptService scriptService;

	public ScriptSaveProcessor() {
		super(ScriptEventType.CREATE, ScriptEventType.UPDATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmScriptDto> process(EntityEvent<IdmScriptDto> event) {
		IdmScriptDto script = event.getContent();
		script = scriptService.saveInternal(script);
		event.setContent(script);
		//
		return new DefaultEventResult<>(event, this);
	}
}
