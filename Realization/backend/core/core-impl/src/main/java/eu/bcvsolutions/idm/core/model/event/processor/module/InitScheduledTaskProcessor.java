package eu.bcvsolutions.idm.core.model.event.processor.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.InitCoreScheduledTask;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;

/**
 * Schedule core long running tasks.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitScheduledTaskProcessor.PROCESSOR_NAME)
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", matchIfMissing = true)
@Description("Schedule core long running tasks.")
public class InitScheduledTaskProcessor extends AbstractInitApplicationProcessor {

	public static final String PROCESSOR_NAME = "core-init-scheduled-task-processor";
	//
	@Autowired private InitCoreScheduledTask coreScheduledTaskInitializer;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		coreScheduledTaskInitializer.init();
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// after all
		return CoreEvent.DEFAULT_ORDER + 10000;
	}
}
