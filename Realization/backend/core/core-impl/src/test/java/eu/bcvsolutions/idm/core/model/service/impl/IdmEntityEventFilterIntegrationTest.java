package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Test for event filters
 * 
 * @author Radek Tomiška
 *
 */
public class IdmEntityEventFilterIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private IdmEntityEventService service;
	
	@Test
	@Transactional
	public void testFindByOwnerId() {
		UUID ownerOneId = UUID.randomUUID();
		IdmEntityEventDto entityEventOne = new IdmEntityEventDto();
		entityEventOne.setOwnerType("mockType");
		entityEventOne.setEventType("mockEvent");
		entityEventOne.setOwnerId(ownerOneId);
		entityEventOne.setInstanceId("mockInstance");
		entityEventOne.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEventOne.setPriority(PriorityType.NORMAL);
		entityEventOne = service.save(entityEventOne);
		//
		UUID ownerTwoId = UUID.randomUUID();
		IdmEntityEventDto entityEventTwo = new IdmEntityEventDto();
		entityEventTwo.setOwnerType("mockType");
		entityEventTwo.setEventType("mockEvent");
		entityEventTwo.setOwnerId(ownerTwoId);
		entityEventTwo.setInstanceId("mockInstance");
		entityEventTwo.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEventTwo.setPriority(PriorityType.NORMAL);
		entityEventTwo = service.save(entityEventTwo);
		//
		IdmEntityEventFilter filter = new IdmEntityEventFilter();
		filter.setOwnerId(ownerOneId);
		//
		List<IdmEntityEventDto> events = service.find(filter, null).getContent();
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(entityEventOne.getId(), events.get(0).getId());
	}
	
	@Test
	@Transactional
	public void testFindByOwnerType() {
		UUID ownerOneId = UUID.randomUUID();
		IdmEntityEventDto entityEventOne = new IdmEntityEventDto();
		entityEventOne.setOwnerType("mockTypeOne");
		entityEventOne.setEventType("mockEvent");
		entityEventOne.setOwnerId(ownerOneId);
		entityEventOne.setInstanceId("mockInstance");
		entityEventOne.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEventOne.setPriority(PriorityType.NORMAL);
		entityEventOne = service.save(entityEventOne);
		//
		UUID ownerTwoId = UUID.randomUUID();
		IdmEntityEventDto entityEventTwo = new IdmEntityEventDto();
		entityEventTwo.setOwnerType("mockTypeTwo");
		entityEventTwo.setEventType("mockEvent");
		entityEventTwo.setOwnerId(ownerTwoId);
		entityEventTwo.setInstanceId("mockInstance");
		entityEventTwo.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEventTwo.setPriority(PriorityType.NORMAL);
		entityEventTwo = service.save(entityEventTwo);
		//
		IdmEntityEventFilter filter = new IdmEntityEventFilter();
		filter.setOwnerType("mockTypeOne");
		//
		List<IdmEntityEventDto> events = service.find(filter, null).getContent();
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(entityEventOne.getId(), events.get(0).getId());
	}
	
	@Test
	@Transactional
	public void testFindByCreated() {
		UUID ownerOneId = UUID.randomUUID();
		IdmEntityEventDto entityEventOne = new IdmEntityEventDto();
		entityEventOne.setOwnerType("mockType");
		entityEventOne.setEventType("mockEvent");
		entityEventOne.setOwnerId(ownerOneId);
		entityEventOne.setInstanceId("mockInstance");
		entityEventOne.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEventOne.setPriority(PriorityType.NORMAL);
		entityEventOne = service.save(entityEventOne);
		//
		helper.waitForResult(null, null, 1);
		//
		UUID ownerTwoId = UUID.randomUUID();
		IdmEntityEventDto entityEventTwo = new IdmEntityEventDto();
		entityEventTwo.setOwnerType("mockType");
		entityEventTwo.setEventType("mockEvent");
		entityEventTwo.setOwnerId(ownerTwoId);
		entityEventTwo.setInstanceId("mockInstance");
		entityEventTwo.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEventTwo.setPriority(PriorityType.NORMAL);
		entityEventTwo = service.save(entityEventTwo);
		//
		IdmEntityEventFilter filter = new IdmEntityEventFilter();
		filter.setCreatedFrom(entityEventTwo.getCreated());
		filter.setOwnerType("mockType");
		List<IdmEntityEventDto> events = service.find(filter, null).getContent();
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(entityEventTwo.getId(), events.get(0).getId());
		//
		filter.setCreatedFrom(null);
		filter.setCreatedTill(entityEventTwo.getCreated());
		events = service.find(filter, null).getContent();
		Assert.assertEquals(2, events.size());
		//
		filter.setCreatedTill(entityEventOne.getCreated());
		events = service.find(filter, null).getContent();
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(entityEventOne.getId(), events.get(0).getId());
	}
	
	@Test
	@Transactional
	public void testFindByStates() {
		UUID ownerId = UUID.randomUUID();
		IdmEntityEventDto entityEvent = new IdmEntityEventDto();
		entityEvent.setOwnerType("mockType");
		entityEvent.setEventType("mockEvent");
		entityEvent.setOwnerId(ownerId);
		entityEvent.setInstanceId("mockInstance");
		entityEvent.setResult(new OperationResultDto(OperationState.CANCELED));
		entityEvent.setPriority(PriorityType.NORMAL);
		entityEvent = service.save(entityEvent);
		//
		UUID ownerOneId = UUID.randomUUID();
		IdmEntityEventDto entityEventOne = new IdmEntityEventDto();
		entityEventOne.setOwnerType("mockType");
		entityEventOne.setEventType("mockEvent");
		entityEventOne.setOwnerId(ownerOneId);
		entityEventOne.setInstanceId("mockInstance");
		entityEventOne.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEventOne.setPriority(PriorityType.NORMAL);
		entityEventOne = service.save(entityEventOne);
		//
		UUID ownerTwoId = UUID.randomUUID();
		IdmEntityEventDto entityEventTwo = new IdmEntityEventDto();
		entityEventTwo.setOwnerType("mockType");
		entityEventTwo.setEventType("mockEvent");
		entityEventTwo.setOwnerId(ownerTwoId);
		entityEventTwo.setInstanceId("mockInstance");
		entityEventTwo.setResult(new OperationResultDto(OperationState.EXCEPTION));
		entityEventTwo.setPriority(PriorityType.NORMAL);
		entityEventTwo = service.save(entityEventTwo);
		//
		IdmEntityEventFilter filter = new IdmEntityEventFilter();
		filter.setOwnerType("mockType");
		filter.setStates(Lists.newArrayList(OperationState.BLOCKED));
		//
		List<IdmEntityEventDto> events = service.find(filter, null).getContent();
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(entityEventOne.getId(), events.get(0).getId());
		//
		filter.setStates(Lists.newArrayList(OperationState.EXCEPTION));
		//
		events = service.find(filter, null).getContent();
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(entityEventTwo.getId(), events.get(0).getId());
		//
		filter.setStates(Lists.newArrayList(OperationState.EXCEPTION, OperationState.BLOCKED));
		//
		events = service.find(filter, null).getContent();
		Assert.assertEquals(2, events.size());
		Assert.assertTrue(events.stream().anyMatch(e -> e.getOwnerId().equals(ownerOneId)));
		Assert.assertTrue(events.stream().anyMatch(e -> e.getOwnerId().equals(ownerTwoId)));
	}
	
	@Test
	@Transactional
	public void testFindByParentId() {
		UUID ownerId = UUID.randomUUID();
		IdmEntityEventDto entityEvent = new IdmEntityEventDto();
		entityEvent.setOwnerType("mockType");
		entityEvent.setEventType("mockEvent");
		entityEvent.setOwnerId(ownerId);
		entityEvent.setInstanceId("mockInstance");
		entityEvent.setResult(new OperationResultDto(OperationState.CANCELED));
		entityEvent.setPriority(PriorityType.NORMAL);
		entityEvent = service.save(entityEvent);
		//
		UUID ownerOneId = UUID.randomUUID();
		IdmEntityEventDto entityEventOne = new IdmEntityEventDto();
		entityEventOne.setOwnerType("mockType");
		entityEventOne.setEventType("mockEvent");
		entityEventOne.setOwnerId(ownerOneId);
		entityEventOne.setInstanceId("mockInstance");
		entityEventOne.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEventOne.setPriority(PriorityType.NORMAL);
		entityEventOne.setParent(entityEvent.getId());
		entityEventOne = service.save(entityEventOne);
		//
		UUID ownerTwoId = UUID.randomUUID();
		IdmEntityEventDto entityEventTwo = new IdmEntityEventDto();
		entityEventTwo.setOwnerType("mockType");
		entityEventTwo.setEventType("mockEvent");
		entityEventTwo.setOwnerId(ownerTwoId);
		entityEventTwo.setInstanceId("mockInstance");
		entityEventTwo.setResult(new OperationResultDto(OperationState.EXCEPTION));
		entityEventTwo.setPriority(PriorityType.NORMAL);
		entityEventTwo.setParent(entityEventOne.getId());
		entityEventTwo = service.save(entityEventTwo);
		//
		IdmEntityEventFilter filter = new IdmEntityEventFilter();
		filter.setOwnerType("mockType");
		filter.setParentId(entityEventTwo.getId());
		//
		List<IdmEntityEventDto> events = service.find(filter, null).getContent();
		Assert.assertEquals(0, events.size());
		//
		filter.setParentId(entityEvent.getId());
		events = service.find(filter, null).getContent();
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(entityEventOne.getId(), events.get(0).getId());
		//
		filter.setParentId(entityEventOne.getId());
		events = service.find(filter, null).getContent();
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(entityEventTwo.getId(), events.get(0).getId());
	}
	
	@Test
	@Transactional
	public void testFindByPriority() {
		UUID ownerOneId = UUID.randomUUID();
		IdmEntityEventDto entityEventOne = new IdmEntityEventDto();
		entityEventOne.setOwnerType("mockType");
		entityEventOne.setEventType("mockEvent");
		entityEventOne.setOwnerId(ownerOneId);
		entityEventOne.setInstanceId("mockInstance");
		entityEventOne.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEventOne.setPriority(PriorityType.NORMAL);
		entityEventOne = service.save(entityEventOne);
		//
		UUID ownerTwoId = UUID.randomUUID();
		IdmEntityEventDto entityEventTwo = new IdmEntityEventDto();
		entityEventTwo.setOwnerType("mockType");
		entityEventTwo.setEventType("mockEvent");
		entityEventTwo.setOwnerId(ownerTwoId);
		entityEventTwo.setInstanceId("mockInstance");
		entityEventTwo.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEventTwo.setPriority(PriorityType.HIGH);
		entityEventTwo = service.save(entityEventTwo);
		//
		IdmEntityEventFilter filter = new IdmEntityEventFilter();
		filter.setOwnerType("mockType");
		filter.setPriority(PriorityType.NORMAL);
		//
		List<IdmEntityEventDto> events = service.find(filter, null).getContent();
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(entityEventOne.getId(), events.get(0).getId());
		//
		filter.setPriority(PriorityType.HIGH);
		events = service.find(filter, null).getContent();
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(entityEventTwo.getId(), events.get(0).getId());
	}
}
