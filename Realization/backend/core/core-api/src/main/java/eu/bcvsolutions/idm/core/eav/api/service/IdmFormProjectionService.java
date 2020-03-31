package eu.bcvsolutions.idm.core.eav.api.service;

import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.CodeableService;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormProjectionFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Form projection service.
 * 
 * @author Radek Tomiška
 * 10.2.0
 */
public interface IdmFormProjectionService extends 
		EventableDtoService<IdmFormProjectionDto, IdmFormProjectionFilter>,
		CodeableService<IdmFormProjectionDto>,
		AuthorizableService<IdmFormProjectionDto>,
		ScriptEnabled {
}
