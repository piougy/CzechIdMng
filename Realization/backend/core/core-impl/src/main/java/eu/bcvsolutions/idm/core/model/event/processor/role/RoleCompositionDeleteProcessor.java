package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleCompositionProcessor;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.event.RoleCompositionEvent.RoleCompositionEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RemoveRoleCompositionTaskExecutor;

/**
 * Deletes role composition - ensures referential integrity.
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
@Component(RoleCompositionDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes role composition from repository.")
public class RoleCompositionDeleteProcessor
		extends CoreEventProcessor<IdmRoleCompositionDto>
		implements RoleCompositionProcessor{
	
	public static final String PROCESSOR_NAME = "core-role-composition-delete-processor";
	//
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	
	public RoleCompositionDeleteProcessor() {
		super(RoleCompositionEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleCompositionDto> process(EntityEvent<IdmRoleCompositionDto> event) {
		IdmRoleCompositionDto roleComposition = event.getContent();
		//
		if (roleComposition.getId() != null) {
			//
			// delete all assigned roles gained by this automatic role by long running task
			RemoveRoleCompositionTaskExecutor roleCompositionTask = AutowireHelper.createBean(RemoveRoleCompositionTaskExecutor.class);
			roleCompositionTask.setRoleCompositionId(roleComposition.getId());
			longRunningTaskManager.execute(roleCompositionTask);
			// TODO: new flag asynchronous?
			return new DefaultEventResult.Builder<>(event, this).setSuspended(true).build();
		}
		//
		return new DefaultEventResult<>(event, this);
	}
}