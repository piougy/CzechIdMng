package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * 
 * @author Radek Tomiška
 *
 */
public class IdmRoleCompositionControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmRoleCompositionDto> {

	@Autowired private IdmRoleCompositionController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmRoleCompositionDto, ?> getController() {
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
	protected IdmRoleCompositionDto prepareDto() {
		IdmRoleCompositionDto dto = new IdmRoleCompositionDto();
		dto.setSuperior(getHelper().createRole().getId());
		dto.setSub(getHelper().createRole().getId());
		//
		return dto;
	}
}
