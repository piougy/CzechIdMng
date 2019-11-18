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
 * Persists LRT.
 * 
 * @author Radek Tomiška
 * @since 9.7.12
 */
@Component(LongRunningTaskSaveProcessor.PROCESSOR_NAME)
@Description("Persists LRT.")
public class LongRunningTaskSaveProcessor
		extends CoreEventProcessor<IdmLongRunningTaskDto> 
		implements LongRunningTaskProcessor  {
	
	public static final String PROCESSOR_NAME = "core-long-running-task-save-processor";
	//
	@Autowired private IdmLongRunningTaskService service;
	
	public LongRunningTaskSaveProcessor() {
		super(LongRunningTaskEventType.UPDATE, LongRunningTaskEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmLongRunningTaskDto> process(EntityEvent<IdmLongRunningTaskDto> event) {
		IdmLongRunningTaskDto lrt = event.getContent();
		lrt = service.saveInternal(lrt);
		event.setContent(lrt);
		//
		return new DefaultEventResult<>(event, this);
	}
}
