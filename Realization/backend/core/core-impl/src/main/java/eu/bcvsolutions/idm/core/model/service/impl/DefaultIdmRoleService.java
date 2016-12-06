package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.RoleOperationType;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.EntityEventProcessorService;
import eu.bcvsolutions.idm.core.model.dto.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;

/**
 * Default role service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmRoleService extends AbstractReadWriteEntityService<IdmRole, RoleFilter>  implements IdmRoleService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmRoleService.class);
	private final IdmRoleRepository roleRepository;
	private final EntityEventProcessorService entityEventProcessorService;
	
	@Autowired
	public DefaultIdmRoleService(
			IdmRoleRepository roleRepository,
			EntityEventProcessorService entityEventProcessorService) {
		super(roleRepository);
		//
		Assert.notNull(entityEventProcessorService);
		//
		this.roleRepository = roleRepository;
		this.entityEventProcessorService = entityEventProcessorService;
	}

	@Override
	@Transactional(readOnly = true)
	public IdmRole getByName(String name) {
		return roleRepository.findOneByName(name);
	}
	
	@Override
	@Transactional
	public void delete(IdmRole role) {
		Assert.notNull(role);
		//
		LOG.debug("Deleting role [{}]", role.getName());
		entityEventProcessorService.process(new RoleEvent(RoleOperationType.DELETE, role));
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmRole> getRolesByIds(String roles) {
		if (roles == null) {
			return null;
		}
		List<IdmRole> idmRoles = new ArrayList<>();
		String[] rolesArray = roles.split(",");
		for (String id : rolesArray) {
			// TODO: try - catch ...
			idmRoles.add(get(UUID.fromString(id)));
		}
		return idmRoles;
	}
}
