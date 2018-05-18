package eu.bcvsolutions.idm.core.rest.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmEntityStateControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmEntityStateDto> {

	@Autowired private IdmEntityStateController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmEntityStateDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmEntityStateDto prepareDto() {
		IdmEntityStateDto dto = new IdmEntityStateDto();
		dto.setOwnerId(UUID.randomUUID());
		dto.setOwnerType("mock");
		dto.setInstanceId("mock");
		dto.setResult(new OperationResultDto(OperationState.CREATED));
		return dto;
	}
}
