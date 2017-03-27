package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.base.Strings;

import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormableService;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.model.event.processor.RoleDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.RoleSaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;

/**
 * Default role service
 * - supports {@link RoleEvent}
 * 
 * @author Radek Tomiška
 *
 */
@Service("roleService")
public class DefaultIdmRoleService extends AbstractFormableService<IdmRole, RoleFilter>  implements IdmRoleService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmRoleService.class);
	private final IdmRoleRepository repository;
	private final EntityEventManager entityEventManager;
	
	@Autowired
	public DefaultIdmRoleService(
			IdmRoleRepository repository,
			EntityEventManager entityEventManager,
			FormService formService) {
		super(repository, formService);
		//
		Assert.notNull(entityEventManager);
		//
		this.repository = repository;
		this.entityEventManager = entityEventManager;
	}

	@Override
	@Transactional(readOnly = true)
	public IdmRole getByName(String name) {
		return repository.findOneByName(name);
	}
	
	/**
	 * Publish {@link RoleEvent} only.
	 * 
	 * @see {@link RoleSaveProcessor}
	 */
	@Override
	@Transactional
	public IdmRole save(IdmRole role) {
		Assert.notNull(role);
		//
		LOG.debug("Saving role [{}]", role.getName());
		//
		return entityEventManager.process(new RoleEvent(isNew(role) ? RoleEventType.CREATE : RoleEventType.UPDATE, role)).getContent();
	}
	
	/**
	 * Publish {@link RoleEvent} only.
	 * 
	 * @see {@link RoleDeleteProcessor}
	 */
	@Override
	@Transactional
	public void delete(IdmRole role) {
		Assert.notNull(role);
		//
		LOG.debug("Deleting role [{}]", role.getName());
		entityEventManager.process(new RoleEvent(RoleEventType.DELETE, role));
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
	
	@Override
	public String findAssignRoleWorkflowDefinition(UUID roleId){
		Assert.notNull(roleId, "Role ID is required!");
		
		String key =  "change-role-without-approve";
		return Strings.isNullOrEmpty(key) ? null : key;
	}

	@Override
	public String findChangeAssignRoleWorkflowDefinition(UUID roleId){
		Assert.notNull(roleId, "Role ID is required!");
	
		String key =  this.get(roleId).getApproveAddWorkflow();
		return Strings.isNullOrEmpty(key) ? null : key;
	}
	
	@Override
	public String findUnAssignRoleWorkflowDefinition(UUID roleId){
		Assert.notNull(roleId, "Role ID is required!");

		String key =  this.get(roleId).getApproveRemoveWorkflow();
		return Strings.isNullOrEmpty(key) ? null : key;
	}
	
}
