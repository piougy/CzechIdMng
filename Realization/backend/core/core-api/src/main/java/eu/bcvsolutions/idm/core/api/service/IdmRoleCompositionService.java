package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCompositionFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with role composition
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
public interface IdmRoleCompositionService extends
	EventableDtoService<IdmRoleCompositionDto, IdmRoleCompositionFilter>,
	AuthorizableService<IdmRoleCompositionDto> {
	
	/**
	 * Return list of sub roles (only one level in depth)
	 * of role given by its role ID. Returning available sub roles by given permissions (AND).
	 * 
	 * @param superiorId
	 * @param permission permissions to evaluate (AND)
	 * @return
	 */
	List<IdmRoleCompositionDto> findDirectSubRoles(UUID superiorId, BasePermission... permission);
	
	/**
	 * Returns all superior roles for given sub role. Superior roles are sorted from sub to upper superior roles.
	 * 
	 * @param subId
	 * @param permission
	 * @return
	 */
	List<IdmRoleCompositionDto> findAllSuperiorRoles(UUID subId, BasePermission... permission);
	
	/**
	 * Assign (create) identity roles for given direct identity role sub roles.
	 * Sub roles will have the same metadata - contract, validFrom, validTill and direct identity role will be filled by given.
	 * {@link IdentityRoleEvent} can be constructed the same way as 
	 * 
	 * @param directRoleEvent event is needed for skip already processed identity roles (prevent cycles)
	 * @param permission permissions to evaluate (AND)
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	void assignSubRoles(EntityEvent<IdmIdentityRoleDto> event, BasePermission... permission);
	
	/**
	 * Remove identity roles assigned (created) by given direct identity role sub roles.
	 * 
	 * @param directRole
	 * @param permission permissions to evaluate (AND)
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	void removeSubRoles(EntityEvent<IdmIdentityRoleDto> event, BasePermission... permission);
	
	/**
	 * Updates metadata (contract, validFrom, validTill) on sub roles assigned (created) by given direct identity role
	 * 
	 * @param event
	 * @param permission permissions to evaluate (AND)
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	void updateSubRoles(EntityEvent<IdmIdentityRoleDto> event, BasePermission... permission);
	
}
