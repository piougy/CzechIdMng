package eu.bcvsolutions.idm.core.model.service.api;

import java.util.Set;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Automatic role service
 * 
 * TODO: authorizable
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleTreeNodeService extends ReadWriteDtoService<IdmRoleTreeNodeDto, RoleTreeNodeFilter> {
	
	/**
	 * Returns all automatic role for given work position. 
	 * 
	 * @param workPosition
	 * @return
	 */
	Set<IdmRoleTreeNodeDto> getAutomaticRolesByTreeNode(UUID workPosition);
	
	/**
	 * Assign automatic roles by standard role request.
	 * Start of process is defined by parameter startRequestInternal.
	 * 
	 * @param contract
	 * @param automaticRoles
	 * @param startRequestInternal
	 * @return
	 */
	IdmRoleRequestDto assignAutomaticRoles(IdmIdentityContractDto contract, Set<IdmRoleTreeNodeDto> automaticRoles, boolean startRequestInternal);
	
	/**
	 * Delete automatic roles by standard role request.
	 * Start of process is defined by parameter startRequestInternal.
	 * 
	 * @param identityRole
	 * @param automaticRoles
	 * @param startRequestInternal
	 * @return
	 */
	IdmRoleRequestDto removeAutomaticRoles(IdmIdentityRoleDto identityRole, Set<IdmRoleTreeNodeDto> automaticRoles, boolean startRequestInternal);
}
