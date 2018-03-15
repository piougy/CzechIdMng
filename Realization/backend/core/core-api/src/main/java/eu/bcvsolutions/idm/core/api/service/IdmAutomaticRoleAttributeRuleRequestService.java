package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleRequestFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for concept role request
 * @author svandav
 *
 */
public interface IdmAutomaticRoleAttributeRuleRequestService extends 
		ReadWriteDtoService<IdmAutomaticRoleAttributeRuleRequestDto, IdmAutomaticRoleAttributeRuleRequestFilter>,
		AuthorizableService<IdmAutomaticRoleAttributeRuleRequestDto>{
	
}
