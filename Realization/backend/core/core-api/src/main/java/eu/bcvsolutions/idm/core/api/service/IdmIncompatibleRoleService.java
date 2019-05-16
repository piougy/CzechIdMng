package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIncompatibleRoleFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Segregation of Duties
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public interface IdmIncompatibleRoleService extends
	EventableDtoService<IdmIncompatibleRoleDto, IdmIncompatibleRoleFilter>,
	AuthorizableService<IdmIncompatibleRoleDto>,
	ScriptEnabled {
	
	/**
	 * Returns all defined incompatible roles for the given role. Given role can be defined as superior or sub (both sides).
	 * 
	 * @param roleId
	 * @return
	 */
	List<IdmIncompatibleRoleDto> findAllByRole(UUID roleId);
	
	/**
	 * Returns all defined incompatible roles for the given role. Given role can be defined as superior or sub (both sides).
	 * 
	 * @param roleIds bulk role ids
	 * @return
	 * @since 9.7.0
	 */
	List<IdmIncompatibleRoleDto> findAllByRoles(List<UUID> roleIds);
	
	/**
	 * Load all defined incompatible roles for the given.
	 * Given roles can be defined as superior or sub for the incompatible role (both sides).
	 * 
	 * @param rolesOrIdentifiers role dtos or {@link Codeable} identifiers
	 * @return
	 */
	Set<ResolvedIncompatibleRoleDto> resolveIncompatibleRoles(List<Serializable> rolesOrIdentifiers);
}
