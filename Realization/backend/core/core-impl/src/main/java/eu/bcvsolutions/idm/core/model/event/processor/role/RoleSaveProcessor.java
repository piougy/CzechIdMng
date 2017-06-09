package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;

/**
 * Persists role.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists role.")
public class RoleSaveProcessor extends CoreEventProcessor<IdmRole> {
	
	public static final String PROCESSOR_NAME = "role-save-processor";
	private final IdmRoleRepository repository;
	
	@Autowired
	public RoleSaveProcessor(IdmRoleRepository repository) {
		super(RoleEventType.UPDATE, RoleEventType.CREATE);
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
	public EventResult<IdmRole> process(EntityEvent<IdmRole> event) {
		IdmRole entity = event.getContent();
		//
		repository.save(entity);
		//
		// TODO: clone content - mutable previous event content :/
		return new DefaultEventResult<>(event, this);
	}

}
