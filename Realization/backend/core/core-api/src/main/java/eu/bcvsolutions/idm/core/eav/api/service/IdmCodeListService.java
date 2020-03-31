package eu.bcvsolutions.idm.core.eav.api.service;

import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.CodeableService;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmCodeListFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * CRUD code lists. Use manager in your code instead.
 * 
 * @see CodeListManager
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public interface IdmCodeListService extends 
		EventableDtoService<IdmCodeListDto, IdmCodeListFilter>,
		CodeableService<IdmCodeListDto>,
		AuthorizableService<IdmCodeListDto>,
		ScriptEnabled {
}
