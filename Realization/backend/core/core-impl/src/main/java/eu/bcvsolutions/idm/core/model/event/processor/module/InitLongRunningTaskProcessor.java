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
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;

/**
 * Cancels all previously ran tasks etc. (before server is restarted).
 * 
 * TODO: recoverable task can be resurected instead.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitLongRunningTaskProcessor.PROCESSOR_NAME)
@Description("Cancels all previously ran tasks etc. (tasks run, before server was restarted).")
public class InitLongRunningTaskProcessor extends AbstractInitApplicationProcessor {

	public static final String PROCESSOR_NAME = "core-init-long-running-task-processor";
	//
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		//
		// Cancels all previously ran tasks
		longRunningTaskManager.init();
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// before all
		return CoreEvent.DEFAULT_ORDER - 10000;
	}
}
