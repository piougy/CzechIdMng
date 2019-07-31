package eu.bcvsolutions.idm.core.api.service.thin;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.thin.IdmIdentityRoleThinDto;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with identity roles - thin variant:
 * - supports get method only.
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 */
public interface IdmIdentityRoleThinService extends
	ReadDtoService<IdmIdentityRoleThinDto, IdmIdentityRoleFilter>,
	AuthorizableService<IdmIdentityRoleThinDto>,
	ScriptEnabled {
	
}
