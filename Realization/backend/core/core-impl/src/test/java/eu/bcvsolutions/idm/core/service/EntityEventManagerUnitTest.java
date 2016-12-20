package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Event processing tests
 * 
 * @author Radek Tomiška
 *
 */
public class EntityEventManagerUnitTest extends AbstractUnitTest {	
	
	private static enum CustomType implements EventType {
		SAVE, CUSTOM
	}
	
	@Test
	public void testSupportAllIdentityEvents() {		
		EntityEventProcessor<?> processor = new EventProcessorIdentity();
		
		assertTrue(processor.supports(new IdentityEvent(IdentityEventType.SAVE, new IdmIdentity())));
		assertTrue(processor.supports(new IdentityEvent(IdentityEventType.DELETE, new IdmIdentity())));
		assertFalse(processor.supports(new RoleEvent(RoleEventType.DELETE, new IdmRole())));
		assertTrue(processor.supports(new CoreEvent<IdmIdentity>(CustomType.SAVE, new IdmIdentity())));
		assertTrue(processor.supports(new CoreEvent<>(CustomType.CUSTOM, new IdmIdentity())));
	}
	
	@Test
	public void testSupportRoleDeleteOnly() {
		
		EntityEventProcessor<?> processor = new EventProcessorRole();
		
		assertFalse(processor.supports(new IdentityEvent(IdentityEventType.SAVE, new IdmIdentity())));
		assertFalse(processor.supports(new IdentityEvent(IdentityEventType.DELETE, new IdmIdentity())));
		assertTrue(processor.supports(new RoleEvent(RoleEventType.DELETE, new IdmRole())));
		assertFalse(processor.supports(new CoreEvent<IdmIdentity>(CustomType.SAVE, new IdmIdentity())));
		assertFalse(processor.supports(new CoreEvent<>(CustomType.CUSTOM, new IdmIdentity())));
		assertTrue(processor.supports(new CoreEvent<IdmRole>(IdentityEventType.SAVE, new IdmRole())));
	}

	private class EventProcessorIdentity extends AbstractEntityEventProcessor<IdmIdentity> {

		@Override
		public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
			event.getContent().setUsername("one");
			return new DefaultEventResult<>(event, this);
		}

		@Override
		public int getOrder() {
			return 1;
		}

	}
	
	private class EventProcessorRole extends AbstractEntityEventProcessor<IdmRole> {

		public EventProcessorRole() {
			super(RoleEventType.DELETE, IdentityEventType.SAVE);
		}
		
		@Override
		public EventResult<IdmRole> process(EntityEvent<IdmRole> event) {
			return new DefaultEventResult<>(event, this);
		}

		@Override
		public int getOrder() {
			return 1;
		}

	}
}
