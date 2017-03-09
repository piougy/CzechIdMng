package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormableService;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;

/**
 * Default role service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmRoleService extends AbstractFormableService<IdmRole, RoleFilter>  implements IdmRoleService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmRoleService.class);
	private final IdmRoleRepository repository;
	private final EntityEventManager entityEventProcessorService;
	
	@Autowired
	public DefaultIdmRoleService(
			IdmRoleRepository repository,
			EntityEventManager entityEventProcessorService,
			FormService formService) {
		super(repository, formService);
		//
		Assert.notNull(entityEventProcessorService);
		//
		this.repository = repository;
		this.entityEventProcessorService = entityEventProcessorService;
	}

	@Override
	@Transactional(readOnly = true)
	public IdmRole getByName(String name) {
		return repository.findOneByName(name);
	}
	
	@Override
	@Transactional
	public IdmRole save(IdmRole entity) {
		return super.save(entity);
	}
	
	@Override
	@Transactional
	public void delete(IdmRole role) {
		Assert.notNull(role);
		//
		LOG.debug("Deleting role [{}]", role.getName());
		entityEventProcessorService.process(new RoleEvent(RoleEventType.DELETE, role));
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
