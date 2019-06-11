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
import org.mockito.Spy;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
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
	@Spy private ModelMapper modelMapper = new ModelMapper();
	@Spy private ObjectMapper mapper = new ObjectMapper();
	//
	@InjectMocks private DefaultEntityEventManager manager;
	
	@Test
	public void testCreatedEventsEmpty() {
		List<IdmEntityEventDto> events = new ArrayList<>();
		when(eventConfiguration.getBatchSize()).thenReturn(100);
		when(entityEventService
				.findToExecute(
						any(), 
						any(DateTime.class), 
						eq(PriorityType.HIGH),
						any(),
						any()))
				.thenReturn(new PageImpl<>(events));
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any(),
						any()))
				.thenReturn(new PageImpl<>(events));
		//
		Assert.assertTrue(manager.getCreatedEvents("instance").isEmpty());
	}
	
	@Test
	public void testCreatedEventsHighOnly() {
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.HIGH), 
						any(),
						any()))
				.thenReturn(createEvents(PriorityType.HIGH, 100));
		when(entityEventService
				.findToExecute(
						any(),						
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any(),
						any()))
				.thenReturn(createEvents(PriorityType.NORMAL, 0));
		when(eventConfiguration.getBatchSize()).thenReturn(100);
		
		List<IdmEntityEventDto> events = manager.getCreatedEvents("instance");
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
						any(),
						any()))
				.thenReturn(createEvents(PriorityType.HIGH, 0));
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any(),
						any()))
				.thenReturn(createEvents(PriorityType.NORMAL, 100));
		when(eventConfiguration.getBatchSize()).thenReturn(100);
		
		List<IdmEntityEventDto> events = manager.getCreatedEvents("instance");
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
						any(),
						any()))
				.thenReturn(createEvents(PriorityType.HIGH, 100));
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any(),
						any()))
				.thenReturn(createEvents(PriorityType.NORMAL, 30));
		when(eventConfiguration.getBatchSize()).thenReturn(100);
		
		List<IdmEntityEventDto> events = manager.getCreatedEvents("instance");
		//
		Assert.assertEquals(70, events.stream().filter(e -> e.getPriority() == PriorityType.HIGH).collect(Collectors.toList()).size());
		Assert.assertEquals(30, events.stream().filter(e -> e.getPriority() == PriorityType.NORMAL).collect(Collectors.toList()).size());
	}
	
	@Test
	public void testCreatedEventsLessHighThanAvailableSize() {
		when(eventConfiguration.getBatchSize()).thenReturn(100);
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.HIGH), 
						any(),
						any()))
				.thenReturn(createEvents(PriorityType.HIGH, 65));
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any(),
						any()))
				.thenReturn(createEvents(PriorityType.NORMAL, 50));
		
		List<IdmEntityEventDto> events = manager.getCreatedEvents("instance");
		//
		Assert.assertEquals(65, events.stream().filter(e -> e.getPriority() == PriorityType.HIGH).collect(Collectors.toList()).size());
		Assert.assertEquals(35, events.stream().filter(e -> e.getPriority() == PriorityType.NORMAL).collect(Collectors.toList()).size());
	}
	
	@Test
	public void testCreatedEventsLessHighThanAvailableSizeNotDividable() {
		when(eventConfiguration.getBatchSize()).thenReturn(16);
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.HIGH), 
						any(),
						any()))
				.thenReturn(createEvents(PriorityType.HIGH, 65));
		when(entityEventService
				.findToExecute(
						any(), 						
						any(DateTime.class), 
						eq(PriorityType.NORMAL), 
						any(),
						any()))
				.thenReturn(createEvents(PriorityType.NORMAL, 50));
		
		List<IdmEntityEventDto> events = manager.getCreatedEvents("instance");
		//
		Assert.assertEquals(11, events.stream().filter(e -> e.getPriority() == PriorityType.HIGH).collect(Collectors.toList()).size());
		Assert.assertEquals(5, events.stream().filter(e -> e.getPriority() == PriorityType.NORMAL).collect(Collectors.toList()).size());
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
						any(),
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
						any(),
						any()))
				.thenReturn(new PageImpl<>(normalEvents));
		when(eventConfiguration.getBatchSize()).thenReturn(100);
		//
		List<IdmEntityEventDto> events = manager.getCreatedEvents("instance");
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
						any(),
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
						any(),
						any()))
				.thenReturn(new PageImpl<>(normalEvents));
		//
		when(entityEventService
				.find(any(IdmEntityEventFilter.class), any(PageRequest.class)))
				.thenReturn(new PageImpl<>(new ArrayList<>()));
		when(eventConfiguration.getBatchSize()).thenReturn(100);
		//
		List<IdmEntityEventDto> events = manager.getCreatedEvents("instance");
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
						any(),
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
						any(),
						any()))
				.thenReturn(new PageImpl<>(normalEvents));
		when(entityEventService
				.find(any(IdmEntityEventFilter.class), any(PageRequest.class)))
				.thenReturn(new PageImpl<>(new ArrayList<>()));
		when(eventConfiguration.getBatchSize()).thenReturn(100);
		//
		List<IdmEntityEventDto> events = manager.getCreatedEvents("instance");
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
		Assert.assertTrue(manager.isDuplicate(highEventOne, highEventOne));
		Assert.assertTrue(manager.isDuplicate(highEventOne, highEventTwo));
		Assert.assertFalse(manager.isDuplicate(highEventOne, highEventThree));
		Assert.assertFalse(manager.isDuplicate(highEventTwo, highEventThree));
	}
	
	@Test
	public void testIsDuplicate() {
		DateTime created = new DateTime();
		UUID ownerId = UUID.randomUUID();
		//
		IdmEntityEventDto eventOne = new IdmEntityEventDto(UUID.randomUUID());
		eventOne.setCreated(created.minusMillis(11));
		eventOne.setPriority(PriorityType.HIGH);
		eventOne.setOwnerId(ownerId);	
		eventOne.setParentEventType("one");
		eventOne.setEventType("type");
		//
		IdmEntityEventDto eventTwo = new IdmEntityEventDto(UUID.randomUUID());
		eventTwo.setCreated(created.minusMillis(21));
		eventTwo.setPriority(PriorityType.HIGH);
		eventTwo.setOwnerId(ownerId);
		eventTwo.setEventType("type");
		eventTwo.setParentEventType("one");
		//
		Assert.assertTrue(manager.isDuplicate(eventOne, eventTwo));
		//
		eventTwo.setParentEventType("two");
		//
		Assert.assertFalse(manager.isDuplicate(eventOne, eventTwo));
		//
		eventTwo.setParentEventType("one");
		eventTwo.setEventType("type2");
		//
		Assert.assertFalse(manager.isDuplicate(eventOne, eventTwo));
		//
		eventTwo.setEventType("type");
		eventOne.getProperties().put("one", "one");
		//
		Assert.assertFalse(manager.isDuplicate(eventOne, eventTwo));
		//
		eventTwo.getProperties().put("one", "one");
		//
		Assert.assertTrue(manager.isDuplicate(eventOne, eventTwo));
		//
		eventTwo.getProperties().put("one", "one2");
		//
		Assert.assertFalse(manager.isDuplicate(eventOne, eventTwo));
		//
		eventTwo.getProperties().put("one", "one");
		//
		Assert.assertTrue(manager.isDuplicate(eventOne, eventTwo));
		//
		IdmIdentityDto originalSourceOne = new IdmIdentityDto(UUID.randomUUID());
		eventOne.setOriginalSource(originalSourceOne);
		//
		Assert.assertFalse(manager.isDuplicate(eventOne, eventTwo));
		//
		eventOne.setOriginalSource(null);
		eventTwo.setOriginalSource(originalSourceOne);
		//
		Assert.assertFalse(manager.isDuplicate(eventOne, eventTwo));
		Assert.assertFalse(manager.isDuplicate(eventTwo, eventOne));
		//
		eventOne.setOriginalSource(new IdmIdentity(originalSourceOne.getId()));
		eventTwo.setOriginalSource(originalSourceOne);
		//
		Assert.assertFalse(manager.isDuplicate(eventOne, eventTwo));
		//
		eventOne.setOriginalSource(new IdmIdentity(originalSourceOne.getId()));
		eventTwo.setOriginalSource(new IdmIdentity(UUID.randomUUID()));
		//
		Assert.assertFalse(manager.isDuplicate(eventOne, eventTwo));
		//
		eventOne.setOriginalSource(new IdmIdentity(originalSourceOne.getId()));
		eventTwo.setOriginalSource(new IdmIdentity(originalSourceOne.getId()));
		//
		Assert.assertFalse(manager.isDuplicate(eventOne, eventTwo));
		//
		eventOne.setOriginalSource(originalSourceOne);
		eventTwo.setOriginalSource(new IdmIdentity(originalSourceOne.getId()));
		//
		Assert.assertFalse(manager.isDuplicate(eventOne, eventTwo));
		//
		eventOne.setOriginalSource(originalSourceOne);
		eventTwo.setOriginalSource(originalSourceOne);
		//
		Assert.assertTrue(manager.isDuplicate(eventOne, eventTwo));
		//
		IdmIdentityDto originalSourceTwo = new IdmIdentityDto(originalSourceOne.getId());
		eventTwo.setOriginalSource(originalSourceTwo);
		//
		Assert.assertTrue(manager.isDuplicate(eventOne, eventTwo));
		//
		IdmIdentityDto embedded =  new IdmIdentityDto(UUID.randomUUID());
		originalSourceOne.getEmbedded().put("embedded", embedded);
		//
		Assert.assertTrue(manager.isDuplicate(eventOne, eventTwo));
		Assert.assertEquals(embedded, originalSourceOne.getEmbedded().get("embedded"));
		//
		originalSourceTwo.setFirstName("hoho");
		//
		Assert.assertFalse(manager.isDuplicate(eventOne, eventTwo));
		//
		originalSourceOne.setFirstName("hoho");
		//
		Assert.assertTrue(manager.isDuplicate(eventOne, eventTwo));
		//
		// audit fields are ignored
		originalSourceOne.setModified(new DateTime());
		//
		Assert.assertTrue(manager.isDuplicate(eventOne, eventTwo));
		
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
						any(),
						any()))
				.thenReturn(new PageImpl<>(highEvents));
		//
		when(entityEventService
				.findToExecute(
						any(),	
						any(DateTime.class),
						eq(PriorityType.NORMAL),
						any(),
						any()))
				.thenReturn(new PageImpl<>(new ArrayList<>()));
		//
		when(entityEventService
				.find(any(IdmEntityEventFilter.class), any(PageRequest.class)))
				.thenReturn(new PageImpl<>(new ArrayList<>()));
		when(eventConfiguration.getBatchSize()).thenReturn(100);
		//
		List<IdmEntityEventDto> events = manager.getCreatedEvents("instance");
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
						any(),
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
						any(),
						any()))
				.thenReturn(new PageImpl<>(normalEvents));
		//
		when(entityEventService
				.find(any(IdmEntityEventFilter.class), any(PageRequest.class)))
				.thenReturn(new PageImpl<>(new ArrayList<>()));
		when(eventConfiguration.getBatchSize()).thenReturn(100);
		//
		List<IdmEntityEventDto> events = manager.getCreatedEvents("instance");
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
		Assert.assertNull(manager.evaluatePriority(null, Lists.newArrayList()));
		Assert.assertNull(manager.evaluatePriority(null, Lists.newArrayList(one, two, three)));
		//
		two.priority = PriorityType.NORMAL;
		Assert.assertEquals(PriorityType.NORMAL, manager.evaluatePriority(null, Lists.newArrayList(one, two, three)));
		//
		one.priority = PriorityType.HIGH;
		Assert.assertEquals(PriorityType.HIGH, manager.evaluatePriority(null, Lists.newArrayList(one, two, three)));
		//
		three.priority = PriorityType.IMMEDIATE;
		Assert.assertEquals(PriorityType.IMMEDIATE, manager.evaluatePriority(null, Lists.newArrayList(one, two, three)));
	}
	
	@Test
	public void testResurrectEventWithPersistedContent() {
		when(lookupService
				.getOwnerType(MockOwner.class))
				.thenReturn(MockOwner.class.getCanonicalName());
		//
		IdmEntityEventDto entityEvent = new IdmEntityEventDto(UUID.randomUUID());
		MockOwner mockOwner =  new MockOwner();
		entityEvent.setOwnerType(manager.getOwnerType(mockOwner.getClass()));
		entityEvent.setOwnerId((UUID) mockOwner.getId());
		entityEvent.setContent(mockOwner);
		entityEvent.setPriority(PriorityType.NORMAL);
		entityEvent.setExecuteDate(new DateTime());
		entityEvent.setEventType(CoreEventType.NOTIFY.name());
		entityEvent.getProperties().put("one", "one");
		entityEvent.setParentEventType(CoreEventType.UPDATE.name());
		//
		EntityEvent<?> event = manager.toEvent(entityEvent);
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
		EntityEvent<?> event = manager.toEvent(entityEvent);
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
		manager.toEvent(entityEvent);
	}
	
	@Test
	public void testResurrectEventWithTransactionId() {
		IdmIdentityDto mockOwner = new IdmIdentityDto(UUID.randomUUID());
		IdmEntityEventDto entityEvent = new IdmEntityEventDto(UUID.randomUUID());
		entityEvent.setOwnerType(IdmIdentity.class.getCanonicalName());
		entityEvent.setOwnerId((UUID) mockOwner.getId());
		entityEvent.setEventType(CoreEventType.NOTIFY.name());
		entityEvent.setTransactionId(UUID.randomUUID());
		//
		when(lookupService.lookupDto(IdmIdentity.class, mockOwner.getId())).thenReturn(mockOwner);
		//
		EntityEvent<?> event = manager.toEvent(entityEvent);
		//
		Assert.assertEquals(mockOwner, event.getContent());
		Assert.assertEquals(CoreEventType.NOTIFY.name(), event.getType().name());
		Assert.assertEquals(entityEvent.getTransactionId(), event.getTransactionId());
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
		IdmEntityEventDto entityEvent = manager.prepareEvent(identity, new CoreEvent<>(CoreEventType.CREATE, identity, props));
		//
		Assert.assertEquals("mockInstance", entityEvent.getInstanceId());
		Assert.assertEquals(executeDate, entityEvent.getExecuteDate());
		Assert.assertEquals(PriorityType.HIGH, entityEvent.getPriority());
	}
	
	@Test
	public void testPropagatePropertiesOnNotifyIntoChildren() {
		IdmIdentity identity = new IdmIdentity(UUID.randomUUID());
		when(lookupService
				.getOwnerId(any()))
				.thenReturn(identity.getId());
		//
		Map<String, Serializable> props = new HashMap<>();
		props.put("one", "valueOne");
		//
		IdmEntityEventDto entityEvent = manager.prepareEvent(identity, new CoreEvent<>(CoreEventType.CREATE, identity, props));
		//
		Assert.assertEquals(props.get("one"), entityEvent.getProperties().get("one"));
	}
	
	@Test
	public void testPropagatePropertiesFromParentEvent() {
		UUID eventId = UUID.randomUUID();
		EntityEvent<?> event = new CoreEvent<>(CoreEventType.CREATE, new IdmIdentityDto());
		event.setId(eventId);
		//
		EntityEvent<?> parentEvent = new CoreEvent<>(CoreEventType.CREATE, new IdmIdentityDto());
		parentEvent.setId(UUID.randomUUID());
		parentEvent.getProperties().put("one", "one");
		parentEvent.getProperties().put(EntityEvent.EVENT_PROPERTY_EXECUTE_DATE, new DateTime());
		//
		manager.propagateProperties(event, parentEvent);
		//
		Assert.assertEquals(eventId, event.getId());
		Assert.assertEquals("one", event.getProperties().get("one"));
		Assert.assertNull(event.getProperties().get(EntityEvent.EVENT_PROPERTY_EXECUTE_DATE));
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
