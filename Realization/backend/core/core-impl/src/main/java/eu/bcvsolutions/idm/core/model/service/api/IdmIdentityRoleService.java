package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityRoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with identity roles
 * 
 * @author svanda
 * @author Radek Tomiška
 *
 */
public interface IdmIdentityRoleService extends 
		ReadWriteDtoService<IdmIdentityRoleDto, IdmIdentityRole, IdentityRoleFilter>,
		AuthorizableService<IdmIdentityRoleDto, ContractGuaranteeFilter> {
	
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
	 * @return
	 */
	Page<IdmIdentityRoleDto> findExpiredRoles(LocalDate expirationDate, Pageable page);
}
