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

import com.google.common.collect.Lists;

/**
 * Controller tests
 * - TODO: test all filters
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
		UUID ownerId = UUID.randomUUID();
		//
		IdmEntityStateDto other = prepareDto();
		other.setOwnerId(ownerId);
		other.getResult().setCode(getHelper().createName());
		other = entityStateService.save(other);
		IdmEntityStateDto idmEntityStateDto = prepareDto();
		idmEntityStateDto.setOwnerId(ownerId);
		idmEntityStateDto.getResult().setCode(getHelper().createName());
		idmEntityStateDto = entityStateService.save(idmEntityStateDto);
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("ownerId", ownerId.toString());
		parameters.set("resultCode", idmEntityStateDto.getResult().getCode());
		//
		List<IdmEntityStateDto> results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(idmEntityStateDto.getId(), results.get(0).getId());
	}

	@Test
	public void testOperationStates(){
		UUID ownerId = UUID.randomUUID();
		//
		IdmEntityStateDto state = prepareDto();
		state.setOwnerId(ownerId);
		state.getResult().setState(OperationState.RUNNING);
		IdmEntityStateDto stateOne = entityStateService.save(state);
		//
		state = prepareDto();
		state.setOwnerId(ownerId);
		state.getResult().setState(OperationState.BLOCKED);
		IdmEntityStateDto stateTwo = entityStateService.save(state);
		//
		state = prepareDto();
		state.setOwnerId(ownerId);
		state.getResult().setState(OperationState.EXCEPTION);
		entityStateService.save(state); // other
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("ownerId", ownerId.toString());
		parameters.put("states", Lists.newArrayList(stateOne.getResult().getState().name(), stateTwo.getResult().getState().name()));
		//
		List<IdmEntityStateDto> results = find(parameters);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateTwo.getId())));
	}
}
