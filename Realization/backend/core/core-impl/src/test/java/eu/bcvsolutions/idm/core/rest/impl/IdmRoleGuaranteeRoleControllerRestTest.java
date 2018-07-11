package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeRoleDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmRoleGuaranteeRoleControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmRoleGuaranteeRoleDto> {

	@Autowired private IdmRoleGuaranteeRoleController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmRoleGuaranteeRoleDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmRoleGuaranteeRoleDto prepareDto() {
		IdmRoleGuaranteeRoleDto dto = new IdmRoleGuaranteeRoleDto();
		dto.setRole(getHelper().createRole().getId());
		dto.setGuaranteeRole(getHelper().createRole().getId());
		return dto;
	}
}
