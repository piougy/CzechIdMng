package eu.bcvsolutions.idm.core.api.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Automatic role by attribute
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmAutomaticRoleAttributeService
		extends ReadWriteDtoService<IdmAutomaticRoleAttributeDto, IdmAutomaticRoleFilter>,
		AuthorizableService<IdmAutomaticRoleAttributeDto> {

	/**
	 * Prepare role request for delete automatic roles by standard role request.
	 * 
	 * @param identityRole
	 * @param automaticRoles
	 * @return
	 */
	IdmRoleRequestDto prepareRemoveAutomaticRoles(IdmIdentityRoleDto identityRole, Set<AbstractIdmAutomaticRoleDto> automaticRoles);
	
	/**
	 * Return all id's of {@link IdmIdentityDto} that passed or not passed (defined in parameter passed) by given automatic role by attribute
	 * and don't has already this automatic role (defined in parameter onlyNew).
	 * 
	 * @param automaticRoleId
	 * @param onlyNew
	 * @param passed
	 * @return
	 */
	Page<UUID> getIdentitiesForAutomaticRole(UUID automaticRoleId, boolean onlyNew, boolean passed, Pageable pageable);
	
	/**
	 * Return only new and passed identities for this automatic role. Call this role is similiar as call {@link IdmAutomaticRoleAttributeService#getIdentitiesForAutomaticRole}
	 * with both parameter set as true.
	 * 
	 * @param automaticRoleId
	 * @param pageable
	 * @return
	 */
	Page<UUID> getNewPassedIdentitiesForAutomaticRole(UUID automaticRoleId, Pageable pageable);
	
	/**
	 * Return only new and not passed identities for this automatic role. Call this role is similiar as call {@link IdmAutomaticRoleAttributeService#getIdentitiesForAutomaticRole}
	 * with both parameter set as false.
	 * 
	 * @param automaticRoleId
	 * @param pageable
	 * @return
	 */
	Page<UUID> getNewNotPassedIdentitiesForAutomaticRole(UUID automaticRoleId, Pageable pageable);
	
	/**
	 * Return all new passed automatic role by attribute for given identity id.
	 * All roles in concept are skipped.
	 * 
	 * @param identityId
	 * @return
	 */
	Set<AbstractIdmAutomaticRoleDto> getAllNewPassedAutomaticRoleForIdentity(UUID identityId);
	
	/**
	 * Return current not passed list of automatic roles for given identity id
	 * All roles in concept are skipped.
	 * 
	 * @param identityId
	 * @return
	 */
	Set<AbstractIdmAutomaticRoleDto> getAllNotPassedAutomaticRoleForIdentity(UUID identityId);
	
	/**
	 * Prepare add automatic role to contract. Return {@link IdmRoleRequestDto}
	 * 
	 * @param contract
	 * @param automaticRoles
	 * @return
	 */
	IdmRoleRequestDto prepareAddAutomaticRoles(IdmIdentityContractDto contract,
			Set<AbstractIdmAutomaticRoleDto> automaticRoles);
	
	/**
	 * Process new automatic roles for identity given in parameter.
	 * New automatic role in parameter passedAutomaticRoles will be add by request to identity (main contract)
	 * and not passed automatic role given in parameter notPassedAutomaticRoles will be removed.
	 * 
	 * @param identityId
	 * @param passedAutomaticRoles
	 * @param notPassedAutomaticRoles
	 */
	void processAutomaticRolesForIdentity(UUID identityId, Set<AbstractIdmAutomaticRoleDto> passedAutomaticRoles, Set<AbstractIdmAutomaticRoleDto> notPassedAutomaticRoles);
	
	/**
	 * Recalculate this automatic role and rules and assign new role to identity or remove.
	 * 
	 * @param automaticRoleId
	 * @return 
	 */
	IdmAutomaticRoleAttributeDto recalculate(UUID automaticRoleId);
}
