package eu.bcvsolutions.idm.core.rest.impl;

import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Controller tests
 * 
 * @author Radek Tomiška
 *
 */
public class IdmEntityStateControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmEntityStateDto> {

	@Autowired private IdmEntityStateController controller;
	@Autowired private IdmEntityStateService entityStateService;

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

	@Test
	public void testResultCode(){
		//init data
		List<IdmEntityStateDto> idmEntityStateDtos;
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

		IdmEntityStateDto idmEntityStateDto = prepareDto();
		idmEntityStateDto.getResult().setCode(getHelper().createName());
		idmEntityStateDto = entityStateService.save(idmEntityStateDto);

		//end init data
		//set filter
		parameters.set("resultCode", idmEntityStateDto.getResult().getCode());
		//end
		//test
		idmEntityStateDtos = find(parameters);
		Assert.assertEquals(1, idmEntityStateDtos.size());
		Assert.assertEquals(idmEntityStateDto.getId(), idmEntityStateDtos.get(0).getId());
		//end test
	}

	@Test
	public void testOperationStates(){
		//init data
		List<IdmEntityStateDto> idmEntityStateDtos;
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

		IdmEntityStateDto idmEntityStateDto = prepareDto();
		idmEntityStateDto.getResult().setState(OperationState.RUNNING);
		idmEntityStateDto = entityStateService.save(idmEntityStateDto);

		IdmEntityStateDto idmEntityStateDto2 = prepareDto();
		idmEntityStateDto2.getResult().setState(OperationState.BLOCKED);
		idmEntityStateDto2 = entityStateService.save(idmEntityStateDto2);

		//end init data
		//set filter
		parameters.add("operationStates", idmEntityStateDto.getResult().getState().name());
		parameters.add("operationStates", idmEntityStateDto2.getResult().getState().name());
		//end
		//test
		idmEntityStateDtos = find(parameters);
		Assert.assertTrue(2 <= idmEntityStateDtos.size());
		//end test
	}
}
