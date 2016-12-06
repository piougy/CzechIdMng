package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.annotation.Order;

import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.IdentityOperationType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultEntityEventProcessorService;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Event processing tests
 * 
 * @author Radek Tomiška
 *
 */
public class EntityEventProcessorUnitTest extends AbstractUnitTest {

	private DefaultEntityEventProcessorService entityProcessorService;
	
	@Before
	public void init() {
		List<EntityEventProcessor<?>> entityProcessors = new ArrayList<>();		
		entityProcessors.add(new EventProcessorThree());	
		entityProcessors.add(new EventProcessorTwo());
		entityProcessors.add(new EventProcessorOne());	
		entityProcessors.add(new EventProcessorFour());	
		entityProcessorService = new DefaultEntityEventProcessorService(entityProcessors);
	}
	
	@Test
	public void testSupportContext() {
		EntityEvent<IdmIdentity> context = new IdentityEvent(IdentityOperationType.SAVE, new IdmIdentity());
		
		EntityEventProcessor<?> processor = new EventProcessorOne();
		
		assertTrue(processor.supports(context));
	}
	
	@Test
	public void testOrder() {
		EntityEvent<IdmIdentity> context = new IdentityEvent(IdentityOperationType.SAVE, new IdmIdentity());
		
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
			super(IdentityOperationType.SAVE);
		}

		@Override
		public EntityEvent<IdmIdentity> process(EntityEvent<IdmIdentity> context) {
			context.getContent().setUsername("one");
			return context;
		}

	}
	
	@Order(2)
	private class EventProcessorTwo extends AbstractEntityEventProcessor<IdmIdentity> {

		public EventProcessorTwo() {
			super(IdentityOperationType.SAVE);
		}

		@Override
		public EntityEvent<IdmIdentity> process(EntityEvent<IdmIdentity> context) {
			context.getContent().setUsername("two");
			context.setComplete(true);
			return context;
		}

	}
	
	@Order(3)
	private class EventProcessorThree extends AbstractEntityEventProcessor<IdmIdentity> {

		public EventProcessorThree() {
			super(IdentityOperationType.SAVE);
		}

		@Override
		public EntityEvent<IdmIdentity> process(EntityEvent<IdmIdentity> context) {
			context.getContent().setUsername("three");
			return context;
		}

	}
	
	@Order(0)
	private class EventProcessorFour extends AbstractEntityEventProcessor<IdmRole> {

		public EventProcessorFour() {
			super("SAVE");
		}

		@Override
		public EntityEvent<IdmRole> process(EntityEvent<IdmRole> context) {
			return context;
		}

	}
}
