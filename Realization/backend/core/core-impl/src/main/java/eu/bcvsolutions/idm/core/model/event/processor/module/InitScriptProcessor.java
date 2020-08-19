package eu.bcvsolutions.idm.core.model.event.processor.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;

/**
 * Init scripts.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitScriptProcessor.PROCESSOR_NAME)
@Description("Init scripts from classpath (file system).")
public class InitScriptProcessor extends AbstractInitApplicationProcessor {

	public static final String PROCESSOR_NAME = "core-init-script-processor";
	//
	@Autowired private IdmScriptService scriptService;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		//
		// initial missing scripts, current scripts isn't re-deployed
		scriptService.init();
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER - 5200;
	}
}
