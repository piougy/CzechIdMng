package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.IdentityRoleOperationType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
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
		super(IdentityRoleOperationType.DELETE);
		//
		Assert.notNull(repository);
		//
		this.repository = repository;
	}

	@Override
	public EntityEvent<IdmIdentityRole> process(EntityEvent<IdmIdentityRole> context) {
		Assert.notNull(context.getContent());
		//
		repository.delete(context.getContent());
		//
		return context;
	}
}