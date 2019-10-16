package eu.bcvsolutions.idm.core.api.service;

import java.util.Set;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Automatic role service
 * - automatic roles by tree structure
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleTreeNodeService extends 
		EventableDtoService<IdmRoleTreeNodeDto, IdmRoleTreeNodeFilter>,
		AuthorizableService<IdmRoleTreeNodeDto> {
	
	/**
	 * Returns all automatic role for given work position. 
	 * 
	 * @param workPosition
	 * @return
	 */
	Set<IdmRoleTreeNodeDto> getAutomaticRolesByTreeNode(UUID workPosition);
	
	/**
	 * Add automatic role to contract. This method doesn't use standard role request 
	 * and add {@link IdmIdentityRoleDto} directly.
	 * In this method skip check changed authorities by processor {@link IdentityRoleAddAuthoritiesProcessor}.
	 
	 * @param contract
	 * @param automaticRoles
	 * @deprecated @since 9.6.0 use {@link IdmRoleRequestService#executeConceptsImmediate(UUID, java.util.List)}
	 */
	@Deprecated
	void addAutomaticRoles(IdmIdentityContractDto contract, Set<IdmRoleTreeNodeDto> automaticRoles);
	
	/**
	 * Add automatic role to contract. This method doesn't use standard role request 
	 * and add {@link IdmIdentityRoleDto} directly.
	 * In this method skip check changed authorities by processor {@link IdentityRoleAddAuthoritiesProcessor}.
	 * 
	 * @param contractPosition
	 * @param automaticRoles
	 * @deprecated @since 9.6.0 use {@link IdmRoleRequestService#executeConceptsImmediate(UUID, java.util.List)}
	 */
	@Deprecated
	void addAutomaticRoles(IdmContractPositionDto contractPosition, Set<IdmRoleTreeNodeDto> automaticRoles);
	
	/**
	 * Remove identity role (must be automatic role). This method doesn't use standard role request 
	 * and remove {@link IdmIdentityRoleDto} directly.
	 * In this method skip check changed authorities by processor {@link IdentityRoleDeleteAuthoritiesProcessor}.
	 * 
	 * @param identityRole
	 * @param automaticRoles
	 * @deprecated @since 9.6.0 use {@link IdmRoleRequestService#executeConceptsImmediate(UUID, java.util.List)}
	 */
	@Deprecated
	void removeAutomaticRoles(IdmIdentityRoleDto identityRole, Set<IdmRoleTreeNodeDto> automaticRoles);
}
