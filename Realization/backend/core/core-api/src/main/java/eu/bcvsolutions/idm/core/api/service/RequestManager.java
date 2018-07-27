package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

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

	Page<Requestable> find(Class<? extends Requestable> dtoClass, Serializable requestId, BaseFilter filter, Pageable pageable, IdmBasePermission permission);

}
