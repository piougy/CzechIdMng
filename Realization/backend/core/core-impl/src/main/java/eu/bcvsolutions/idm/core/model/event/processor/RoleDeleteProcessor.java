package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.RoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;

/**
 * Delete role
 * 
 * @author Radek Tomiška
 *
 */
@Order(0)
@Component
public class RoleDeleteProcessor extends AbstractEntityEventProcessor<IdmRole> {
	
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