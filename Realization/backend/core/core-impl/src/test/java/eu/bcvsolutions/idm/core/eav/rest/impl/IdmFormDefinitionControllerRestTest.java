package eu.bcvsolutions.idm.core.eav.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;

/**
 * Controller tests
 * - CRUD
 * 
 * @author Radek Tomiška
 *
 */
public class IdmFormDefinitionControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmFormDefinitionDto> {

	@Autowired private IdmFormDefinitionController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmFormDefinitionDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmFormDefinitionDto prepareDto() {
		IdmFormDefinitionDto dto = new IdmFormDefinitionDto();
		dto.setType(getHelper().createName());
		dto.setName(getHelper().createName());
		dto.setCode(getHelper().createName());
		//
		return dto;
	}
}
