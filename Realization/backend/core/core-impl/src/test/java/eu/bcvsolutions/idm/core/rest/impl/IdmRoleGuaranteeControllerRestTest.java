package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmRoleGuaranteeControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmRoleGuaranteeDto> {

	@Autowired private IdmRoleGuaranteeController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmRoleGuaranteeDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmRoleGuaranteeDto prepareDto() {
		IdmRoleGuaranteeDto dto = new IdmRoleGuaranteeDto();
		dto.setRole(getHelper().createRole().getId());
		dto.setGuarantee(getHelper().createIdentity((GuardedString) null).getId());
		return dto;
	}
}
