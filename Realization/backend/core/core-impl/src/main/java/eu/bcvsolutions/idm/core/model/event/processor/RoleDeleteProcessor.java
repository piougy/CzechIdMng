package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;

/**
 * Deletes role - ensures referential integrity.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes role from repository.")
public class RoleDeleteProcessor extends CoreEventProcessor<IdmRole> {
	
	public static final String PROCESSOR_NAME = "role-delete-processor";
	private final IdmRoleRepository repository;
	private final IdmIdentityRoleRepository identityRoleRepository;
	
	@Autowired
	public RoleDeleteProcessor(
			IdmRoleRepository repository,
			IdmIdentityRoleRepository identityRoleRepository) {
		super(RoleEventType.DELETE);
		//
		Assert.notNull(repository);
		Assert.notNull(identityRoleRepository);
		//
		this.repository = repository;
		this.identityRoleRepository = identityRoleRepository;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRole> process(EntityEvent<IdmRole> event) {
		IdmRole role = event.getContent();
		
		// role assigned to identity could not be deleted
		if(identityRoleRepository.countByRole(role) > 0) {
			throw new ResultCodeException(CoreResultCode.ROLE_DELETE_FAILED_IDENTITY_ASSIGNED, ImmutableMap.of("role", role.getName()));
		}
		// guarantees and compositions are deleted by hibernate mapping
		repository.delete(role);
		//
		return new DefaultEventResult<>(event, this);
	}
}