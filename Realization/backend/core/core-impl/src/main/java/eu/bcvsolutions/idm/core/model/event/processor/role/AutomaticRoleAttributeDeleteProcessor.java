package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.event.AutomaticRoleAttributeEvent.AutomaticRoleAttributeEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RemoveAutomaticRoleTaskExecutor;

@Component
@Description("Delete automatic role by attribute.")
public class AutomaticRoleAttributeDeleteProcessor extends CoreEventProcessor<IdmAutomaticRoleAttributeDto> {

	public static final String PROCESSOR_NAME = "automatic-role-attribute-delete-processor";
	
	private final LongRunningTaskManager longRunningTaskManager;
	
	@Autowired
	public AutomaticRoleAttributeDeleteProcessor(
			LongRunningTaskManager longRunningTaskManager) {
		super(AutomaticRoleAttributeEventType.DELETE);
		//
		Assert.notNull(longRunningTaskManager);
		//
		this.longRunningTaskManager = longRunningTaskManager;
	}
	
	@Override
	public EventResult<IdmAutomaticRoleAttributeDto> process(EntityEvent<IdmAutomaticRoleAttributeDto> event) {
		IdmAutomaticRoleAttributeDto content = event.getContent();
		//
		// delete all assigned roles gained by this automatic role by long running task
		RemoveAutomaticRoleTaskExecutor automaticRoleTask = AutowireHelper.createBean(RemoveAutomaticRoleTaskExecutor.class);
		automaticRoleTask.setAutomaticRoleId(content.getId());
		longRunningTaskManager.executeSync(automaticRoleTask);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
}
