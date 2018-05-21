package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmContractGuaranteeControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmContractGuaranteeDto> {

	@Autowired private IdmContractGuaranteeController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmContractGuaranteeDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmContractGuaranteeDto prepareDto() {
		IdmContractGuaranteeDto dto = new IdmContractGuaranteeDto();
		dto.setIdentityContract(getHelper().getPrimeContract(getHelper().createIdentity().getId()).getId());
		dto.setGuarantee(getHelper().createIdentity().getId());
		return dto;
	}
}
