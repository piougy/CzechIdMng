package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Controller tests
 * - all filters.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmEntityStateControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmEntityStateDto> {

	@Autowired private IdmEntityStateController controller;
	@Autowired private IdmEntityStateService entityStateService;
	@Autowired private IdmEntityEventService entityEventService;
	@Autowired private EntityEventManager entityEventManager;

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
	public void testFindByText() {
		UUID ownerOne = UUID.randomUUID();
		UUID ownerTwo = UUID.randomUUID();
		String ownerTypeOne = getHelper().createName();
		String ownerTypeTwo = ownerTypeOne + getHelper().createName();
		//
		IdmEntityStateDto state = prepareDto();
		state.setOwnerId(ownerOne);
		state.setOwnerType(ownerTypeOne);
		IdmEntityStateDto stateOne = entityStateService.save(state);
		//
		state = prepareDto();
		state.setOwnerId(ownerTwo);
		state.setOwnerType(ownerTypeTwo);
		state.getResult().setCode("mOck-code");
		IdmEntityStateDto stateTwo = entityStateService.save(state);
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("text", ownerTypeOne);
		List<IdmEntityStateDto> results = find(parameters);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateTwo.getId())));
		//
		parameters.set("text", ownerOne.toString().substring(0, 6));
		parameters.set("ownerType", ownerTypeOne);
		results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateOne.getId())));
		//
		parameters.set("text", "moCk-");
		parameters.set("ownerType", ownerTypeTwo);
		results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateTwo.getId())));
	}

	@Test
	public void testFindByResultCode() {
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
	public void testFindByOperationStates() {
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
	
	/**
	 * Find by owner and type
	 */
	@Test
	public void testFindByOwner() {
		UUID ownerOne = UUID.randomUUID();
		UUID ownerTwo = UUID.randomUUID();
		String ownerType = getHelper().createName();
		//
		IdmEntityStateDto state = prepareDto();
		state.setOwnerId(ownerOne);
		state.setOwnerType(ownerType);
		IdmEntityStateDto stateOne = entityStateService.save(state);
		//
		state = prepareDto();
		state.setOwnerId(ownerTwo);
		state.setOwnerType(ownerType);
		entityStateService.save(state); // other
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("ownerId", ownerOne.toString());
		parameters.set("ownerType", ownerType);
		//
		List<IdmEntityStateDto> results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateOne.getId())));
	}
	
	/**
	 * Find by owner as codeable
	 */
	@Test
	public void testFindByOwnerAsCodeable() {
		IdmIdentityDto ownerOne = getHelper().createIdentity((GuardedString) null);
		UUID ownerTwo = UUID.randomUUID();
		//
		IdmEntityStateDto state = prepareDto();
		state.setOwnerId(ownerOne.getId());
		state.setOwnerType(entityEventManager.getOwnerType(ownerOne));
		IdmEntityStateDto stateOne = entityStateService.save(state);
		//
		state = prepareDto();
		state.setOwnerId(ownerTwo);
		state.setOwnerType(entityEventManager.getOwnerType(ownerOne));
		entityStateService.save(state); // other
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("ownerId", ownerOne.getUsername());
		parameters.set("ownerType", entityEventManager.getOwnerType(ownerOne));
		//
		List<IdmEntityStateDto> results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateOne.getId())));
	}
	
	@Test
	public void testFindByWrongOwnerId() throws Exception {
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("ownerId", "wrong-without-owner-type");
		//
		getMockMvc().perform(get(getFindUrl(null))
        		.with(authentication(getAdminAuthentication()))
        		.params(parameters)
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testFindBySuperOwnerId() {
		UUID owner = UUID.randomUUID();
		UUID superOwnerOne = UUID.randomUUID();
		UUID superOwnerTwo = UUID.randomUUID();
		String ownerType = getHelper().createName();
		//
		IdmEntityStateDto state = prepareDto();
		state.setOwnerId(owner);
		state.setSuperOwnerId(superOwnerOne);
		state.setOwnerType(ownerType);
		IdmEntityStateDto stateOne = entityStateService.save(state);
		//
		state = prepareDto();
		state.setOwnerId(owner);
		state.setSuperOwnerId(superOwnerTwo);
		state.setOwnerType(ownerType);
		entityStateService.save(state); // other
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("superOwnerId", superOwnerOne.toString());
		parameters.set("ownerType", ownerType);
		//
		List<IdmEntityStateDto> results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateOne.getId())));
	}
	
	@Test
	public void testFindByEventId() {
		UUID owner = UUID.randomUUID();
		IdmEntityEventDto eventOne = createEvent();
		IdmEntityEventDto eventTwo = createEvent();
		String ownerType = getHelper().createName();
		//
		IdmEntityStateDto state = prepareDto();
		state.setOwnerId(owner);
		state.setEvent(eventOne.getId());
		state.setOwnerType(ownerType);
		IdmEntityStateDto stateOne = entityStateService.save(state);
		//
		state = prepareDto();
		state.setOwnerId(owner);
		state.setEvent(eventTwo.getId());
		state.setOwnerType(ownerType);
		entityStateService.save(state); // other
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("eventId", eventOne.getId().toString());
		parameters.set("ownerType", ownerType);
		//
		List<IdmEntityStateDto> results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateOne.getId())));
	}
	
	@Test
	public void testFindByCreated() {
		UUID ownerOne = UUID.randomUUID();
		UUID ownerTwo = UUID.randomUUID();
		String ownerType = getHelper().createName();
		//
		IdmEntityStateDto state = prepareDto();
		state.setOwnerId(ownerOne);
		state.setOwnerType(ownerType);
		IdmEntityStateDto stateOne = entityStateService.save(state);
		//
		getHelper().waitForResult(null, 2, 1);
		//
		state = prepareDto();
		state.setOwnerId(ownerTwo);
		state.setOwnerType(ownerType);
		IdmEntityStateDto stateTwo = entityStateService.save(state);
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("createdFrom", stateOne.getCreated().truncatedTo(ChronoUnit.MILLIS).toString());
		parameters.set("ownerType", ownerType);
		List<IdmEntityStateDto> results = find(parameters);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateTwo.getId())));
		// nanos are not persisted into db ...
		parameters.set("createdFrom", stateTwo.getCreated().truncatedTo(ChronoUnit.MILLIS).toString());
		results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateTwo.getId())));
		//
		parameters.remove("createdFrom");
		parameters.set("createdTill", stateTwo.getCreated().truncatedTo(ChronoUnit.MILLIS).plus(1, ChronoUnit.MILLIS).toString());
		results = find(parameters);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateTwo.getId())));
		//
		parameters.set("createdTill", stateOne.getCreated().truncatedTo(ChronoUnit.MILLIS).plus(1, ChronoUnit.MILLIS).toString());
		results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(stateOne.getId())));
	}
	
	private IdmEntityEventDto createEvent() {
		IdmEntityEventDto dto = new IdmEntityEventDto();
		dto.setOwnerId(UUID.randomUUID());
		dto.setOwnerType("mock");
		dto.setInstanceId("mock");
		dto.setPriority(PriorityType.NORMAL);
		dto.setResult(new OperationResultDto(OperationState.CREATED));
		//
		return entityEventService.save(dto);
	}
}
