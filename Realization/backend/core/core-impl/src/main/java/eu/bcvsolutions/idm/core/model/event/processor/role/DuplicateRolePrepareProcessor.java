package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;

/**
 * Duplicate role - prepare role's basic properties. 
 * 
 * - register custom processor after this processor's order, if some property has to be overriden.
 * 
 * @author Radek Tomiška
 * @since 9.5.0
 */
@Component(DuplicateRolePrepareProcessor.PROCESSOR_NAME)
@Description("Duplicate role - prepare role's basic data. ")
public class DuplicateRolePrepareProcessor
		extends CoreEventProcessor<IdmRoleDto> 
		implements RoleProcessor {
	
	public static final String PROCESSOR_NAME = "core-duplicate-role-prepare-processor";
	
	public DuplicateRolePrepareProcessor() {
		super(RoleEventType.DUPLICATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		IdmRoleDto duplicate = event.getContent(); // newly setted role
		IdmRoleDto originalSource = event.getOriginalSource(); // cloned role
		//
		duplicate.setName(originalSource.getName());
		duplicate.setDescription(originalSource.getDescription());
		duplicate.setApproveRemove(originalSource.isApproveRemove());
		duplicate.setCanBeRequested(originalSource.isCanBeRequested());
		duplicate.setDisabled(originalSource.isDisabled());
		duplicate.setRoleType(originalSource.getRoleType());
		duplicate.setPriority(originalSource.getPriority());
		//
		// it's here ... prevent to call two save on duplicated role (or create the second processor :/)
		if (getBooleanProperty(DuplicateRoleFormAttributeProcessor.PARAMETER_INCLUDE_ROLE_FORM_ATTRIBUTE, event.getProperties())) {
			duplicate.setIdentityRoleAttributeDefinition(originalSource.getIdentityRoleAttributeDefinition());
		}
		//
		event.setContent(duplicate);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return -1000;
	}

}
