package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceGuaranteeFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Identity's contract slice guarantee - manually defined manager (if no tree
 * structure is defined etc.)
 * 
 * @author svandav
 *
 */
public interface IdmContractSliceGuaranteeService extends 
		EventableDtoService<IdmContractSliceGuaranteeDto, IdmContractSliceGuaranteeFilter>,
		AuthorizableService<IdmContractSliceGuaranteeDto> {

}
