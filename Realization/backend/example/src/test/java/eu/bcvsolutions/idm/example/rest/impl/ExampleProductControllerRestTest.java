package eu.bcvsolutions.idm.example.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.example.dto.ExampleProductDto;

/**
 * Controller tests
 * 
 * @author Radek Tomiška
 *
 */
public class ExampleProductControllerRestTest extends AbstractReadWriteDtoControllerRestTest<ExampleProductDto> {

	@Autowired private ExampleProductController controller;
	
	@Override
	protected AbstractReadWriteDtoController<ExampleProductDto, ?> getController() {
		return controller;
	}

	@Override
	protected ExampleProductDto prepareDto() {
		ExampleProductDto dto = new ExampleProductDto();
		dto.setCode(getHelper().createName());
		dto.setName(getHelper().createName());
		return dto;
	}

}
