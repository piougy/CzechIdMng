package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
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
	 * @deprecated since 9.0.0 use {@link IdmRoleCompositionService#findDirectSubRoles(UUID)}
	 */
	@Deprecated
	List<IdmRoleDto> getSubroles(UUID roleId);
	
	/**
	 * Get list of {@link IdmRoleDto} for role catalogue given in parameter.
	 * 
	 * @param roleCatalogue
	 * @return
	 */
	List<IdmRoleDto> findAllByRoleCatalogue(UUID roleCatalogueId);

	/**
	 * Get list of approvers for approving a changes of given role
	 * 
	 * First try to find guarantees for given role. If any guarantees will be found, then returns they.
	 * Without guarantees try to find approvers with role defined in property file {@link RoleConfiguration#getRoleForApproveChangeOfRole()}.
	 * 
	 * @param roleId
	 * @param pageable
	 * @return
	 */
	Page<IdmIdentityDto> findApproversByRoleId(UUID roleId, Pageable pageable);
	
	/**
	 * Returns code without environment suffix.
	 * e.g. code is 'code - env', 'code' is returned.
	 * 
	 * @param role
	 * @return
	 * @since 9.3.0
	 */
	String getCodeWithoutEnvironment(IdmRoleDto role);
	
	/**
	 * Returns code with environment suffix.
	 * e.g. code is 'code', environment is 'env', then 'code - env' is returned.
	 * 
	 * @param role
	 * @return
	 * @since 9.3.0
	 */
	String getCodeWithEnvironment(IdmRoleDto role);

	/**
	 * Finds form definition for given role by sub-definition. Returns only
	 * attributes that has same attribute sets in this role as sub-definition
	 * (IdmRoleFormAttribute). The default value returned in attributes is used from
	 * sub-definition attribute.
	 * 
	 * @param role
	 * @return
	 */
	IdmFormDefinitionDto getFormAttributeSubdefinition(IdmRoleDto role);
	
	/**
	 * Finds role with given base code and environment
	 * 
	 * @param baseCode
	 * @param environment [optional]
	 * @return
	 */
	IdmRoleDto getByBaseCodeAndEnvironment(String baseCode, String environment);
}
