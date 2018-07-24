package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestItemFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for request's items
 * 
 * @author svandav
 * 
 */
public interface IdmRequestItemService
		extends ReadWriteDtoService<IdmRequestItemDto, IdmRequestItemFilter>,
		AuthorizableService<IdmRequestItemDto> {

}
