package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmTreeTypeControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmTreeTypeDto> {

	@Autowired private IdmTreeTypeController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmTreeTypeDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmTreeTypeDto prepareDto() {
		IdmTreeTypeDto dto = new IdmTreeTypeDto();
		dto.setName(getHelper().createName());
		dto.setCode(getHelper().createName());
		return dto;
	}
}
