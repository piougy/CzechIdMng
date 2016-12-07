package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.RoleOperationType;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

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
		super(RoleOperationType.DELETE);
		//
		Assert.notNull(roleSystemService);
		//
		this.roleSystemService = roleSystemService;
	}

	@Override
	public EntityEvent<IdmRole> process(EntityEvent<IdmRole> event) {
		Assert.notNull(event.getContent());
		
		// delete mapped roles
		RoleSystemFilter roleSystemFilter = new RoleSystemFilter();
		roleSystemFilter.setRoleId(event.getContent().getId());
		roleSystemService.find(roleSystemFilter, null).forEach(roleSystem -> {
			roleSystemService.delete(roleSystem);
		});
		
		return event;
	}
}