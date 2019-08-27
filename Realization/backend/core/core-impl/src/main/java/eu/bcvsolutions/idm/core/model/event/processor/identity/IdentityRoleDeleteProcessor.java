package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityRoleProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;

/**
 * Delete identity role
 * 
 * @author Radek Tomiška
 *
 */
@Component(IdentityRoleDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes identity role from repository.")
public class IdentityRoleDeleteProcessor 
		extends CoreEventProcessor<IdmIdentityRoleDto> 
		implements IdentityRoleProcessor {

	public static final String PROCESSOR_NAME = "identity-role-delete-processor";
	//
	@Autowired private IdmIdentityRoleService service;
	@Autowired private IdmIdentityRoleValidRequestService identityRoleValidRequestService;
	@Autowired private IdmRoleCompositionService roleCompositionService;

	public IdentityRoleDeleteProcessor() {
		super(IdentityRoleEventType.DELETE);
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
		// remove all IdentityRoleValidRequest for this role
		List<IdmIdentityRoleValidRequestDto> validRequests = identityRoleValidRequestService.findAllValidRequestForIdentityRoleId(identityRole.getId());
		identityRoleValidRequestService.deleteAll(validRequests);
		//
		// remove sub roles
		roleCompositionService.removeSubRoles(event);
		//
		// Delete identity role
		service.deleteInternal(identityRole);
		//
		return new DefaultEventResult<>(event, this);
	}
}
