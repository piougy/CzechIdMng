package eu.bcvsolutions.idm.core.eav.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;

/**
 * Controller tests
 * - CRUD
 * 
 * @author Radek Tomiška
 *
 */
public class IdmCodeListControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmCodeListDto> {

	@Autowired private IdmCodeListController controller;
	
	@Override
	protected boolean supportsFormValues() {
		return false;
	}
	
	@Override
	protected AbstractReadWriteDtoController<IdmCodeListDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmCodeListDto prepareDto() {
		IdmCodeListDto dto = new IdmCodeListDto();
		dto.setName(getHelper().createName());
		dto.setCode(getHelper().createName());
		//
		return dto;
	}
}
