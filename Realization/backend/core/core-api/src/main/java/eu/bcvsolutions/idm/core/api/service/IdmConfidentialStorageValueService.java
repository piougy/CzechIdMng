package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmConfidentialStorageValueDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConfidentialStorageValueFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

public interface IdmConfidentialStorageValueService extends 
ReadDtoService<IdmConfidentialStorageValueDto, IdmConfidentialStorageValueFilter>,
AuthorizableService<IdmConfidentialStorageValueDto> {

}
