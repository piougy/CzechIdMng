package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.EntityEventProcessorDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EntityEventProcessorFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.DefaultEventContext;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.event.ConditionalContent;
import eu.bcvsolutions.idm.core.event.TestContent;
import eu.bcvsolutions.idm.core.event.TestContentTwo;
import eu.bcvsolutions.idm.core.event.TestEntityEventProcessorConfiguration;
import eu.bcvsolutions.idm.core.event.domain.MockOwner;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.event.EntityEventDeleteExecutedProcessor;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Entity events integration tests
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultEntityEventManagerIntergationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private ApplicationContext context;
	@Autowired private ApplicationEventPublisher publisher;
	@Autowired private EnabledEvaluator enabledEvaluator;
	@Autowired private LookupService lookupService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private ConfigurationService configurationService;
	@Autowired private IdmEntityEventService entityEventService;

	@Autowired
	@Qualifier("testTwoEntityEventProcessorOne")
	private EntityEventProcessor<?> testTwoEntityEventProcessorOne;
	//
	private DefaultEntityEventManager entityEventManager; 
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_USER_1);
		entityEventManager = new DefaultEntityEventManager(context, publisher, enabledEvaluator, lookupService);
	}
	
	@After 
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testFindProcessors() {
		EntityEventProcessorFilter filter = null;
		List<EntityEventProcessorDto> processors = entityEventManager.find(filter);
		int size = processors.size();
		//
		assertTrue(size > 11);
		//
		filter = new EntityEventProcessorFilter();
		filter.setContentClass(Serializable.class);
	    processors = entityEventManager.find(filter);
		//
	    assertEquals(size, processors.size());
	    //
	    filter.setContentClass(TestContent.class);
	    processors = entityEventManager.find(filter);
	    //
	    assertEquals(11, processors.size());
	}
	
	@Test
	public void testEventGreenLine() {
		EntityEvent<TestContent> event = new CoreEvent<>(CoreEventType.CREATE, new TestContent());
		EventContext<TestContent> context = entityEventManager.process(event);
		//
		assertEquals(4, context.getResults().size());
		assertEquals(4, context.getProcessedOrder().intValue());
		assertEquals("4", context.getLastResult().getEvent().getContent().getText());
	}
	
	@Test
	public void testCloseEvent() {
		EntityEvent<TestContent> event = new CoreEvent<>(CoreEventType.CREATE, new TestContent());
		event.getContent().setClose(2);
		EventContext<TestContent> context = entityEventManager.process(event);
		//
		assertEquals(2, context.getResults().size());
		assertEquals(2, context.getProcessedOrder().intValue());
		assertEquals("2", context.getLastResult().getEvent().getContent().getText());
	}
	
	@Test
	public void testSuspendEvent() {
		EntityEvent<TestContent> event = new CoreEvent<>(CoreEventType.CREATE, new TestContent());
		event.getContent().setSuspend(1);
		EventContext<TestContent> context = entityEventManager.process(event);
		//
		assertEquals(1, context.getResults().size());
		assertEquals(1, context.getProcessedOrder().intValue());
		assertEquals("1", context.getLastResult().getEvent().getContent().getText());
		//
		event.getContent().setSuspend(3);
		context = entityEventManager.process(event);
		//
		assertEquals(3, context.getResults().size());
		assertEquals(3, context.getProcessedOrder().intValue());
		assertEquals("3", context.getLastResult().getEvent().getContent().getText());
		//
		event.getContent().setSuspend(null);
		context = entityEventManager.process(event);
		//
		assertEquals(4, context.getResults().size());
		assertEquals(4, context.getProcessedOrder().intValue());
		assertEquals("4", context.getLastResult().getEvent().getContent().getText());
	}
	
	@Test
	public void testStartEventInMiddle() {
		DefaultEventContext<TestContent> initContext = new DefaultEventContext<>();
		initContext.setProcessedOrder(2);
		EntityEvent<TestContent> event = new CoreEvent<>(CoreEventType.CREATE, new TestContent(), null, initContext);
		EventContext<TestContent> context = entityEventManager.process(event);
		//
		assertEquals(2, context.getResults().size());
		assertEquals(4, context.getProcessedOrder().intValue());
		assertEquals("4", context.getLastResult().getEvent().getContent().getText());
	}
	
	@Test 
	public void testOriginalSource() {
		IdmIdentityDto createdIdentity = helper.createIdentity();
		// process change
		IdmIdentityDto updateIdentity = identityService.get(createdIdentity.getId());
		updateIdentity.setFirstName("newFirst");
		updateIdentity.setLastName("newLast");
		EntityEvent<IdmIdentityDto> event = new IdentityEvent(IdentityEventType.UPDATE, updateIdentity);
		EventContext<IdmIdentityDto> context = entityEventManager.process(event);
		IdmIdentityDto originalIdentity = context.getLastResult().getEvent().getOriginalSource();
		IdmIdentityDto savedIdentity = context.getLastResult().getEvent().getContent();
		// check
		assertEquals(createdIdentity.getUsername(), originalIdentity.getUsername());
		assertEquals(createdIdentity.getFirstName(), originalIdentity.getFirstName());
		assertEquals(createdIdentity.getLastName(), originalIdentity.getLastName());
		assertEquals(updateIdentity.getUsername(), savedIdentity.getUsername());
		assertEquals(updateIdentity.getFirstName(), savedIdentity.getFirstName());
		assertEquals(updateIdentity.getLastName(), savedIdentity.getLastName());
	}
	
	@Test 
	public void testProcessorSameOrder() {
		EntityEvent<TestContentTwo> event = new CoreEvent<>(CoreEventType.EAV_SAVE, new TestContentTwo());
		EventContext<TestContentTwo> context = entityEventManager.process(event);
		//
		assertEquals(2, context.getResults().size());
		assertEquals(2, context.getProcessedOrder().intValue());
	}

	@Test
	public void testConfigPropertyEventTypeOverwrite() {
		String eventTypeName = System.nanoTime() + "_test_type";
		EventType type = (EventType) () -> eventTypeName;
		EntityEvent<TestContentTwo> event = new CoreEvent<>(type, new TestContentTwo());
		EventContext<TestContentTwo> context = entityEventManager.process(event);
		assertEquals(0, context.getResults().size());

		String configPropName = testTwoEntityEventProcessorOne.getConfigurationPropertyName(EntityEventProcessor.PROPERTY_EVENT_TYPES);
		configurationService.setValue(configPropName, eventTypeName);

		EntityEvent<TestContentTwo> event2 = new CoreEvent<>(type, new TestContentTwo());
		EventContext<TestContentTwo> context2 = entityEventManager.process(event2);
		assertEquals(2, context2.getResults().size());
	}
	
	@Test
	public void testConditionalProcessor() {
		EntityEvent<ConditionalContent> event = new CoreEvent<>(CoreEventType.CREATE, new ConditionalContent(false));
		EventContext<ConditionalContent> context = entityEventManager.process(event);
		//
		assertEquals(0, context.getResults().size());
		//
		event = new CoreEvent<>(CoreEventType.CREATE, new ConditionalContent(true));
		context = entityEventManager.process(event);
		//
		assertEquals(1, context.getResults().size());
	}
	
	@Test
	public void testSameOrderBeansOrder() {
		EntityEvent<TestContent> event = new CoreEvent<>(TestEntityEventProcessorConfiguration.EVENT_TYPE_ORDER, new TestContent());
		EventContext<TestContent> context = entityEventManager.process(event);
		//
		// Look out: processors are executed in random order in configured order is same
		assertEquals(7, context.getResults().size());
	}
	
	@Test
	public void testMultiThreadEventProcessing() {
		List<IdmEntityEventDto> events = new ArrayList<>();
		try {
			helper.setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, false);
			helper.disable(EntityEventDeleteExecutedProcessor.class);
			int count = 250; // 15s 
			//
			// create events
			for (int i = 0; i < count; i++) {
				MockOwner mockOwner = new MockOwner();
				IdmEntityEventDto entityEvent = new IdmEntityEventDto();
				entityEvent.setOwnerType(mockOwner.getClass().getCanonicalName());
				entityEvent.setEventType("empty");
				entityEvent.setOwnerId((UUID) mockOwner.getId());
				entityEvent.setContent(mockOwner);
				entityEvent.setInstanceId(configurationService.getInstanceId());
				entityEvent.setResult(new OperationResultDto(OperationState.CREATED));
				entityEvent.setPriority(PriorityType.NORMAL);
				events.add(entityEventService.save(entityEvent));
			}
			//
			IdmEntityEventFilter filter = new IdmEntityEventFilter();
			filter.setOwnerType(MockOwner.class.getCanonicalName());
			filter.setStates(Lists.newArrayList(OperationState.CREATED));
			Assert.assertEquals(count, entityEventService.find(filter, new PageRequest(0, 1)).getTotalElements());
			//
			// execute
			helper.setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
			//
			// wait for executed events
			helper.waitForResult(res -> {
				return entityEventService.find(filter, new PageRequest(0, 1)).getTotalElements() != 0;
			}, 1000, Integer.MAX_VALUE);
			//
			// check what happened
			filter.setStates(Lists.newArrayList(OperationState.EXECUTED));
			Assert.assertEquals(count, entityEventService.find(filter, new PageRequest(0, 1)).getTotalElements());			
		} finally {
			events.forEach(e -> entityEventService.delete(e));
			helper.setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
			helper.enable(EntityEventDeleteExecutedProcessor.class);
		}
	}
	
	@Test
	public void testRemoveDuplicateEventsForTheSameOwner() {
		List<IdmEntityEventDto> events = new ArrayList<>();
		try {
			helper.setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, false);
			helper.disable(EntityEventDeleteExecutedProcessor.class);
			int count = 10;
			//
			// create events
			MockOwner mockOwner = new MockOwner();
			for (int i = 0; i < count; i++) {
				IdmEntityEventDto entityEvent = new IdmEntityEventDto();
				entityEvent.setOwnerType(mockOwner.getClass().getCanonicalName());
				entityEvent.setEventType("empty");
				entityEvent.setOwnerId((UUID) mockOwner.getId());
				entityEvent.setContent(mockOwner);
				entityEvent.setInstanceId(configurationService.getInstanceId());
				entityEvent.setResult(new OperationResultDto(OperationState.CREATED));
				entityEvent.setPriority(PriorityType.NORMAL);
				events.add(entityEventService.save(entityEvent));
			}
			//
			IdmEntityEventFilter filter = new IdmEntityEventFilter();
			filter.setOwnerType(MockOwner.class.getCanonicalName());
			filter.setStates(Lists.newArrayList(OperationState.CREATED));
			Assert.assertEquals(count, entityEventService.find(filter, new PageRequest(0, 1)).getTotalElements());
			//
			// execute
			helper.setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
			//
			// wait for executed events
			helper.waitForResult(res -> {
				return entityEventService.find(filter, new PageRequest(0, 1)).getTotalElements() != 0;
			}, 1000, Integer.MAX_VALUE);
			//
			// check what happened
			filter.setStates(Lists.newArrayList(OperationState.EXECUTED));
			Assert.assertEquals(1, entityEventService.find(filter, new PageRequest(0, 1)).getTotalElements());			
		} finally {
			entityEventService.delete(events.get(9)); // the last one
			helper.setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
			helper.enable(EntityEventDeleteExecutedProcessor.class);
		}
	}
}
