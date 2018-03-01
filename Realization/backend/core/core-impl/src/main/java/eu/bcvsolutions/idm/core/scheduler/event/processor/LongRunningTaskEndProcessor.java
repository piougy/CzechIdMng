package eu.bcvsolutions.idm.core.scheduler.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.LongRunningTaskProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.event.LongRunningTaskEvent.LongRunningTaskEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;

/**
 * Ends long running task and persists him.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Ends long running task and persists task's state.")
public class LongRunningTaskEndProcessor 
		extends CoreEventProcessor<IdmLongRunningTaskDto> 
		implements LongRunningTaskProcessor {
	
	public static final String PROCESSOR_NAME = "long-running-task-end-processor";
	//
	@Autowired protected IdmLongRunningTaskService service;
	
	public LongRunningTaskEndProcessor() {
		super(LongRunningTaskEventType.END);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmLongRunningTaskDto> process(EntityEvent<IdmLongRunningTaskDto> event) {
		IdmLongRunningTaskDto task = event.getContent();
		// end task
		task.setRunning(false);
		// and persist state
		service.save(task);
		event.setContent(task);
		//
		return new DefaultEventResult<>(event, this);
	}

}
