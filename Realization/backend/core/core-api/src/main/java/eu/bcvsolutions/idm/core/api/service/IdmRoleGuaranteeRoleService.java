package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeRoleFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with role guarantees - roles
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleGuaranteeRoleService extends
	EventableDtoService<IdmRoleGuaranteeRoleDto, IdmRoleGuaranteeRoleFilter>,
	AuthorizableService<IdmRoleGuaranteeRoleDto> {
}
