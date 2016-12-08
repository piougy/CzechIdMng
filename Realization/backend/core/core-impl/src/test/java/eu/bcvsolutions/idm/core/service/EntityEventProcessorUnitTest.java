package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.annotation.Order;

import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEventType;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultEntityEventProcessorManager;
import eu.bcvsolutions.idm.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Event processing tests
 * 
 * @author Radek Tomiška
 *
 */
public class EntityEventProcessorUnitTest extends AbstractUnitTest {

	private DefaultEntityEventProcessorManager entityProcessorService;
	private RoleEventProcessor roleEventProcessor;
	
	@Mock
	private EnabledEvaluator enabledEvaluator;
	
	@Before
	public void init() {
		List<EntityEventProcessor<?>> entityProcessors = new ArrayList<>();		
		entityProcessors.add(new EventProcessorThree());	
		entityProcessors.add(new EventProcessorTwo());
		entityProcessors.add(new EventProcessorOne());	
		roleEventProcessor = new RoleEventProcessor();
		entityProcessors.add(roleEventProcessor);	
		entityProcessors.add(new EventProcessorFive());	
		entityProcessorService = new DefaultEntityEventProcessorManager(entityProcessors, enabledEvaluator);
	}
	
	@Test
	public void testSupportContext() {
		EntityEvent<IdmIdentity> event = new IdentityEvent(IdentityEventType.SAVE, new IdmIdentity());
		
		EntityEventProcessor<?> processor = new EventProcessorOne();
		
		assertTrue(processor.supports(event));
	}
	
	@Test
	public void testOrder() {
		when(enabledEvaluator.isEnabled((Object)any())).thenReturn(true);		
		EntityEvent<IdmIdentity> event = new IdentityEvent(IdentityEventType.SAVE, new IdmIdentity());		
		List<EntityEventProcessor<IdmIdentity>> processors = entityProcessorService.getProcessors(event);
		
		assertEquals(4, processors.size());
		
		entityProcessorService.process(event);
		
		assertEquals("two", event.getContent().getUsername());
		
		verify(enabledEvaluator, times(8)).isEnabled((Object)any());
	}
	
	@Test
	public void testSkipAfterComplete() {
		when(enabledEvaluator.isEnabled((Object)any())).thenReturn(true);		
		EntityEvent<IdmIdentity> event = new IdentityEvent(IdentityEventType.SAVE, new IdmIdentity());		
		
		entityProcessorService.process(event);
		
		assertEquals("two", event.getContent().getUsername());
		
		verify(enabledEvaluator, times(4)).isEnabled((Object)any());
	}
	
	@Test
	public void testDisabledModule() {
		when(enabledEvaluator.isEnabled(roleEventProcessor)).thenReturn(false);
		EntityEvent<IdmRole> event = new RoleEvent(RoleEventType.DELETE, new IdmRole());		
		List<EntityEventProcessor<IdmRole>> processors = entityProcessorService.getProcessors(event);
		
		assertEquals(0, processors.size());		
		
		verify(enabledEvaluator).isEnabled(roleEventProcessor);
	}
	
	@Test
	public void testEnabledModule() {
		when(enabledEvaluator.isEnabled(roleEventProcessor)).thenReturn(true);
		EntityEvent<IdmRole> event = new RoleEvent(RoleEventType.DELETE, new IdmRole());		
		List<EntityEventProcessor<IdmRole>> processors = entityProcessorService.getProcessors(event);
		
		assertEquals(1, processors.size());		
		
		verify(enabledEvaluator).isEnabled(roleEventProcessor);
	}

	@Order(1)
	private class EventProcessorOne extends AbstractEntityEventProcessor<IdmIdentity> {

		public EventProcessorOne() {
			super(IdentityEventType.SAVE);
		}

		@Override
		public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
			event.getContent().setUsername("one");
			return new DefaultEventResult<>(event, this);
		}

	}
	
	@Order(2)
	private class EventProcessorTwo extends AbstractEntityEventProcessor<IdmIdentity> {

		public EventProcessorTwo() {
			super(IdentityEventType.SAVE);
		}

		@Override
		public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
			event.getContent().setUsername("two");
			return new DefaultEventResult<>(event, this, true);
		}

	}
	
	@Order(3)
	private class EventProcessorThree extends AbstractEntityEventProcessor<IdmIdentity> {

		public EventProcessorThree() {
			super(IdentityEventType.SAVE);
		}

		@Override
		public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
			event.getContent().setUsername("three");
			return new DefaultEventResult<>(event, this, true);
		}

	}
	
	@Order(4)
	private class EventProcessorFive extends AbstractEntityEventProcessor<IdmIdentity> {

		public EventProcessorFive() {
			super(IdentityEventType.SAVE);
		}

		@Override
		public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
			event.getContent().setUsername("three");
			return new DefaultEventResult<>(event, this, true);
		}

	}
	
	@Order(0)
	private class RoleEventProcessor extends AbstractEntityEventProcessor<IdmRole> {

		public RoleEventProcessor() {
			super(RoleEventType.DELETE);
		}

		@Override
		public EventResult<IdmRole> process(EntityEvent<IdmRole> event) {
			return new DefaultEventResult<>(event, this);
		}

	}
}
