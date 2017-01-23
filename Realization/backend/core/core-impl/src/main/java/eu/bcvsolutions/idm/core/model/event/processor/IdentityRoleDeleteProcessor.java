package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;

/**
 * Delete identity role
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes identity role from repository.")
public class IdentityRoleDeleteProcessor extends CoreEventProcessor<IdmIdentityRole> {

	public static final String PROCESSOR_NAME = "identity-role-delete-processor";
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
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityRole> process(EntityEvent<IdmIdentityRole> event) {
		repository.delete(event.getContent());
		//
		return new DefaultEventResult<>(event, this);
	}
}