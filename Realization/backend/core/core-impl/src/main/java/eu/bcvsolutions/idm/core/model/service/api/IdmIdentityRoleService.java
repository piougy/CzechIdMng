package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with identity roles
 * 
 * @author svanda
 * @author Radek Tomiška
 *
 */
public interface IdmIdentityRoleService extends
	EventableDtoService<IdmIdentityRoleDto, IdentityRoleFilter>,
	AuthorizableService<IdmIdentityRoleDto>,
	ScriptEnabled {
	
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
}
