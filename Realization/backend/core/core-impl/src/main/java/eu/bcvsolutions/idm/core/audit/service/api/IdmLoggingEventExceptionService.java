package eu.bcvsolutions.idm.core.audit.service.api;

import eu.bcvsolutions.idm.core.api.dto.IdmLoggingEventExceptionDto;
import eu.bcvsolutions.idm.core.api.dto.filter.LoggingEventExceptionFilter;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for logging event exception
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmLoggingEventExceptionService
		extends ReadDtoService<IdmLoggingEventExceptionDto, LoggingEventExceptionFilter>,
		AuthorizableService<IdmLoggingEventExceptionDto> {

	/**
	 * Method delete all exceptions by given logging event id.
	 * 
	 * @param eventId
	 */
	void deleteByEventId(Long eventId);
}
