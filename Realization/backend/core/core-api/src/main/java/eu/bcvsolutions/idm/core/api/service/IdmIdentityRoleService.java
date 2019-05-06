package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with identity roles
 * 
 * @author svanda
 * @author Radek Tomiška
 * @author Ondrej Kopr
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
	 * Returns all roles related to given {@link IdmContractPositionDto}
	 * 
	 * @param identityContractId
	 * @return
	 */
	List<IdmIdentityRoleDto> findAllByContractPosition(UUID contractPositionId);
	
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

	/**
	 * Get form instance for given identity role
	 * 
	 * @param dto
	 * @return
	 */
	IdmFormInstanceDto getRoleAttributeValues(IdmIdentityRoleDto dto);

	/**
	 * Validate form attributes for given identityRole
	 * 
	 * @param identityRole
	 * @return
	 */
	List<InvalidFormAttributeDto> validateFormAttributes(IdmIdentityRoleDto identityRole);

	/**
	 * Check if {@link IdmIdentityRoleDto} <b>ONE</b> is duplicit against {@link IdmIdentityRoleDto} <b>TWO</b>.</br></br>
	 * Method check these states:</br>
	 * - If {@link IdmIdentityRoleDto} has same {@link IdmRoleDto}</br>
	 * - If {@link IdmIdentityRoleDto} has same {@link IdmIdentityContractDto}</br>
	 * - If both roles are automatically added (in this case is return always false)</br>
	 * - If role <b>ONE</b> is duplicity with validity to role <b>TWO</b>. When are both roles manually added is also check if
	 * role <b>TWO</b> is duplicity with validity to role <b>ONE</b>
	 * - If {@link IdmIdentityRoleDto} has same definition and values (this can be skipped by parameter @param <b>skipSubdefinition</b>)</br>
	 * </br>
	 * <b>Beware,</b> for check subdefinition is needed that given identity role has filled <b>_eavs</b> attribute with form instance. Form
	 * definition with values is not get by database.
	 * 
	 * @param one
	 * @param two
	 * @param skipSubdefinition
	 * @return true if {@link IdmIdentityRoleDto} are same or similar. Otherwise false if {@link IdmIdentityRoleDto} are different
	 * @since 9.5.0
	 * @see <a href="https://wiki.czechidm.com/devel/documentation/roles/dev/identity-role-deduplication">Documentation link</a> for more information
	 */
	IdmIdentityRoleDto getDuplicated(IdmIdentityRoleDto one, IdmIdentityRoleDto two, Boolean skipSubdefinition);
}
