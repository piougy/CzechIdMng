package eu.bcvsolutions.idm.core.eav.api.service;

import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * From attribute values
 *
 * @author Roman Kučera
 */
public interface IdmFormValueService extends
		ReadDtoService<IdmFormValueDto, IdmFormValueFilter>, AuthorizableService<IdmFormValueDto> {


}
