package eu.bcvsolutions.idm.core.model.service.api;

import java.util.Set;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * Automatic role service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleTreeNodeService extends ReadWriteDtoService<IdmRoleTreeNodeDto, IdmRoleTreeNode, RoleTreeNodeFilter> {
	
	/**
	 * Returns all automatic role for given work position. 
	 * 
	 * @param workPosition
	 * @return
	 */
	Set<IdmRoleTreeNode> getAutomaticRoles(IdmTreeNode workPosition);
	
	/**
	 * Assign automatic roles by standard role request.
	 * Start of process is defined by parameter startRequestInternal.
	 * 
	 * @param contract
	 * @param automaticRoles
	 * @param startRequestInternal
	 * @return
	 */
	IdmRoleRequestDto assignAutomaticRoles(IdmIdentityContract contract, Set<IdmRoleTreeNode> automaticRoles, boolean startRequestInternal);
	
	/**
	 * Delete automatic roles by standard role request.
	 * Start of process is defined by parameter startRequestInternal.
	 * 
	 * @param identityRole
	 * @param automaticRoles
	 * @param startRequestInternal
	 * @return
	 */
	IdmRoleRequestDto removeAutomaticRoles(IdmIdentityRole identityRole, Set<IdmRoleTreeNode> automaticRoles, boolean startRequestInternal);
}
