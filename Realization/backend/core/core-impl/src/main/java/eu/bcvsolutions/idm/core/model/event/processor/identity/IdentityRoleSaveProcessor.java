package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityRoleProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;

/**
 * Save identity role
 * 
 * @author Radek Tomiška
 *
 */
@Component(IdentityRoleSaveProcessor.PROCESSOR_NAME)
@Description("Persists identity role.")
public class IdentityRoleSaveProcessor 
		extends CoreEventProcessor<IdmIdentityRoleDto> 
		implements IdentityRoleProcessor {

	public static final String PROCESSOR_NAME = "identity-role-save-processor";
	private final IdmIdentityRoleService service;
	private final IdmIdentityRoleValidRequestService validRequestService;
	
	@Autowired
	public IdentityRoleSaveProcessor(
			IdmIdentityRoleService service,
			IdmIdentityRoleValidRequestService validRequestService) {
		super(IdentityRoleEventType.CREATE, IdentityRoleEventType.UPDATE);
		//
		Assert.notNull(service);
		Assert.notNull(validRequestService);
		//
		this.service = service;
		this.validRequestService = validRequestService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityRoleDto> process(EntityEvent<IdmIdentityRoleDto> event) {
		IdmIdentityRoleDto identityRole = event.getContent();
		identityRole = service.saveInternal(identityRole);
		event.setContent(identityRole);
		//
		// if identityRole isn't valid save request into validRequests
		if (!EntityUtils.isValid(identityRole)) {
			// create new IdmIdentityRoleValidRequest
			validRequestService.createByIdentityRoleId(identityRole.getId());
		}
		//
		return new DefaultEventResult<>(event, this);
	}
}