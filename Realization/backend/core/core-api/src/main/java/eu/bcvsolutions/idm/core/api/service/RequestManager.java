package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
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

	IdmFormInstanceDto saveFormInstance(UUID requestId, Requestable owner, IdmFormDefinitionDto formDefinition,
			List<IdmFormValueDto> newValues, BasePermission... permission);

	IdmFormInstanceDto getFormInstance(UUID fromString, Requestable owner, IdmFormDefinitionDto formDefinition,
			BasePermission... permission);


}
