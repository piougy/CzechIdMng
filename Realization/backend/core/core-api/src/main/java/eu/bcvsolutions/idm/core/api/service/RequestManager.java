package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;

/**
 * Manager for automatic role request
 * 
 * @author svandav
 * 
 */
public interface RequestManager
		extends RequestService<IdmRequestDto> {

	Requestable post(Serializable requestId, Requestable dto);

	Requestable delete(Serializable requestId, Requestable dto);

	Requestable get(Serializable requestId, Requestable dto);

	IdmRequestDto createRequest(Requestable dto);

}
