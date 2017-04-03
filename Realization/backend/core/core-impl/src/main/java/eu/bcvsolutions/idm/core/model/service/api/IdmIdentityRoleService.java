package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityRoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

/**
 * Operations with identity roles
 * 
 * @author svanda
 * @author Radek Tomiška
 *
 */
public interface IdmIdentityRoleService extends ReadWriteEntityService<IdmIdentityRole, IdentityRoleFilter> {
	
	/**
	 * Returns all identity's roles
	 * 
	 * @param identity
	 * @return
	 */
	List<IdmIdentityRole> getRoles(IdmIdentity identity);
	
	/**
	 * Returns all roles related to given {@link IdmIdentityContract}
	 * 
	 * @param identityContract
	 * @return
	 */
	List<IdmIdentityRole> getRoles(IdmIdentityContract identityContract);
	
	/**
	 * Returns assigned roles by given automatic role.
	 * 
	 * @param roleTreeNodeId	
	 * @return
	 */
	Page<IdmIdentityRole> getRolesByAutomaticRole(UUID roleTreeNodeId, Pageable pageable);
	
	/**
	 * Returns identity roles by their ids (uuid in string).
	 * 
	 * Used from wf.
	 * 
	 * @param ids
	 * @return
	 */
	List<IdmIdentityRole> getByIds(List<String> ids);

	/**
	 * Used from wf.
	 * 
	 * @param id
	 * @param dto
	 * @return
	 */
	IdmIdentityRole updateByDto(String id, IdmIdentityRoleDto dto);

	/**
	 * Used from wf.
	 * 
	 * @param dto
	 * @return
	 */
	IdmIdentityRole addByDto(IdmIdentityRoleDto dto);
	
	/**
	 * Check if {@link IdmIdentityRole} is valid from now. Use localDate
	 * @param identityRole
	 * @return
	 */
	boolean isIdentityRoleValidFromNow(IdmIdentityRole identityRole);
	
	/**
	 * Returns all roles with date lower than given expiration date.
	 * @return
	 */
	Page<IdmIdentityRole> findExpiredRoles(LocalDate expirationDate, Pageable page);
}
