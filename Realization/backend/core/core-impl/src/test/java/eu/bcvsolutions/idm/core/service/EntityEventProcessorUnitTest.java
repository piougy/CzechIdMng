package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
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
import eu.bcvsolutions.idm.core.model.event.RoleEventType;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultEntityEventProcessorManager;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Event processing tests
 * 
 * @author Radek Tomiška
 *
 */
public class EntityEventProcessorUnitTest extends AbstractUnitTest {

	private DefaultEntityEventProcessorManager entityProcessorService;
	
	@Before
	public void init() {
		List<EntityEventProcessor<?>> entityProcessors = new ArrayList<>();		
		entityProcessors.add(new EventProcessorThree());	
		entityProcessors.add(new EventProcessorTwo());
		entityProcessors.add(new EventProcessorOne());	
		entityProcessors.add(new EventProcessorFour());	
		entityProcessorService = new DefaultEntityEventProcessorManager(entityProcessors);
	}
	
	@Test
	public void testSupportContext() {
		EntityEvent<IdmIdentity> event = new IdentityEvent(IdentityEventType.SAVE, new IdmIdentity());
		
		EntityEventProcessor<?> processor = new EventProcessorOne();
		
		assertTrue(processor.supports(event));
	}
	
	@Test
	public void testOrder() {
		EntityEvent<IdmIdentity> context = new IdentityEvent(IdentityEventType.SAVE, new IdmIdentity());
		
		List<EntityEventProcessor<IdmIdentity>> processors = entityProcessorService.getProcessors(context);
		
		assertEquals(3, processors.size());
		
		entityProcessorService.process(context);
		
		assertEquals("two", context.getContent().getUsername());
	}
	
	@Test
	public void testSkipAfterComplete() {
		
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
	
	@Order(0)
	private class EventProcessorFour extends AbstractEntityEventProcessor<IdmRole> {

		public EventProcessorFour() {
			super(RoleEventType.DELETE);
		}

		@Override
		public EventResult<IdmRole> process(EntityEvent<IdmRole> event) {
			return new DefaultEventResult<>(event, this);
		}

	}
}
