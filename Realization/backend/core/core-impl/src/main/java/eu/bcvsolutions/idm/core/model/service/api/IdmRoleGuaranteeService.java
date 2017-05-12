package eu.bcvsolutions.idm.core.model.service.api;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Operations with identity roles
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleGuaranteeService extends
	ReadWriteDtoService<IdmRoleGuaranteeDto, EmptyFilter> {
}
