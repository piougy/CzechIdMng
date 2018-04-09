package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with identity roles
 * 
 * @author svanda
 * @author Radek Tomiška
 *
 */
public interface IdmIdentityRoleService extends
	EventableDtoService<IdmIdentityRoleDto, IdmIdentityRoleFilter>,
	AuthorizableService<IdmIdentityRoleDto>,
	ScriptEnabled {
	
	String SKIP_CHECK_AUTHORITIES = "skipCheckAuthorities";
	
	/**
	 * Returns all identity's roles
	 * 
	 * @param identityId
	 * @return
	 */
	List<IdmIdentityRoleDto> findAllByIdentity(UUID identityId);
	
	/**
	 * Returns all roles related to given {@link IdmIdentityContractDto}
	 * 
	 * @param identityContractId
	 * @return
	 */
	List<IdmIdentityRoleDto> findAllByContract(UUID identityContractId);
	
	/**
	 * Returns assigned roles by given automatic role.
	 * 
	 * @param roleTreeNodeId	
	 * @return
	 */
	Page<IdmIdentityRoleDto> findByAutomaticRole(UUID roleTreeNodeId, Pageable pageable);
	
	/**
	 * Returns all roles with date lower than given expiration date.
	 * 
	 * @param expirationDate
	 * @param pageable
	 * @return
	 */
	Page<IdmIdentityRoleDto> findExpiredRoles(LocalDate expirationDate, Pageable pageable);

	/**
	 * Find valid identity-roles in this moment. Includes check on contract validity. 
	 * @param identityId
	 * @param pageable
	 * @return
	 * @deprecated @since 8.0.0 - use {@link #findValidRoles(UUID, Pageable)}
	 */
	@Deprecated
	Page<IdmIdentityRoleDto> findValidRole(UUID identityId, Pageable pageable);
	
	/**
	 * Find valid identity-roles in this moment. Includes check on contract validity. 
	 * 
	 * @param identityId
	 * @param pageable
	 * @return
	 */
	Page<IdmIdentityRoleDto> findValidRoles(UUID identityId, Pageable pageable);
}
