package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFormAttributeFilter;
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

	
}
