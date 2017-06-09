package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleValidRequestService;

/**
 * Save identity role
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists identity role.")
public class IdentityRoleSaveProcessor extends CoreEventProcessor<IdmIdentityRoleDto> {

	public static final String PROCESSOR_NAME = "identity-role-save-processor";
	private final IdmIdentityRoleService service;
	private final IdmIdentityRoleValidRequestService validRequestService;
	private final ModelMapper modelMapper;
	
	@Autowired
	public IdentityRoleSaveProcessor(
		IdmIdentityRoleService service,
		IdmIdentityRoleValidRequestService validRequestService, ModelMapper modelMapper) {
		super(IdentityRoleEventType.CREATE, IdentityRoleEventType.UPDATE);
		//
		Assert.notNull(service);
		Assert.notNull(validRequestService);
		Assert.notNull(modelMapper);
		//
		this.service = service;
		this.validRequestService = validRequestService;
		this.modelMapper = modelMapper;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityRoleDto> process(EntityEvent<IdmIdentityRoleDto> event) {
		IdmIdentityRoleDto identityRole = event.getContent();
		identityRole = service.saveInternal(identityRole);
		//TODO: Create validable DTO? Or validate in service?
		final IdmIdentityRole roleEntity = new IdmIdentityRole();
		modelMapper.map(identityRole, roleEntity);
		event.setContent(identityRole);
		//
		// if identityRole isn't valid save request into validRequests
		if (!EntityUtils.isValid(roleEntity)) {
			// create new IdmIdentityRoleValidRequest
			validRequestService.createByIdentityRoleId(identityRole.getId());
		}
		//
		return new DefaultEventResult<>(event, this);
	}
}