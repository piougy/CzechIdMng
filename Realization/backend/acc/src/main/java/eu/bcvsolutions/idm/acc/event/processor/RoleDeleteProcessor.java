package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.RoleOperationType;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Delete role - deletes all role system mappings
 * 
 * @author Radek Tomiška
 *
 */
@Order(2)
@Component("accRoleDeleteProcessor")
public class RoleDeleteProcessor extends AbstractEntityEventProcessor<IdmRole> {
	
	public RoleDeleteProcessor() {
		super(RoleOperationType.DELETE);
		
	}

	@Override
	public EntityEvent<IdmRole> process(EntityEvent<IdmRole> event) {
		Assert.notNull(event.getContent());
		
		return event;
	}
}