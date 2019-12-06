package eu.bcvsolutions.idm.core.scheduler.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.event.LongRunningTaskEvent.LongRunningTaskEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;

/**
 * Deletes long running task from repository.
 * 
 * @author Radek Tomiška
 * @since 9.7.12
 */
@Component(LongRunningTaskDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes long running task from repository.")
public class LongRunningTaskDeleteProcessor
		extends CoreEventProcessor<IdmLongRunningTaskDto> {
	
	public static final String PROCESSOR_NAME = "core-long-running-task-delete-processor";
	//
	@Autowired private IdmLongRunningTaskService service;
	@Autowired private AttachmentManager attachmentManager;
	
	public LongRunningTaskDeleteProcessor() {
		super(LongRunningTaskEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmLongRunningTaskDto> process(EntityEvent<IdmLongRunningTaskDto> event) {
		IdmLongRunningTaskDto lrt = event.getContent();
		//
		attachmentManager.deleteAttachments(lrt);
		//		
		service.deleteInternal(lrt);
		//
		return new DefaultEventResult<>(event, this);
	}
}