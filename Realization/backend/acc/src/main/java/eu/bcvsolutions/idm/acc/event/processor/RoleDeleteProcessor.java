package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.RoleEventType;

/**
 * Before role delete - deletes all role system mappings
 * 
 * @author Radek Tomiška
 *
 */
@Order(-1)
@Component("accRoleDeleteProcessor")
public class RoleDeleteProcessor extends AbstractEntityEventProcessor<IdmRole> {
	
	private final SysRoleSystemService roleSystemService;
	
	@Autowired
	public RoleDeleteProcessor(SysRoleSystemService roleSystemService) {
		super(RoleEventType.DELETE);
		//
		Assert.notNull(roleSystemService);
		//
		this.roleSystemService = roleSystemService;
	}

	@Override
	public EventResult<IdmRole> process(EntityEvent<IdmRole> event) {
		// delete mapped roles
		RoleSystemFilter roleSystemFilter = new RoleSystemFilter();
		roleSystemFilter.setRoleId(event.getContent().getId());
		roleSystemService.find(roleSystemFilter, null).forEach(roleSystem -> {
			roleSystemService.delete(roleSystem);
		});
		//
		return new DefaultEventResult<>(event, this);
	}
}