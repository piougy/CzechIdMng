package eu.bcvsolutions.idm.core.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.test.api.AbstractVerifiableUnitTest;

/**
 * Event processing tests
 * 
 * @author Radek Tomiška
 *
 */
public class EntityEventProcessorUnitTest extends AbstractVerifiableUnitTest {

	@Mock
	private IdmConfigurationService configurationServiceMock;

	@InjectMocks
	private EntityEventProcessor<?> mockedRoleProcessor = new EventProcessorRole();

	private static enum CustomType implements EventType {
		SAVE, CUSTOM
	}
	
	@Test
	public void testSupportAllIdentityEvents() {		
		EntityEventProcessor<?> processor = new EventProcessorIdentity();
		
		assertTrue(processor.supports(new IdentityEvent(IdentityEventType.CREATE, new IdmIdentityDto())));
		assertTrue(processor.supports(new IdentityEvent(IdentityEventType.UPDATE, new IdmIdentityDto())));
		assertTrue(processor.supports(new IdentityEvent(IdentityEventType.DELETE, new IdmIdentityDto())));
		assertFalse(processor.supports(new RoleEvent(RoleEventType.DELETE, new IdmRole())));
		assertTrue(processor.supports(new CoreEvent<IdmIdentityDto>(CustomType.SAVE, new IdmIdentityDto())));
		assertTrue(processor.supports(new CoreEvent<>(CustomType.CUSTOM, new IdmIdentityDto())));
	}
	
	@Test
	public void testSupportRoleDeleteOnly() {
		
		EntityEventProcessor<?> processor = new EventProcessorRole();
		
		assertFalse(processor.supports(new IdentityEvent(IdentityEventType.UPDATE, new IdmIdentityDto())));
		assertFalse(processor.supports(new IdentityEvent(IdentityEventType.DELETE, new IdmIdentityDto())));
		assertTrue(processor.supports(new RoleEvent(RoleEventType.DELETE, new IdmRole())));
		assertFalse(processor.supports(new CoreEvent<IdmIdentityDto>(CustomType.SAVE, new IdmIdentityDto())));
		assertFalse(processor.supports(new CoreEvent<>(CustomType.CUSTOM, new IdmIdentityDto())));
		assertTrue(processor.supports(new CoreEvent<IdmRole>(IdentityEventType.UPDATE, new IdmRole())));
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

	@Test
	public void testOverwriteOfEventTypes() {

		when(configurationServiceMock.getValue(anyString())).thenReturn(null);
		final List<String> eventTypes1 = Arrays.asList(mockedRoleProcessor.getEventTypes());
		assertEquals(eventTypes1.size(), 2);
		assertTrue(eventTypes1.contains(RoleEventType.DELETE.name()));
		assertTrue(eventTypes1.contains(RoleEventType.UPDATE.name()));
		verify(configurationServiceMock, times(1)).getValue(anyString());
		reset(configurationServiceMock);
		//
		when(configurationServiceMock.getValue(anyString())).thenReturn("one");
		final List<String> eventTypes2 = Arrays.asList(mockedRoleProcessor.getEventTypes());
		assertEquals(eventTypes2.size(), 1);
		assertTrue(eventTypes2.contains("one"));
		verify(configurationServiceMock, times(1)).getValue(anyString());
		reset(configurationServiceMock);
		//
		when(configurationServiceMock.getValue(anyString())).thenReturn("one, two,three, four");
		final List<String> eventTypes3 = Arrays.asList(mockedRoleProcessor.getEventTypes());
		assertEquals(eventTypes3.size(), 4);
		assertTrue(eventTypes3.contains("one"));
		assertTrue(eventTypes3.contains("two"));
		assertTrue(eventTypes3.contains("three"));
		assertTrue(eventTypes3.contains("four"));
		verify(configurationServiceMock, times(1)).getValue(anyString());
		reset(configurationServiceMock);
		//
		when(configurationServiceMock.getValue(anyString())).thenReturn("one,,,  ,  ,,  two  ,three , four,,");
		final List<String> eventTypes4 = Arrays.asList(mockedRoleProcessor.getEventTypes());
		assertEquals(eventTypes4.size(), 4);
		assertTrue(eventTypes4.contains("one"));
		assertTrue(eventTypes4.contains("two"));
		assertTrue(eventTypes4.contains("three"));
		assertTrue(eventTypes4.contains("four"));
		verify(configurationServiceMock, times(1)).getValue(anyString());
		reset(configurationServiceMock);

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
	
	private class EventProcessorRole extends AbstractEntityEventProcessor<IdmRole> {

		public EventProcessorRole() {
			super(RoleEventType.DELETE, IdentityEventType.UPDATE);
		}
		
		@Override
		public EventResult<IdmRole> process(EntityEvent<IdmRole> event) {
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
