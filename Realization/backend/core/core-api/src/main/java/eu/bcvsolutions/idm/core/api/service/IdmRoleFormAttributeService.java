package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for relation between role and definition of form-attribution. Is elementary part
 * of role form "subdefinition".
 * 
 * @author Vít Švanda
 *
 */
public interface IdmRoleFormAttributeService extends
	EventableDtoService<IdmRoleFormAttributeDto, IdmRoleFormAttributeFilter>,
	AuthorizableService<IdmRoleFormAttributeDto> {

	/**
	 * Add given form attribute to sub-definition of given role. 
	 * Creates {@link IdmRoleFormAttributeDto} by given attribute (includes default value and validation settings). 
	 * @param role
	 * @param attribute
	 * @return
	 */
	IdmRoleFormAttributeDto addAttributeToSubdefintion(IdmRoleDto role, IdmFormAttributeDto attribute,  BasePermission... permission);

	
}
