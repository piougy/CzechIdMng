package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * 
 * @author Radek Tomiška
 *
 */
public class IdmIncompatibleRoleControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmIncompatibleRoleDto> {

	@Autowired private IdmIncompatibleRoleController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmIncompatibleRoleDto, ?> getController() {
		return controller;
	}
	
	@Override
	protected boolean supportsPatch() {
		return false;
	}
	
	@Override
	protected boolean supportsPut() {
		return false;
	}

	@Override
	protected IdmIncompatibleRoleDto prepareDto() {
		IdmIncompatibleRoleDto dto = new IdmIncompatibleRoleDto();
		dto.setSuperior(getHelper().createRole().getId());
		dto.setSub(getHelper().createRole().getId());
		//
		return dto;
	}
}
