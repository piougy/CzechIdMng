package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Identity controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmIdentityContractControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmIdentityContractDto> {

	@Autowired private IdmIdentityContractController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmIdentityContractDto, ?> getController() {
		return controller;
	}
	
	@Override
	protected boolean supportsPatch() {
		return false;
	}

	@Override
	protected IdmIdentityContractDto prepareDto() {
		IdmIdentityContractDto dto = new IdmIdentityContractDto();
		dto.setIdentity(getHelper().createIdentity().getId());
		return dto;
	}
}
