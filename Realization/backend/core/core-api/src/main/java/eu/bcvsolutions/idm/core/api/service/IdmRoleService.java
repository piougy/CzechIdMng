package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Role service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleService extends 
		EventableDtoService<IdmRoleDto, IdmRoleFilter>, 
		CodeableService<IdmRoleDto>, 
		AuthorizableService<IdmRoleDto>, ScriptEnabled {
	
	String WF_BY_ROLE_PRIORITY_PREFIX = "idm.sec.core.wf.role.approval."; // TODO: rename property ... 
	String PROPERTY_DEFAULT_ROLE = RoleConfiguration.PROPERTY_DEFAULT_ROLE;
	
	/**
	 * Returns role by name
	 * 
	 * @param name
	 * @return
	 * @deprecated - use {@link #getByCode(String)}
	 */
	@Deprecated
	IdmRoleDto getByName(String name);
	
	/**
	 * Return roles by uuids in string
	 * 
	 * @param roles
	 * @return
	 */
	List<IdmRoleDto> getRolesByIds(String roleIds);

	/**
	 * Find workflow definition key for assign Role to Identity
	 * 
	 * @param roleId
	 * @return
	 */
	String findAssignRoleWorkflowDefinition(UUID roleId);


	/**
	 * Find workflow definition key for change assigned Role on Identity
	 * 
	 * @param roleId
	 * @return
	 */
	String findChangeAssignRoleWorkflowDefinition(UUID roleId);


	/**
	 * Find workflow definition key for remove assigned Role on Identity
	 * 
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
	IdmRoleDto getDefaultRole();
	
	/**
	 * Returns admin user role by configuration {@value #PROPERTY_ADMIN_ROLE}.
	 * 
	 * @return
	 * @see RoleConfiguration#getAdminRoleId()
	 */
	IdmRoleDto getAdminRole();
	
	/**
	 * Return list of subroles (only one level in depth)
	 * of role given by its role ID. 
	 * 
	 * @param roleId
	 * @return
	 */
	List<IdmRoleDto> getSubroles(UUID roleId);
	
	/**
	 * Get list of {@link IdmRoleDto} for role catalogue given in parameter.
	 * 
	 * @param roleCatalogue
	 * @return
	 */
	List<IdmRoleDto> findAllByRoleCatalogue(UUID roleCatalogueId);
}
