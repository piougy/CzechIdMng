package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Identity's contract guarantee - manually defined manager (if no tree
 * structure is defined etc.)
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmContractGuaranteeService extends 
		ReadWriteDtoService<IdmContractGuaranteeDto, IdmContractGuaranteeFilter>,
		AuthorizableService<IdmContractGuaranteeDto> {

}
