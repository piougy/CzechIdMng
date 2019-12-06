package eu.bcvsolutions.idm.example.bulk.action.impl;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;

/**
 * Duplicate role - example processor - override description value by name 
 * 
 * @author Radek Tomiška
 * @since 9.5.0
 */
@Enabled(ExampleModuleDescriptor.MODULE_ID)
@Component(CustomDuplicateRolePrepareProcessor.PROCESSOR_NAME)
@Description("Duplicate role - example processor - override description value by name.")
public class CustomDuplicateRolePrepareProcessor
		extends CoreEventProcessor<IdmRoleDto> 
		implements RoleProcessor {
	
	public static final String PROCESSOR_NAME = "example-duplicate-role-prepare-processor";
	
	public CustomDuplicateRolePrepareProcessor() {
		super(RoleEventType.DUPLICATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		IdmRoleDto duplicate = event.getContent();
		IdmRoleDto originalSource = event.getOriginalSource();
		//
		duplicate.setDescription(originalSource.getName());
		event.setContent(duplicate);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return -900; // after core prepare data
	}
}
