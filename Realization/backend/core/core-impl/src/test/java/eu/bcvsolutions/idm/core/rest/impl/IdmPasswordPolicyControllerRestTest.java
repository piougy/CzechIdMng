package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - TODO: rewrite repository find to criteria ... use DataFilter
 * 
 * @author Radek Tomiška
 *
 */
public class IdmPasswordPolicyControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmPasswordPolicyDto> {

	@Autowired private IdmPasswordPolicyController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmPasswordPolicyDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmPasswordPolicyDto prepareDto() {
		IdmPasswordPolicyDto dto = new IdmPasswordPolicyDto();
		dto.setName(getHelper().createName());
		return dto;
	}
}
