package eu.bcvsolutions.idm.core.api.audit.service;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventPropertyDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmLoggingEventPropertyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for {@link IdmLoggingEventPropertyDto}
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmLoggingEventPropertyService
		extends ReadDtoService<IdmLoggingEventPropertyDto, IdmLoggingEventPropertyFilter>,
		AuthorizableService<IdmLoggingEventPropertyDto> {

	/**
	 * Remove all properties by event id
	 * 
	 * @param eventId
	 */
	void deleteAllByEventId(Long eventId);
}
