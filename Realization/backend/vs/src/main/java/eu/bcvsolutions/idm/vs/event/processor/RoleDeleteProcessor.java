package eu.bcvsolutions.idm.vs.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.vs.dto.filter.VsSystemImplementerFilter;
import eu.bcvsolutions.idm.vs.service.api.VsSystemImplementerService;

/**
 * Before role delete we need delete vs implementers
 * 
 * @author svandav
 *
 */
@Component("vsRoleDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class RoleDeleteProcessor extends AbstractEntityEventProcessor<IdmRoleDto> {

	public static final String PROCESSOR_NAME = "role-delete-processor";
	private final VsSystemImplementerService systemImplementerService;

	@Autowired
	public RoleDeleteProcessor(VsSystemImplementerService systemImplementerService) {
		super(RoleEventType.DELETE);
		//
		Assert.notNull(systemImplementerService);
		//
		this.systemImplementerService = systemImplementerService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {

		IdmRoleDto role = event.getContent();
		Assert.notNull(role);
		Assert.notNull(role.getId());
		// Delete vs implementers
		VsSystemImplementerFilter implementerFilter = new VsSystemImplementerFilter();
		implementerFilter.setRoleId(role.getId());
		systemImplementerService.find(implementerFilter, null).forEach(entity -> {
			systemImplementerService.delete(entity);
		});
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// right now before identity delete
		return CoreEvent.DEFAULT_ORDER - 1;
	}

	@Override
	public boolean isDisableable() {
		return false;
	}
}