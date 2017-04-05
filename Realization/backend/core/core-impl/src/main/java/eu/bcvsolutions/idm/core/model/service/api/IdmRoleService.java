package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
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
		IdentifiableByNameEntityService<IdmRole>, AuthorizableEntityService<IdmRole, RoleFilter> {

	public static final String WF_BY_ROLE_PRIORITY_PREFIX = "idm.sec.core.wf.role.approval.";
	
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
}
