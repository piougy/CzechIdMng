package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for universal request
 * 
 * @author svandav
 * 
 */
public interface IdmRequestService
		extends ReadWriteDtoService<IdmRequestDto, IdmRequestFilter>,
		AuthorizableService<IdmRequestDto> {

}
