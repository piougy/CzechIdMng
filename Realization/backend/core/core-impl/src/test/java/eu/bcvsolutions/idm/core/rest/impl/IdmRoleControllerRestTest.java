package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmRoleControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmRoleDto> {

	@Autowired private IdmRoleController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmRoleDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmRoleDto prepareDto() {
		IdmRoleDto dto = new IdmRoleDto();
		dto.setName(getHelper().createName());
		return dto;
	}
}
