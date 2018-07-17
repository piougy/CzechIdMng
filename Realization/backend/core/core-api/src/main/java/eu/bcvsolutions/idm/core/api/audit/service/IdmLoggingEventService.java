package eu.bcvsolutions.idm.core.api.audit.service;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventDto;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventExceptionDto;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventPropertyDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmLoggingEventFilter;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for logging events
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmLoggingEventService
		extends ReadDtoService<IdmLoggingEventDto, IdmLoggingEventFilter>, AuthorizableService<IdmLoggingEventDto> {

	/**
	 * Remove all logging event by id
	 * 
	 * @param eventId
	 */
	void deleteAllById(Long eventId);

	/**
	 * Remove all {@link IdmLoggingEventDto}, {@link IdmLoggingEventExceptionDto} and {@link IdmLoggingEventPropertyDto},
	 * where timestamp is lower or equal thank given. Method is used for quick clear logs.
	 *
	 * @param timestamp
	 * @return
	 */
	int deleteLowerOrEqualTimestamp(Long timestamp);
}
