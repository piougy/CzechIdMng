package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmTreeNodeControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmTreeNodeDto> {

	@Autowired private IdmTreeNodeController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmTreeNodeDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmTreeNodeDto prepareDto() {
		IdmTreeNodeDto dto = new IdmTreeNodeDto();
		dto.setName(getHelper().createName());
		dto.setCode(getHelper().createName());
		dto.setTreeType(getHelper().getDefaultTreeType().getId());
		return dto;
	}
}
