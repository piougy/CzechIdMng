package eu.bcvsolutions.idm.core.model.service.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.AsyncEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.EventContentDeletedException;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.event.domain.MockOwner;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmEntityEventRepository;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Event manager unit tests
 * - event priority
 * - find events to execute
 * - resurrect event 
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultEntityEventManagerUnitTest extends AbstractUnitTest {

	@Mock private ApplicationContext context;
	@Mock private ApplicationEventPublisher publisher;
	@Mock private EnabledEvaluator enabledEvaluator;
	@Mock private LookupService lookupService;
	@Mock private IdmEntityEventService entityEventService;
	@Mock private IdmEntityStateService entityStateService;
	@Mock private EventConfiguration eventConfiguration;
	@Mock private IdmEntityEventRepository entityEventRepository;
	//
	@InjectMocks private DefaultEntityEventManager eventManager;
	
	@Test
	public void testCreatedEventsEmpty() {
		List<IdmEntityEventDto> events = new ArrayList<>();
		when(entityEventService
				.findToExecute(
						any(), 
						any(DateTime.class), 
						eq(PriorityType.HIGH), 
						any()))
				.thenReturn(new PageImpl<>(events));
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any()))
				.thenReturn(new PageImpl<>(events));
		//
		Assert.assertTrue(eventManager.getCreatedEvents("instance").isEmpty());
	}
	
	@Test
	public void testCreatedEventsHighOnly() {
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.HIGH), 
						any()))
				.thenReturn(createEvents(PriorityType.HIGH, 100));
		when(entityEventService
				.findToExecute(
						any(),						
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any()))
				.thenReturn(createEvents(PriorityType.NORMAL, 0));
		
		List<IdmEntityEventDto> events = eventManager.getCreatedEvents("instance");
		//
		Assert.assertEquals(100, events.size());
		Assert.assertTrue(events.stream().allMatch(e -> e.getPriority() == PriorityType.HIGH));
	}
	
	@Test
	public void testCreatedEventsNormalOnly() {
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.HIGH), 
						any()))
				.thenReturn(createEvents(PriorityType.HIGH, 0));
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any()))
				.thenReturn(createEvents(PriorityType.NORMAL, 100));
		
		List<IdmEntityEventDto> events = eventManager.getCreatedEvents("instance");
		//
		Assert.assertEquals(100, events.size());
		Assert.assertTrue(events.stream().allMatch(e -> e.getPriority() == PriorityType.NORMAL));
	}
	
	@Test
	public void testCreatedEventsMoreHighThanAvailableSize() {
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.HIGH), 
						any()))
				.thenReturn(createEvents(PriorityType.HIGH, 100));
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any()))
				.thenReturn(createEvents(PriorityType.NORMAL, 30));
		
		List<IdmEntityEventDto> events = eventManager.getCreatedEvents("instance");
		//
		Assert.assertEquals(70, events.stream().filter(e -> e.getPriority() == PriorityType.HIGH).collect(Collectors.toList()).size());
		Assert.assertEquals(30, events.stream().filter(e -> e.getPriority() == PriorityType.NORMAL).collect(Collectors.toList()).size());
	}
	
	@Test
	public void testCreatedEventsLessHighThanAvailableSize() {
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.HIGH), 
						any()))
				.thenReturn(createEvents(PriorityType.HIGH, 65));
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any()))
				.thenReturn(createEvents(PriorityType.NORMAL, 50));
		
		List<IdmEntityEventDto> events = eventManager.getCreatedEvents("instance");
		//
		Assert.assertEquals(65, events.stream().filter(e -> e.getPriority() == PriorityType.HIGH).collect(Collectors.toList()).size());
		Assert.assertEquals(35, events.stream().filter(e -> e.getPriority() == PriorityType.NORMAL).collect(Collectors.toList()).size());
	}
	
	@Test
	public void testCreatedEventsSortByCreatedAndPriority() {
		List<IdmEntityEventDto> highEvents = new ArrayList<>();
		DateTime created = new DateTime();
		IdmEntityEventDto highEventOne = new IdmEntityEventDto(UUID.randomUUID());
		highEventOne.setCreated(created.minusMillis(11));
		highEventOne.setPriority(PriorityType.HIGH);
		highEventOne.setOwnerId(UUID.randomUUID());		
		highEvents.add(highEventOne);
		IdmEntityEventDto highEventTwo = new IdmEntityEventDto(UUID.randomUUID());
		highEventTwo.setCreated(created.minusMillis(21));
		highEventTwo.setPriority(PriorityType.HIGH);
		highEventTwo.setOwnerId(UUID.randomUUID());		
		highEvents.add(highEventTwo);
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.HIGH), 
						any()))
				.thenReturn(new PageImpl<>(highEvents));
		//
		List<IdmEntityEventDto> normalEvents = new ArrayList<>();
		IdmEntityEventDto normalEventOne = new IdmEntityEventDto(UUID.randomUUID());
		normalEventOne.setCreated(created.minusMillis(18));
		normalEventOne.setPriority(PriorityType.NORMAL);
		normalEventOne.setOwnerId(UUID.randomUUID());		
		normalEvents.add(normalEventOne);
		IdmEntityEventDto normalEventTwo = new IdmEntityEventDto(UUID.randomUUID());
		normalEventTwo.setCreated(created.minusMillis(40));
		normalEventTwo.setPriority(PriorityType.NORMAL);
		normalEventTwo.setOwnerId(UUID.randomUUID());		
		normalEvents.add(normalEventTwo);
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any()))
				.thenReturn(new PageImpl<>(normalEvents));
		//
		List<IdmEntityEventDto> events = eventManager.getCreatedEvents("instance");
		//
		// highEventTwo - highEventOne - normalEventTwo - normalEventOne
		Assert.assertEquals(4, events.size());
		Assert.assertEquals(highEventTwo.getId(), events.get(0).getId());
		Assert.assertEquals(highEventOne.getId(), events.get(1).getId());
		Assert.assertEquals(normalEventTwo.getId(), events.get(2).getId());
		Assert.assertEquals(normalEventOne.getId(), events.get(3).getId());
	}
	
	@Test
	public void testCreatedEventsHigherPriorityByDuplicate() {
		List<IdmEntityEventDto> highEvents = new ArrayList<>();
		DateTime created = new DateTime();
		UUID ownerId = UUID.randomUUID();
		IdmEntityEventDto highEventOne = new IdmEntityEventDto(UUID.randomUUID());
		highEventOne.setCreated(created.minusMillis(11));
		highEventOne.setPriority(PriorityType.HIGH);
		highEventOne.setOwnerId(UUID.randomUUID());
		highEvents.add(highEventOne);
		IdmEntityEventDto highEventTwo = new IdmEntityEventDto(UUID.randomUUID());
		highEventTwo.setCreated(created.minusMillis(21));
		highEventTwo.setPriority(PriorityType.HIGH);
		highEventTwo.setOwnerId(ownerId);		
		highEvents.add(highEventTwo);
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.HIGH), 
						any()))
				.thenReturn(new PageImpl<>(highEvents));
		//
		List<IdmEntityEventDto> normalEvents = new ArrayList<>();
		IdmEntityEventDto normalEventOne = new IdmEntityEventDto(UUID.randomUUID());
		normalEventOne.setCreated(created.minusMillis(18));
		normalEventOne.setPriority(PriorityType.NORMAL);
		normalEventOne.setOwnerId(UUID.randomUUID());		
		normalEvents.add(normalEventOne);
		IdmEntityEventDto normalEventTwo = new IdmEntityEventDto(UUID.randomUUID());
		normalEventTwo.setCreated(created.minusMillis(4));
		normalEventTwo.setPriority(PriorityType.NORMAL);
		normalEventTwo.setOwnerId(ownerId);		
		normalEvents.add(normalEventTwo);
		when(entityEventService
				.findToExecute(
						any(),						
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any()))
				.thenReturn(new PageImpl<>(normalEvents));
		//
		when(entityEventService
				.find(any(IdmEntityEventFilter.class), any(PageRequest.class)))
				.thenReturn(new PageImpl<>(new ArrayList<>()));
		//
		List<IdmEntityEventDto> events = eventManager.getCreatedEvents("instance");
		//
		// normalEventTwo (high now) - highEventOne - normalEventOne
		Assert.assertEquals(3, events.size());
		Assert.assertEquals(PriorityType.HIGH, events.get(0).getPriority());
		Assert.assertEquals(normalEventTwo.getId(), events.get(0).getId());		
		Assert.assertEquals(highEventOne.getId(), events.get(1).getId());
		Assert.assertEquals(normalEventOne.getId(), events.get(2).getId());
	}
	
	@Test
	public void testCreatedEventsRemoveOlderDuplicates() {
		List<IdmEntityEventDto> highEvents = new ArrayList<>();
		DateTime created = new DateTime();
		UUID ownerId = UUID.randomUUID();
		IdmEntityEventDto highEventOne = new IdmEntityEventDto(UUID.randomUUID());
		highEventOne.setCreated(created.minusMillis(11));
		highEventOne.setPriority(PriorityType.HIGH);
		highEventOne.setOwnerId(UUID.randomUUID());		
		highEvents.add(highEventOne);
		IdmEntityEventDto highEventTwo = new IdmEntityEventDto(UUID.randomUUID());
		highEventTwo.setCreated(created.minusMillis(21));
		highEventTwo.setPriority(PriorityType.HIGH);
		highEventTwo.setOwnerId(ownerId);		
		highEvents.add(highEventTwo);
		IdmEntityEventDto highEventThree = new IdmEntityEventDto(UUID.randomUUID());
		highEventThree.setCreated(created.minusMillis(2));
		highEventThree.setPriority(PriorityType.HIGH);
		highEventThree.setOwnerId(ownerId);		
		highEvents.add(highEventThree);
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.HIGH), 
						any()))
				.thenReturn(new PageImpl<>(highEvents));
		//
		List<IdmEntityEventDto> normalEvents = new ArrayList<>();
		IdmEntityEventDto normalEventOne = new IdmEntityEventDto(UUID.randomUUID());
		normalEventOne.setCreated(created.minusMillis(18));
		normalEventOne.setPriority(PriorityType.NORMAL);
		normalEventOne.setOwnerId(UUID.randomUUID());		
		normalEvents.add(normalEventOne);
		IdmEntityEventDto normalEventTwo = new IdmEntityEventDto(UUID.randomUUID());
		normalEventTwo.setCreated(created.minusMillis(1));
		normalEventTwo.setPriority(PriorityType.NORMAL);
		normalEventTwo.setOwnerId(ownerId);		
		normalEvents.add(normalEventTwo);
		IdmEntityEventDto normalEventThree = new IdmEntityEventDto(UUID.randomUUID());
		normalEventThree.setCreated(created.minusMillis(2));
		normalEventThree.setPriority(PriorityType.NORMAL);
		normalEventThree.setOwnerId(ownerId);		
		normalEvents.add(normalEventThree);
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any()))
				.thenReturn(new PageImpl<>(normalEvents));
		when(entityEventService
				.find(any(IdmEntityEventFilter.class), any(PageRequest.class)))
				.thenReturn(new PageImpl<>(new ArrayList<>()));
		//
		List<IdmEntityEventDto> events = eventManager.getCreatedEvents("instance");
		//
		// normalEventTwo (high now) - highEventOne - normalEventOne
		Assert.assertEquals(3, events.size());
		Assert.assertEquals(PriorityType.HIGH, events.get(0).getPriority());
		Assert.assertEquals(normalEventTwo.getId(), events.get(0).getId());		
		Assert.assertEquals(highEventOne.getId(), events.get(1).getId());
		Assert.assertEquals(normalEventOne.getId(), events.get(2).getId());
	}
	
	@Test
	public void testCreatedEventsDontRemoveDuplicatesWithDifferentPerentEventType() {
		DateTime created = new DateTime();
		UUID ownerId = UUID.randomUUID();
		IdmEntityEventDto highEventOne = new IdmEntityEventDto(UUID.randomUUID());
		highEventOne.setCreated(created.minusMillis(11));
		highEventOne.setPriority(PriorityType.HIGH);
		highEventOne.setOwnerId(ownerId);	
		highEventOne.setParentEventType("one");
		//
		IdmEntityEventDto highEventTwo = new IdmEntityEventDto(UUID.randomUUID());
		highEventTwo.setCreated(created.minusMillis(21));
		highEventTwo.setPriority(PriorityType.HIGH);
		highEventTwo.setOwnerId(ownerId);		
		highEventTwo.setParentEventType("one");
		//
		IdmEntityEventDto highEventThree = new IdmEntityEventDto(UUID.randomUUID());
		highEventThree.setCreated(created.minusMillis(2));
		highEventThree.setPriority(PriorityType.HIGH);
		highEventThree.setOwnerId(ownerId);	
		highEventThree.setParentEventType("two");
		//
		Assert.assertTrue(eventManager.isDuplicate(highEventOne, highEventOne));
		Assert.assertTrue(eventManager.isDuplicate(highEventOne, highEventTwo));
		Assert.assertFalse(eventManager.isDuplicate(highEventOne, highEventThree));
		Assert.assertFalse(eventManager.isDuplicate(highEventTwo, highEventThree));
	}
	
	@Test
	public void testCreatedEventsRemoveDuplicatesByProps() {
		List<IdmEntityEventDto> highEvents = new ArrayList<>();
		DateTime created = new DateTime();
		UUID ownerId = UUID.randomUUID();
		IdmEntityEventDto highEventOne = new IdmEntityEventDto(UUID.randomUUID());
		highEventOne.setCreated(created.minusMillis(21));
		highEventOne.setPriority(PriorityType.HIGH);
		highEventOne.setOwnerId(ownerId);	
		highEventOne.getProperties().put("one", "one");
		highEvents.add(highEventOne);
		IdmEntityEventDto highEventTwo = new IdmEntityEventDto(UUID.randomUUID());
		highEventTwo.setCreated(created.minusMillis(11));
		highEventTwo.setPriority(PriorityType.HIGH);
		highEventTwo.setOwnerId(ownerId);
		highEventTwo.getProperties().put("one", "one");
		highEvents.add(highEventTwo);
		IdmEntityEventDto highEventThree = new IdmEntityEventDto(UUID.randomUUID());
		highEventThree.setCreated(created.minusMillis(2));
		highEventThree.setPriority(PriorityType.HIGH);
		highEventThree.setOwnerId(ownerId);		
		highEventThree.getProperties().put("one", "oneU");
		highEvents.add(highEventThree);
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.HIGH), 
						any()))
				.thenReturn(new PageImpl<>(highEvents));
		//
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any()))
				.thenReturn(new PageImpl<>(new ArrayList<>()));
		//
		when(entityEventService
				.find(any(IdmEntityEventFilter.class), any(PageRequest.class)))
				.thenReturn(new PageImpl<>(new ArrayList<>()));
		//
		List<IdmEntityEventDto> events = eventManager.getCreatedEvents("instance");
		Assert.assertEquals(1, events.size());
		Assert.assertTrue(events.stream().anyMatch(e -> e.getId().equals(highEventTwo.getId())));
		verify(entityEventService).delete(highEventOne);
	}
	
	@Test
	public void testCreatedEventsDistinctByOwner() {
		List<IdmEntityEventDto> highEvents = new ArrayList<>();
		DateTime created = new DateTime();
		UUID ownerId = UUID.randomUUID();
		IdmEntityEventDto highEventOne = new IdmEntityEventDto(UUID.randomUUID());
		highEventOne.setCreated(created.minusMillis(11));
		highEventOne.setPriority(PriorityType.HIGH);
		highEventOne.setOwnerId(ownerId);		
		highEvents.add(highEventOne);
		IdmEntityEventDto highEventTwo = new IdmEntityEventDto(UUID.randomUUID());
		highEventTwo.setCreated(created.minusMillis(21));
		highEventTwo.setPriority(PriorityType.HIGH);
		highEventTwo.setOwnerId(ownerId);		
		highEvents.add(highEventTwo);
		IdmEntityEventDto highEventThree = new IdmEntityEventDto(UUID.randomUUID());
		highEventThree.setCreated(created.minusMillis(2));
		highEventThree.setPriority(PriorityType.HIGH);
		highEventThree.setOwnerId(ownerId);		
		highEvents.add(highEventThree);
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.HIGH), 
						any()))
				.thenReturn(new PageImpl<>(highEvents));
		//
		List<IdmEntityEventDto> normalEvents = new ArrayList<>();
		IdmEntityEventDto normalEventOne = new IdmEntityEventDto(UUID.randomUUID());
		normalEventOne.setCreated(created.minusMillis(18));
		normalEventOne.setPriority(PriorityType.NORMAL);
		normalEventOne.setOwnerId(ownerId);		
		normalEvents.add(normalEventOne);
		IdmEntityEventDto normalEventTwo = new IdmEntityEventDto(UUID.randomUUID());
		normalEventTwo.setCreated(created.minusMillis(1));
		normalEventTwo.setPriority(PriorityType.NORMAL);
		normalEventTwo.setOwnerId(ownerId);		
		normalEvents.add(normalEventTwo);
		IdmEntityEventDto normalEventThree = new IdmEntityEventDto(UUID.randomUUID());
		normalEventThree.setCreated(created.minusMillis(3));
		normalEventThree.setPriority(PriorityType.NORMAL);
		normalEventThree.setOwnerId(ownerId);		
		normalEvents.add(normalEventThree);
		when(entityEventService
				.findToExecute(
						any(), 
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any()))
				.thenReturn(new PageImpl<>(normalEvents));
		//
		when(entityEventService
				.find(any(IdmEntityEventFilter.class), any(PageRequest.class)))
				.thenReturn(new PageImpl<>(new ArrayList<>()));
		//
		List<IdmEntityEventDto> events = eventManager.getCreatedEvents("instance");
		//
		// normalEventTwo (high now)
		Assert.assertEquals(1, events.size());
		Assert.assertEquals(PriorityType.HIGH, events.get(0).getPriority());
		Assert.assertEquals(normalEventTwo.getId(), events.get(0).getId());		
	}
	
	@Test
	public void testVoteAboutEventPriority() {
		MockAsyncProcessor one = new MockAsyncProcessor(null);
		MockAsyncProcessor two = new MockAsyncProcessor(null);
		MockAsyncProcessor three = new MockAsyncProcessor(null);
		//
		Assert.assertNull(eventManager.evaluatePriority(null, Lists.newArrayList()));
		Assert.assertNull(eventManager.evaluatePriority(null, Lists.newArrayList(one, two, three)));
		//
		two.priority = PriorityType.NORMAL;
		Assert.assertEquals(PriorityType.NORMAL, eventManager.evaluatePriority(null, Lists.newArrayList(one, two, three)));
		//
		one.priority = PriorityType.HIGH;
		Assert.assertEquals(PriorityType.HIGH, eventManager.evaluatePriority(null, Lists.newArrayList(one, two, three)));
		//
		three.priority = PriorityType.IMMEDIATE;
		Assert.assertEquals(PriorityType.IMMEDIATE, eventManager.evaluatePriority(null, Lists.newArrayList(one, two, three)));
	}
	
	@Test
	public void testResurrectEventWithPersistedContent() {
		when(lookupService
				.getOwnerType(MockOwner.class))
				.thenReturn(MockOwner.class.getCanonicalName());
		//
		IdmEntityEventDto entityEvent = new IdmEntityEventDto(UUID.randomUUID());
		MockOwner mockOwner =  new MockOwner();
		entityEvent.setOwnerType(eventManager.getOwnerType(mockOwner.getClass()));
		entityEvent.setOwnerId((UUID) mockOwner.getId());
		entityEvent.setContent(mockOwner);
		entityEvent.setPriority(PriorityType.NORMAL);
		entityEvent.setExecuteDate(new DateTime());
		entityEvent.setEventType(CoreEventType.NOTIFY.name());
		entityEvent.getProperties().put("one", "one");
		entityEvent.setParentEventType(CoreEventType.UPDATE.name());
		//
		EntityEvent<?> event = eventManager.toEvent(entityEvent);
		//
		Assert.assertEquals(mockOwner, event.getContent());
		Assert.assertEquals(CoreEventType.NOTIFY.name(), event.getType().name());
		Assert.assertEquals(entityEvent.getId(), event.getId());
		Assert.assertEquals(entityEvent.getPriority(), event.getPriority());
		Assert.assertEquals(entityEvent.getExecuteDate(), event.getExecuteDate());
		Assert.assertEquals(CoreEventType.UPDATE.name(), event.getParentType());
		Assert.assertEquals("one", event.getProperties().get("one"));
	}
	
	@Test
	public void testResurrectEventWithLoadedContent() {
		IdmIdentityDto mockOwner = new IdmIdentityDto(UUID.randomUUID());
		IdmEntityEventDto entityEvent = new IdmEntityEventDto(UUID.randomUUID());
		entityEvent.setOwnerType(IdmIdentity.class.getCanonicalName());
		entityEvent.setOwnerId((UUID) mockOwner.getId());
		entityEvent.setEventType(CoreEventType.NOTIFY.name());
		//
		when(lookupService.lookupDto(IdmIdentity.class, mockOwner.getId())).thenReturn(mockOwner);
		//
		EntityEvent<?> event = eventManager.toEvent(entityEvent);
		//
		Assert.assertEquals(mockOwner, event.getContent());
		Assert.assertEquals(CoreEventType.NOTIFY.name(), event.getType().name());
	}
	
	@Test(expected = EventContentDeletedException.class)
	public void testResurrectEventWithDeletedContent() {
		IdmEntityEventDto entityEvent = new IdmEntityEventDto(UUID.randomUUID());
		entityEvent.setOwnerType(IdmIdentity.class.getCanonicalName());
		entityEvent.setOwnerId(UUID.randomUUID());
		entityEvent.setEventType(CoreEventType.NOTIFY.name());
		//
		when(lookupService.lookupDto(IdmIdentity.class, entityEvent.getOwnerId())).thenReturn(null);
		//
		eventManager.toEvent(entityEvent);
	}
	
	@Test
	public void testSetAdditionalPrioritiesForEvent() {
		when(eventConfiguration.getAsynchronousInstanceId()).thenReturn("mockInstance");
		//
		DateTime executeDate = new DateTime();
		IdmIdentity identity = new IdmIdentity(UUID.randomUUID());
		when(lookupService
				.getOwnerId(any()))
				.thenReturn(identity.getId());
		Map<String, Serializable> props = new HashMap<>();
		props.put(EntityEvent.EVENT_PROPERTY_EXECUTE_DATE, executeDate);
		props.put(EntityEvent.EVENT_PROPERTY_PRIORITY, PriorityType.HIGH);
		//
		IdmEntityEventDto entityEvent = eventManager.prepareEvent(identity, new CoreEvent<>(CoreEventType.CREATE, identity, props));
		//
		Assert.assertEquals("mockInstance", entityEvent.getInstanceId());
		Assert.assertEquals(executeDate, entityEvent.getExecuteDate());
		Assert.assertEquals(PriorityType.HIGH, entityEvent.getPriority());
	}
	
	private Page<IdmEntityEventDto> createEvents(PriorityType priority, int count) {
		List<IdmEntityEventDto> events = new ArrayList<>();
		DateTime created = new DateTime().minusMillis(count);
		for(int i = 0; i < count; i++) {
			IdmEntityEventDto event = new IdmEntityEventDto();
			event.setCreated(created.plusMillis(count));
			event.setPriority(priority);
			event.setOwnerId(UUID.randomUUID());
			event.setEventType("custom");
			events.add(event);
		}
		//
		return new PageImpl<>(events);
	}
	
	private class MockAsyncProcessor 
			extends AbstractEntityEventProcessor<Serializable>
			implements AsyncEntityEventProcessor<Serializable> {

		PriorityType priority;
		
		public MockAsyncProcessor(PriorityType priority) {
			this.priority = priority;
		}
		
		@Override
		public EventResult<Serializable> process(EntityEvent<Serializable> event) {
			return null;
		}

		@Override
		public int getOrder() {
			return 0;
		}
		
		@Override
		public PriorityType getPriority(EntityEvent<Serializable> event) {
			return priority;
		}
		
	}
}
