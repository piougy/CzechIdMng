package eu.bcvsolutions.idm.core.api.service;

import java.util.Set;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Automatic role service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleTreeNodeService extends 
		ReadWriteDtoService<IdmRoleTreeNodeDto, IdmRoleTreeNodeFilter>,
		AuthorizableService<IdmRoleTreeNodeDto> {
	
	/**
	 * Returns all automatic role for given work position. 
	 * 
	 * @param workPosition
	 * @return
	 */
	Set<IdmRoleTreeNodeDto> getAutomaticRolesByTreeNode(UUID workPosition);
	
	/**
	 * Assign automatic roles by standard role request.
	 * Beware after was method marked as deprecated, returns null instead role request
	 * 
	 * @param contract
	 * @param automaticRoles
	 * @return
	 * @deprecated now is added automatic role directly trough {@link IdmIdentityRoleDto}, please use {@link IdmRoleTreeNodeService#addAutomaticRoles(IdmIdentityContractDto, Set)}
	 */
	@Deprecated
	IdmRoleRequestDto prepareAssignAutomaticRoles(IdmIdentityContractDto contract, Set<IdmRoleTreeNodeDto> automaticRoles);
	
	/**
	 * Assign automatic roles with directly add - create identity role.
	 * Beware after was method marked as deprecated, returns null instead role request
	 * 
	 * @param contract
	 * @param automaticRoles
	 * @return
	 * @deprecated Role request was removed from automatic roles, please use {@link IdmRoleTreeNodeService#addAutomaticRoles(IdmIdentityContractDto, Set)}
	 */
	@Deprecated
	IdmRoleRequestDto assignAutomaticRoles(IdmIdentityContractDto contract, Set<IdmRoleTreeNodeDto> automaticRoles);
	
	/**
	 * Add automatic role to contract. This method doesn't use standard role request 
	 * and add {@link IdmIdentityRoleDto} directly.
	 * In this method skip check changed authorities by processor {@link IdentityRoleAddAuthoritiesProcessor}.
	 
	 * @param contract
	 * @param automaticRoles
	 */
	void addAutomaticRoles(IdmIdentityContractDto contract, Set<IdmRoleTreeNodeDto> automaticRoles);
	/**
	 * Remove automatic role directly by removing {@link IdmIdentityRoleDto}.
	 * Beware after was method marked as deprecated, returns null instead role request
	 * 
	 * @param identityRole
	 * @param automaticRoles
	 * @return
	 * @deprecated Role request was removed from automatic roles, please use {@link IdmRoleTreeNodeService#removeAutomaticRoles(IdmIdentityContractDto, Set)}
	 */
	@Deprecated
	IdmRoleRequestDto prepareRemoveAutomaticRoles(IdmIdentityRoleDto identityRole, Set<IdmRoleTreeNodeDto> automaticRoles);
	
	/**
	 * Remove identity role (must be automatic role). This method doesn't use standard role request 
	 * and remove {@link IdmIdentityRoleDto} directly.
	 * In this method skip check changed authorities by processor {@link IdentityRoleDeleteAuthoritiesProcessor}.
	 * 
	 * @param identityRole
	 * @param automaticRoles
	 */
	void removeAutomaticRoles(IdmIdentityRoleDto identityRole, Set<IdmRoleTreeNodeDto> automaticRoles);
}
