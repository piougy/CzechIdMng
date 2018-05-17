package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Identity controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmIdentityControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmIdentityDto> {

	@Autowired private IdmIdentityController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmIdentityDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmIdentityDto prepareDto() {
		IdmIdentityDto dto = new IdmIdentityDto();
		dto.setUsername(getHelper().createName());
		return dto;
	}
}
