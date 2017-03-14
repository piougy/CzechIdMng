package eu.bcvsolutions.idm.core.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.EntityEventProcessorDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EntityEventProcessorFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.DefaultEventContext;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultEntityEventManager;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * entity events integration tests
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultEntityEventManagerIntergationTest extends AbstractIntegrationTest {

	@Autowired
	private ApplicationContext context;
	@Autowired
	private ApplicationEventPublisher publisher;
	@Autowired
	private EnabledEvaluator enabledEvaluator;
	//
	private DefaultEntityEventManager entityEventManager; 
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_USER_1);
		entityEventManager = new DefaultEntityEventManager(context, publisher, enabledEvaluator);
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
		assertTrue(size > 4);
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
	    assertEquals(4, processors.size());
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
}
