package eu.bcvsolutions.idm.core.model.service.api;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.RoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with identity roles
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleGuaranteeService extends
	ReadWriteDtoService<IdmRoleGuaranteeDto, RoleGuaranteeFilter>,
	AuthorizableService<IdmRoleGuaranteeDto> {
}
