package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with identity roles
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleGuaranteeService extends
	ReadWriteDtoService<IdmRoleGuaranteeDto, IdmRoleGuaranteeFilter>,
	AuthorizableService<IdmRoleGuaranteeDto> {
}
