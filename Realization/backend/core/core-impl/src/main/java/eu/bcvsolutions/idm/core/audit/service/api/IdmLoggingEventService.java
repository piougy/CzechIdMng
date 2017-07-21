package eu.bcvsolutions.idm.core.audit.service.api;

import eu.bcvsolutions.idm.core.api.dto.IdmLoggingEventDto;
import eu.bcvsolutions.idm.core.api.dto.filter.LoggingEventFilter;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for logging events
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmLoggingEventService
		extends ReadDtoService<IdmLoggingEventDto, LoggingEventFilter>, AuthorizableService<IdmLoggingEventDto> {

}
