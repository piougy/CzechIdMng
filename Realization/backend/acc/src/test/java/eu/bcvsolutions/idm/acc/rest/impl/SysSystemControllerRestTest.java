package eu.bcvsolutions.idm.acc.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - CRUD
 * 
 * @author Radek Tomiška
 *
 */
public class SysSystemControllerRestTest extends AbstractReadWriteDtoControllerRestTest<SysSystemDto> {

	@Autowired private SysSystemController controller;
	
	@Override
	protected AbstractReadWriteDtoController<SysSystemDto, ?> getController() {
		return controller;
	}

	@Override
	protected SysSystemDto prepareDto() {
		SysSystemDto dto = new SysSystemDto();
		dto.setName(getHelper().createName());
		return dto;
	}
	
	@Override
	protected boolean supportsFormValues() {
		// TODO: connector eav are controlled
		return false;
	}
}
