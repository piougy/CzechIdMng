package eu.bcvsolutions.idm.core.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.test.api.AbstractVerifiableUnitTest;

/**
 * Event processing tests
 * 
 * @author Radek Tomiška
 *
 */
public class EntityEventProcessorUnitTest extends AbstractVerifiableUnitTest {	
	
	private static enum CustomType implements EventType {
		SAVE, CUSTOM
	}
	
	@Test
	public void testSupportAllIdentityEvents() {		
		EntityEventProcessor<?> processor = new EventProcessorIdentity();
		
		assertTrue(processor.supports(new IdentityEvent(IdentityEventType.CREATE, new IdmIdentityDto())));
		assertTrue(processor.supports(new IdentityEvent(IdentityEventType.UPDATE, new IdmIdentityDto())));
		assertTrue(processor.supports(new IdentityEvent(IdentityEventType.DELETE, new IdmIdentityDto())));
		assertFalse(processor.supports(new RoleEvent(RoleEventType.DELETE, new IdmRoleDto())));
		assertTrue(processor.supports(new CoreEvent<IdmIdentityDto>(CustomType.SAVE, new IdmIdentityDto())));
		assertTrue(processor.supports(new CoreEvent<>(CustomType.CUSTOM, new IdmIdentityDto())));
	}
	
	@Test
	public void testSupportRoleDeleteOnly() {
		
		EntityEventProcessor<?> processor = new EventProcessorRole();
		
		assertFalse(processor.supports(new IdentityEvent(IdentityEventType.UPDATE, new IdmIdentityDto())));
		assertFalse(processor.supports(new IdentityEvent(IdentityEventType.DELETE, new IdmIdentityDto())));
		assertTrue(processor.supports(new RoleEvent(RoleEventType.DELETE, new IdmRoleDto())));
		assertFalse(processor.supports(new CoreEvent<IdmIdentityDto>(CustomType.SAVE, new IdmIdentityDto())));
		assertFalse(processor.supports(new CoreEvent<>(CustomType.CUSTOM, new IdmIdentityDto())));
		assertTrue(processor.supports(new CoreEvent<IdmRoleDto>(IdentityEventType.UPDATE, new IdmRoleDto())));
	}
	
	@Test
	public void testSuppotsAll() {
		
		EntityEventProcessor<?> processor = new EventProcessorBase();
		
		assertTrue(processor.supports(new IdentityEvent(IdentityEventType.UPDATE, new IdmIdentityDto())));
		assertTrue(processor.supports(new IdentityEvent(IdentityEventType.DELETE, new IdmIdentityDto())));
		assertTrue(processor.supports(new IdentityContractEvent(IdentityContractEventType.DELETE, new IdmIdentityContractDto())));
		assertTrue(processor.supports(new CoreEvent<IdmIdentityDto>(CustomType.SAVE, new IdmIdentityDto())));
		assertTrue(processor.supports(new CoreEvent<>(CustomType.CUSTOM, new IdmIdentityDto())));
		assertTrue(processor.supports(new CoreEvent<IdmIdentityContractDto>(IdentityContractEventType.UPDATE, new IdmIdentityContractDto())));
	}

	private class EventProcessorIdentity extends AbstractEntityEventProcessor<IdmIdentityDto> {

		@Override
		public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
			event.getContent().setUsername("one");
			return new DefaultEventResult<>(event, this);
		}

		@Override
		public int getOrder() {
			return 1;
		}

		@Override
		public String getName() {
			return "one";
		}

	}
	
	private class EventProcessorRole extends AbstractEntityEventProcessor<IdmRoleDto> {

		public EventProcessorRole() {
			super(RoleEventType.DELETE, IdentityEventType.UPDATE);
		}
		
		@Override
		public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
			return new DefaultEventResult<>(event, this);
		}

		@Override
		public int getOrder() {
			return 1;
		}

		@Override
		public String getName() {
			return "two";
		}
	}
	
	private class EventProcessorBase extends AbstractEntityEventProcessor<BaseDto> {

		@Override
		public EventResult<BaseDto> process(EntityEvent<BaseDto> event) {
			return new DefaultEventResult<>(event, this);
		}

		@Override
		public int getOrder() {
			return 1;
		}

		@Override
		public String getName() {
			return "base-one";
		}

	}
}
