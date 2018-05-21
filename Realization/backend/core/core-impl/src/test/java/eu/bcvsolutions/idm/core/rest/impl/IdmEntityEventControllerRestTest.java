package eu.bcvsolutions.idm.core.rest.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
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
public class IdmEntityEventControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmEntityEventDto> {

	@Autowired private IdmEntityEventController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmEntityEventDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmEntityEventDto prepareDto() {
		IdmEntityEventDto dto = new IdmEntityEventDto();
		dto.setOwnerId(UUID.randomUUID());
		dto.setOwnerType("mock");
		dto.setInstanceId("mock");
		dto.setPriority(PriorityType.NORMAL);
		dto.setResult(new OperationResultDto(OperationState.CREATED));
		return dto;
	}
}
