package eu.bcvsolutions.idm.core.api.audit.service;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventExceptionDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmLoggingEventExceptionFilter;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for logging event exception
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmLoggingEventExceptionService
		extends ReadDtoService<IdmLoggingEventExceptionDto, IdmLoggingEventExceptionFilter>,
		AuthorizableService<IdmLoggingEventExceptionDto> {

	/**
	 * Method delete all exceptions by given logging event id.
	 * 
	 * @param eventId
	 */
	void deleteByEventId(Long eventId);
}
