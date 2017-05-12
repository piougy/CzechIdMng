package eu.bcvsolutions.idm.core.model.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Identity's contract guarantee - manually defined manager (if no tree
 * structure is defined etc.)
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmContractGuaranteeService extends 
		ReadWriteDtoService<IdmContractGuaranteeDto, ContractGuaranteeFilter>,
		AuthorizableService<IdmContractGuaranteeDto> {

}
