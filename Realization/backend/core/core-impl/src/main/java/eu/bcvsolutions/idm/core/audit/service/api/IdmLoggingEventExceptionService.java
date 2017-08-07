package eu.bcvsolutions.idm.core.audit.service.api;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.IdmLoggingEventExceptionDto;
import eu.bcvsolutions.idm.core.api.dto.filter.LoggingEventExceptionFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for logging event exception
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmLoggingEventExceptionService
		extends ReadWriteDtoService<IdmLoggingEventExceptionDto, LoggingEventExceptionFilter>,
		AuthorizableService<IdmLoggingEventExceptionDto> {

	/**
	 * Method find all {@link IdmLoggingEventExceptionDto} by event id.
	 * 
	 * @param id
	 * @return
	 */
	Page<IdmLoggingEventExceptionDto> findAllByEvent(Long id, Pageable pageable);
	
	/**
	 * Method delete all exceptions given in parameter eventExceptions.
	 * 
	 * @param eventExceptions
	 * @param permission
	 */
	void deleteAll(List<IdmLoggingEventExceptionDto> eventExceptions, BasePermission... permission);
}
