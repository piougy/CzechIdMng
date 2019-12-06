package eu.bcvsolutions.idm.core.security.api.service;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Processing an exception in service
 * 
 * Some time we need to processing an exception for DTO. For example if is DTO
 * some request (keeps state and log) and this DTO is processed
 * asynchronously in events, then we need to log exception to this request DTO
 * too (change the state to exception basically).
 * 
 * @author Vít Švanda
 *
 * @param <DTO>
 */
public interface ExceptionProcessable<DTO extends AbstractDto> {

	/**
	 * Processing an exception for given DTO's ID.
	 * @param requestId
	 * @param ex
	 * @return
	 */
	DTO processException(UUID entityId, Exception ex);

}