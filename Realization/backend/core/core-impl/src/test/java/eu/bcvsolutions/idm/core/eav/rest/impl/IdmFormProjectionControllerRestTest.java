package eu.bcvsolutions.idm.core.eav.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;

/**
 * Controller tests
 * - CRUD
 * 
 * @author Radek Tomiška
 *
 */
public class IdmFormProjectionControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmFormProjectionDto> {

	@Autowired private IdmFormProjectionController controller;
	
	@Override
	protected boolean supportsFormValues() {
		return false;
	}
	
	@Override
	protected AbstractReadWriteDtoController<IdmFormProjectionDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmFormProjectionDto prepareDto() {
		IdmFormProjectionDto dto = new IdmFormProjectionDto();
		dto.setCode(getHelper().createName());
		dto.setOwnerType(getHelper().createName());
		//
		return dto;
	}
}
