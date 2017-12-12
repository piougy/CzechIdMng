package eu.bcvsolutions.idm.core.api.service;

import java.util.Set;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
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
	 * Method resolve all automatic roles by attribute for given identity id
	 * 
	 * @param identityId
	 */
	void resolveAutomaticRolesByAttribute(UUID identityId);
	
}
