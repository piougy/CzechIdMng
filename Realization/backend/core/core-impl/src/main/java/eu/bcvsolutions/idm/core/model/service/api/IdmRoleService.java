package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.CodeableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableEntityService;

/**
 * Role service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleService 
		extends ReadWriteEntityService<IdmRole, RoleFilter>, 
		CodeableService<IdmRole>, AuthorizableEntityService<IdmRole, RoleFilter>, ScriptEnabled {
	
	static final String WF_BY_ROLE_PRIORITY_PREFIX = "idm.sec.core.wf.role.approval."; // TODO: rename property ... 
	static final String PROPERTY_DEFAULT_ROLE = RoleConfiguration.PROPERTY_DEFAULT_ROLE;
	
	/**
	 * Returns role by name
	 * 
	 * @param name
	 * @return
	 */
	IdmRole getByName(String name);
	
	/**
	 * Return roles by uuids in string
	 * 
	 * @param roles
	 * @return
	 */
	List<IdmRole> getRolesByIds(String roleIds);

	/**
	 * Find workflow definition key for assign Role to Identity
	 * @param roleId
	 * @return
	 */
	String findAssignRoleWorkflowDefinition(UUID roleId);


	/**
	 * Find workflow definition key for change assigned Role on Identity
	 * @param roleId
	 * @return
	 */
	String findChangeAssignRoleWorkflowDefinition(UUID roleId);


	/**
	 * Find workflow definition key for remove assigned Role on Identity
	 * @param roleId
	 * @return
	 */
	String findUnAssignRoleWorkflowDefinition(UUID roleId);
	
	/**
	 * Returns default user role by configuration {@value #PROPERTY_DEFAULT_ROLE}.
	 * 
	 * @return
	 * @see RoleConfiguration#getDefaultRoleId()
	 */
	IdmRole getDefaultRole();
	
	/**
	 * Returns admin user role by configuration {@value #PROPERTY_ADMIN_ROLE}.
	 * 
	 * @return
	 * @see RoleConfiguration#getAdminRoleId()
	 */
	IdmRole getAdminRole();
	
	/**
	 * Return list of subroles (only one level in depth)
	 * of role given by its role ID. 
	 * @param roleId
	 * @return
	 */
	List<IdmRole> getSubroles(UUID roleId);
}
