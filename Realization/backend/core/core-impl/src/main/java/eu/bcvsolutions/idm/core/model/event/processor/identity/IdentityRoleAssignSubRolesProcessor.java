package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityRoleProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;

/**
 * Assign sub roles of currently assigned identity roles.
 * - assign direct sub roles only, works recursively
 * - prevents cycles (just for sure) - adds processed roles into event property
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
@Component(IdentityRoleAssignSubRolesProcessor.PROCESSOR_NAME)
@Description("Assing sub roles of currently assigned identity roles.")
public class IdentityRoleAssignSubRolesProcessor
		extends CoreEventProcessor<IdmIdentityRoleDto>
		implements IdentityRoleProcessor {
	
	public static final String PROCESSOR_NAME = "core-identity-role-assign-subroles-processor";
	//
	@Autowired private IdmRoleCompositionService roleCompositionService;
	
	public IdentityRoleAssignSubRolesProcessor() {
		super(IdentityRoleEventType.NOTIFY);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmIdentityRoleDto> process(EntityEvent<IdmIdentityRoleDto> event) {
		IdmIdentityRoleDto identityRole = event.getContent();
		Assert.notNull(identityRole.getId());
		//
		if (CoreEventType.CREATE.name().equals(event.getParentType())) {
			// create
			roleCompositionService.assignSubRoles(event);
		} else if (CoreEventType.UPDATE.name().equals(event.getParentType())) {
			// update
			roleCompositionService.updateSubRoles(event);
		}
		// default will be constructed in abstract
		return null;
	}
	
	@Override
	public int getOrder() {
		return 500;
	}
}
