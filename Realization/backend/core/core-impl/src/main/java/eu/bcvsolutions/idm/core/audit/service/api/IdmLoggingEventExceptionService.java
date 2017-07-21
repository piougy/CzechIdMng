package eu.bcvsolutions.idm.core.audit.service.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
	 * Method find all {@link IdmLoggingEventExceptionDto} by event id.
	 * 
	 * @param id
	 * @return
	 */
	Page<IdmLoggingEventExceptionDto> findAllByEvent(Long id, Pageable pageable);
}
