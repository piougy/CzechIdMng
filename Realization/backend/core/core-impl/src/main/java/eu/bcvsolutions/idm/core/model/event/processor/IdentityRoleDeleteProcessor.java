package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;

/**
 * Delete identity role
 * 
 * @author Radek Tomiška
 *
 */
@Order(0)
@Component
public class IdentityRoleDeleteProcessor extends AbstractEntityEventProcessor<IdmIdentityRole> {

	private final IdmIdentityRoleRepository repository;
	
	@Autowired
	public IdentityRoleDeleteProcessor(
			IdmIdentityRoleRepository repository) {
		super(IdentityRoleEventType.DELETE);
		//
		Assert.notNull(repository);
		//
		this.repository = repository;
	}

	@Override
	public EventResult<IdmIdentityRole> process(EntityEvent<IdmIdentityRole> event) {
		repository.delete(event.getContent());
		//
		return new DefaultEventResult<>(event, this);
	}
}