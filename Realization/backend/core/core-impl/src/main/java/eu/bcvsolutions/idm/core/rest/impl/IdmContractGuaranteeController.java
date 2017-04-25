package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.model.service.api.IdmContractGuaranteeService;

/**
 * Automatic role controller
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/contract-guarantees")
public class IdmContractGuaranteeController extends DefaultReadWriteDtoController<IdmContractGuaranteeDto, ContractGuaranteeFilter> {
	
	@Autowired
	public IdmContractGuaranteeController(IdmContractGuaranteeService service) {
		super(service);
	}
}
