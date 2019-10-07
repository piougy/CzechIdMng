package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Controller tests
 * - all filters.
 * - delete all events.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmEntityEventControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmEntityEventDto> {

	@Autowired private IdmEntityEventController controller;
	@Autowired private IdmEntityEventService entityEventService;
	@Autowired private EntityEventManager entityEventManager;
	
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
	
	@Test
	public void testFindByText() {
		UUID ownerOne = UUID.randomUUID();
		UUID ownerTwo = UUID.randomUUID();
		String ownerTypeOne = getHelper().createName();
		String ownerTypeTwo = ownerTypeOne + getHelper().createName();
		//
		IdmEntityEventDto event = prepareDto();
		event.setOwnerId(ownerOne);
		event.setOwnerType(ownerTypeOne);
		IdmEntityEventDto eventOne = entityEventService.save(event);
		//
		event = prepareDto();
		event.setOwnerId(ownerTwo);
		event.setOwnerType(ownerTypeTwo);
		IdmEntityEventDto eventTwo = entityEventService.save(event);
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("text", ownerTypeOne);
		List<IdmEntityEventDto> results = find(parameters);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventTwo.getId())));
		//
		parameters.set("text", ownerOne.toString().substring(0, 6));
		parameters.set("ownerType", ownerTypeOne);
		results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventOne.getId())));
		//
		parameters.set("text", eventOne.getId().toString().substring(0, 6));
		parameters.set("ownerType", ownerTypeOne);
		results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventOne.getId())));
	}

	@Test
	public void testFindByResultCode() {
		UUID ownerId = UUID.randomUUID();
		//
		IdmEntityEventDto other = prepareDto();
		other.setOwnerId(ownerId);
		other.getResult().setCode(getHelper().createName());
		other = entityEventService.save(other);
		IdmEntityEventDto IdmEntityEventDto = prepareDto();
		IdmEntityEventDto.setOwnerId(ownerId);
		IdmEntityEventDto.getResult().setCode(getHelper().createName());
		IdmEntityEventDto = entityEventService.save(IdmEntityEventDto);
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("ownerId", ownerId.toString());
		parameters.set("resultCode", IdmEntityEventDto.getResult().getCode());
		//
		List<IdmEntityEventDto> results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(IdmEntityEventDto.getId(), results.get(0).getId());
	}

	@Test
	public void testFindByOperationStates() {
		UUID ownerId = UUID.randomUUID();
		//
		IdmEntityEventDto event = prepareDto();
		event.setOwnerId(ownerId);
		event.getResult().setState(OperationState.RUNNING);
		IdmEntityEventDto eventOne = entityEventService.save(event);
		//
		event = prepareDto();
		event.setOwnerId(ownerId);
		event.getResult().setState(OperationState.BLOCKED);
		IdmEntityEventDto eventTwo = entityEventService.save(event);
		//
		event = prepareDto();
		event.setOwnerId(ownerId);
		event.getResult().setState(OperationState.EXCEPTION);
		entityEventService.save(event); // other
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("ownerId", ownerId.toString());
		parameters.put("states", Lists.newArrayList(eventOne.getResult().getState().name(), eventTwo.getResult().getState().name()));
		//
		List<IdmEntityEventDto> results = find(parameters);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventTwo.getId())));
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
		IdmEntityEventDto event = prepareDto();
		event.setOwnerId(ownerOne);
		event.setOwnerType(ownerType);
		IdmEntityEventDto eventOne = entityEventService.save(event);
		//
		event = prepareDto();
		event.setOwnerId(ownerTwo);
		event.setOwnerType(ownerType);
		entityEventService.save(event); // other
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("ownerId", ownerOne.toString());
		parameters.set("ownerType", ownerType);
		//
		List<IdmEntityEventDto> results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventOne.getId())));
	}
	
	/**
	 * Find by owner as codeable
	 */
	@Test
	public void testFindByOwnerAsCodeable() {
		IdmIdentityDto ownerOne = getHelper().createIdentity((GuardedString) null);
		UUID ownerTwo = UUID.randomUUID();
		//
		IdmEntityEventDto event = prepareDto();
		event.setOwnerId(ownerOne.getId());
		event.setOwnerType(entityEventManager.getOwnerType(ownerOne));
		IdmEntityEventDto eventOne = entityEventService.save(event);
		//
		event = prepareDto();
		event.setOwnerId(ownerTwo);
		event.setOwnerType(entityEventManager.getOwnerType(ownerOne));
		entityEventService.save(event); // other
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("ownerId", ownerOne.getUsername());
		parameters.set("ownerType", entityEventManager.getOwnerType(ownerOne));
		//
		List<IdmEntityEventDto> results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventOne.getId())));
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
		IdmEntityEventDto event = prepareDto();
		event.setOwnerId(owner);
		event.setSuperOwnerId(superOwnerOne);
		event.setOwnerType(ownerType);
		IdmEntityEventDto eventOne = entityEventService.save(event);
		//
		event = prepareDto();
		event.setOwnerId(owner);
		event.setSuperOwnerId(superOwnerTwo);
		event.setOwnerType(ownerType);
		entityEventService.save(event); // other
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("superOwnerId", superOwnerOne.toString());
		parameters.set("ownerType", ownerType);
		//
		List<IdmEntityEventDto> results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventOne.getId())));
	}
	
	@Test
	public void testFindByCreated() {
		UUID ownerOne = UUID.randomUUID();
		UUID ownerTwo = UUID.randomUUID();
		String ownerType = getHelper().createName();
		//
		IdmEntityEventDto event = prepareDto();
		event.setOwnerId(ownerOne);
		event.setOwnerType(ownerType);
		IdmEntityEventDto eventOne = entityEventService.save(event);
		//
		getHelper().waitForResult(null, 1, 1);
		//
		event = prepareDto();
		event.setOwnerId(ownerTwo);
		event.setOwnerType(ownerType);
		IdmEntityEventDto eventTwo = entityEventService.save(event);
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("createdFrom", eventOne.getCreated().truncatedTo(ChronoUnit.MILLIS).toString());
		parameters.set("ownerType", ownerType);
		List<IdmEntityEventDto> results = find(parameters);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventTwo.getId())));
		//
		parameters.set("createdFrom", eventTwo.getCreated().truncatedTo(ChronoUnit.MILLIS).toString());
		results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventTwo.getId())));
		//
		parameters.remove("createdFrom");
		parameters.set("createdTill", eventTwo.getCreated().truncatedTo(ChronoUnit.MILLIS).plus(1, ChronoUnit.MILLIS).toString());
		results = find(parameters);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventTwo.getId())));
		//
		parameters.set("createdTill", eventOne.getCreated().truncatedTo(ChronoUnit.MILLIS).plus(1, ChronoUnit.MILLIS).toString());
		results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventOne.getId())));
	}
	
	@Test
	public void testFindByRootId() {
		UUID owner = UUID.randomUUID();
		UUID rootOne = UUID.randomUUID();
		UUID rootTwo = UUID.randomUUID();
		String ownerType = getHelper().createName();
		//
		IdmEntityEventDto event = prepareDto();
		event.setOwnerId(owner);
		event.setRootId(rootOne);
		event.setOwnerType(ownerType);
		IdmEntityEventDto eventOne = entityEventService.save(event);
		//
		event = prepareDto();
		event.setOwnerId(owner);
		event.setRootId(rootTwo);
		event.setOwnerType(ownerType);
		entityEventService.save(event); // other
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("rootId", rootOne.toString());
		parameters.set("ownerType", ownerType);
		//
		List<IdmEntityEventDto> results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventOne.getId())));
	}
	
	@Test
	public void testFindByParentId() {
		UUID owner = UUID.randomUUID();
		IdmEntityEventDto parentOne = createDto();
		IdmEntityEventDto parentTwo = createDto();
		String ownerType = getHelper().createName();
		//
		IdmEntityEventDto event = prepareDto();
		event.setOwnerId(owner);
		event.setParent(parentOne.getId());
		event.setOwnerType(ownerType);
		IdmEntityEventDto eventOne = entityEventService.save(event);
		//
		event = prepareDto();
		event.setOwnerId(owner);
		event.setParent(parentTwo.getId());
		event.setOwnerType(ownerType);
		entityEventService.save(event); // other
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("parentId", parentOne.getId().toString());
		parameters.set("ownerType", ownerType);
		//
		List<IdmEntityEventDto> results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventOne.getId())));
	}
	
	@Test
	public void testFindByEventType() {
		UUID owner = UUID.randomUUID();
		String eventTypeOne = "mock-one";
		String eventTypeTwo = "mock-two";
		String ownerType = getHelper().createName();
		//
		IdmEntityEventDto event = prepareDto();
		event.setOwnerId(owner);
		event.setEventType(eventTypeOne);
		event.setOwnerType(ownerType);
		IdmEntityEventDto eventOne = entityEventService.save(event);
		//
		event = prepareDto();
		event.setOwnerId(owner);
		event.setEventType(eventTypeTwo);
		event.setOwnerType(ownerType);
		entityEventService.save(event); // other
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("eventType", eventTypeOne);
		parameters.set("ownerType", ownerType);
		//
		List<IdmEntityEventDto> results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventOne.getId())));
	}
	
	@Test
	public void testFindByPriority() {
		UUID owner = UUID.randomUUID();
		PriorityType priorityOne = PriorityType.HIGH;
		PriorityType priorityTwo = PriorityType.NORMAL;
		String ownerType = getHelper().createName();
		//
		IdmEntityEventDto event = prepareDto();
		event.setOwnerId(owner);
		event.setPriority(priorityOne);
		event.setOwnerType(ownerType);
		IdmEntityEventDto eventOne = entityEventService.save(event);
		//
		event = prepareDto();
		event.setOwnerId(owner);
		event.setPriority(priorityTwo);
		event.setOwnerType(ownerType);
		entityEventService.save(event); // other
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.set("priority", priorityOne.name());
		parameters.set("ownerType", ownerType);
		//
		List<IdmEntityEventDto> results = find(parameters);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(eventOne.getId())));
	}
	
	@Test
	public void testDeleteAllEvents() throws Exception {
		createDto();
		createDto();
		//
		Assert.assertTrue(entityEventService.find(null, PageRequest.of(0, 1)).getTotalElements() > 1);
		//
		getMockMvc().perform(delete(getBaseUrl() + "/action/bulk/delete")
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isNoContent());
		//
		Assert.assertEquals(0, entityEventService.find(null, PageRequest.of(0, 1)).getTotalElements());
	}
}
