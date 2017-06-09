package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.RoleAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.AccRoleAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;

/**
 * Before role delete - deletes all role system mappings
 * 
 * @author Radek Tomiška
 *
 */
@Component("accRoleDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class RoleDeleteProcessor extends AbstractEntityEventProcessor<IdmRole> {
	
	public static final String PROCESSOR_NAME = "role-delete-processor";
	private final SysRoleSystemService roleSystemService;
	private final AccRoleAccountService roleAccountService;
	
	@Autowired
	public RoleDeleteProcessor(SysRoleSystemService roleSystemService, AccRoleAccountService roleAccountService) {
		super(RoleEventType.DELETE);
		//
		Assert.notNull(roleSystemService);
		Assert.notNull(roleAccountService);
		//
		this.roleSystemService = roleSystemService;
		this.roleAccountService = roleAccountService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
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
		// delete relations on account (includes delete of account	)
		RoleAccountFilter filter = new RoleAccountFilter();
		filter.setRoleId(event.getContent().getId());
		roleAccountService.find(filter, null).forEach(roleAccount -> {
			roleAccountService.delete(roleAccount);
		});
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// right now before role delete
		return CoreEvent.DEFAULT_ORDER - 1;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
}