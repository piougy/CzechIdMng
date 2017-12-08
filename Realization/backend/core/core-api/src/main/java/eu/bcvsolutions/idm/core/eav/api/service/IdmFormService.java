package eu.bcvsolutions.idm.core.eav.api.service;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * CRUD for Common eav forms. Use {@link CommonsFormService} instead.
 * 
 * @see CommonsFormService
 * 
 * @author Radek Tomiška
 * @since 7.6.0
 */
public interface IdmFormService extends 
		ReadWriteDtoService<IdmFormDto, IdmFormFilter>,
		AuthorizableService<IdmFormDto> {	
}
