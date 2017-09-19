package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import io.swagger.annotations.Api;

/**
 * Contract guarantee controller
 * 
 * TODO: Data is secured, but add @PreAuthorize annotations to methods (see IdmIdentityContractController)
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/contract-guarantees")
@Api(
		value = IdmContractGuaranteeController.TAG, 
		description = "Operations with identity contract guarantees", 
		tags = { IdmContractGuaranteeController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmContractGuaranteeController extends DefaultReadWriteDtoController<IdmContractGuaranteeDto, IdmContractGuaranteeFilter> {
	
	protected static final String TAG = "Contract guarantees";
	
	@Autowired
	public IdmContractGuaranteeController(IdmContractGuaranteeService service) {
		super(service);
	}
}
