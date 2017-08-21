package eu.bcvsolutions.idm.core.audit.service.api;

import eu.bcvsolutions.idm.core.api.dto.IdmLoggingEventPropertyDto;
import eu.bcvsolutions.idm.core.api.dto.filter.LoggingEventPropertyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for {@link IdmLoggingEventPropertyDto}
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmLoggingEventPropertyService
		extends ReadDtoService<IdmLoggingEventPropertyDto, LoggingEventPropertyFilter>,
		AuthorizableService<IdmLoggingEventPropertyDto> {

	/**
	 * Remove all properties by event id
	 * 
	 * @param eventId
	 */
	void deleteAllByEventId(Long eventId);
}
