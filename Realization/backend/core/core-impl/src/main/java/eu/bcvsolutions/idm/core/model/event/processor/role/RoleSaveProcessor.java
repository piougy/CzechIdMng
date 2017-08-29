package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;

/**
 * Persists role.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists role.")
public class RoleSaveProcessor extends CoreEventProcessor<IdmRoleDto> {
	
	public static final String PROCESSOR_NAME = "role-save-processor";
	private final IdmRoleService service;
	
	@Autowired
	public RoleSaveProcessor(IdmRoleService service) {
		super(RoleEventType.UPDATE, RoleEventType.CREATE);
		//
		Assert.notNull(service);
		//
		this.service = service;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		IdmRoleDto entity = event.getContent();
		entity = service.saveInternal(entity);
		event.setContent(entity);
		//
		// TODO: clone content - mutable previous event content :/
		return new DefaultEventResult<>(event, this);
	}

}
