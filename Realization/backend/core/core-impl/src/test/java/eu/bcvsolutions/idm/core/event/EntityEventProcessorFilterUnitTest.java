package eu.bcvsolutions.idm.core.event;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.EntityEventProcessorDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EntityEventProcessorFilter;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentitySaveProcessor;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultEntityEventManager;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Event processors filtering
 * 
 * @author Petr Hanák
 * @author Radek Tomiška
 */
public class EntityEventProcessorFilterUnitTest extends AbstractUnitTest {
	
	@Mock private ApplicationContext context;
	@Mock private ApplicationEventPublisher publisher;
	@Mock private EnabledEvaluator enabledEvaluator;
	@Mock private LookupService lookupService;
	//
	@InjectMocks private DefaultEntityEventManager eventManager;

	@Test
	public void testEmptyFilter() {
		Map<String, Object> registeredProcessors = new HashMap<>();
		registeredProcessors.put(IdentitySaveProcessor.PROCESSOR_NAME, new MockIdentityProcessor(IdentitySaveProcessor.PROCESSOR_NAME, null, null));
		registeredProcessors.put(IdentityDeleteProcessor.PROCESSOR_NAME, new MockIdentityProcessor(IdentityDeleteProcessor.PROCESSOR_NAME, null, null));
		when(enabledEvaluator.isEnabled(any(Object.class))).thenReturn(true);
		when(context.getBeansOfType(any())).thenReturn(registeredProcessors);
		//
		List<EntityEventProcessorDto> results = eventManager.find(null);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentitySaveProcessor.PROCESSOR_NAME)));
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentityDeleteProcessor.PROCESSOR_NAME)));
	}
	
	@Test
	public void testNameFilter() {
		Map<String, Object> registeredProcessors = new HashMap<>();
		registeredProcessors.put(IdentitySaveProcessor.PROCESSOR_NAME, new MockIdentityProcessor(IdentitySaveProcessor.PROCESSOR_NAME, null, null));
		registeredProcessors.put(IdentityDeleteProcessor.PROCESSOR_NAME, new MockIdentityProcessor(IdentityDeleteProcessor.PROCESSOR_NAME, null, null));
		when(enabledEvaluator.isEnabled(any(Object.class))).thenReturn(true);
		when(context.getBeansOfType(any())).thenReturn(registeredProcessors);
		//
		EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
		filter.setName(IdentitySaveProcessor.PROCESSOR_NAME);
		//
		List<EntityEventProcessorDto> results = eventManager.find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentitySaveProcessor.PROCESSOR_NAME)));
	}
	
	@Test
	public void testTextFilter() {
		Map<String, Object> registeredProcessors = new HashMap<>();
		registeredProcessors.put(IdentitySaveProcessor.PROCESSOR_NAME, new MockIdentityProcessor(IdentitySaveProcessor.PROCESSOR_NAME, null, null));
		registeredProcessors.put(IdentityDeleteProcessor.PROCESSOR_NAME, new MockIdentityProcessor(IdentityDeleteProcessor.PROCESSOR_NAME, null, null));
		when(enabledEvaluator.isEnabled(any(Object.class))).thenReturn(true);
		when(context.getBeansOfType(any())).thenReturn(registeredProcessors);
		//
		EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
		filter.setText("wrong");
		//
		List<EntityEventProcessorDto> results = eventManager.find(filter);
		Assert.assertTrue(results.isEmpty());
		//
		filter.setText("identity");
		//
		results = eventManager.find(filter);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentitySaveProcessor.PROCESSOR_NAME)));
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentityDeleteProcessor.PROCESSOR_NAME)));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testIdFilter() {
		Map<String, Object> registeredProcessors = new HashMap<>();
		registeredProcessors.put(IdentitySaveProcessor.PROCESSOR_NAME, new MockIdentityProcessor(IdentitySaveProcessor.PROCESSOR_NAME, null, null));
		registeredProcessors.put(IdentityDeleteProcessor.PROCESSOR_NAME, new MockIdentityProcessor(IdentityDeleteProcessor.PROCESSOR_NAME, null, null));
		when(enabledEvaluator.isEnabled(any(Object.class))).thenReturn(true);
		when(context.getBeansOfType(any())).thenReturn(registeredProcessors);
		//
		EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
		filter.setId(UUID.randomUUID());
		eventManager.find(filter);
	}
	
	@Test
	public void testDescriptionFilter() {
		Map<String, Object> registeredProcessors = new HashMap<>();
		registeredProcessors.put(IdentitySaveProcessor.PROCESSOR_NAME, new MockIdentityProcessor(
				IdentitySaveProcessor.PROCESSOR_NAME, CoreModuleDescriptor.MODULE_ID, "description one"));
		registeredProcessors.put(IdentityDeleteProcessor.PROCESSOR_NAME, new MockIdentityProcessor(
				IdentityDeleteProcessor.PROCESSOR_NAME, "custom", "description two"));
		when(enabledEvaluator.isEnabled(any(Object.class))).thenReturn(true);
		when(context.getBeansOfType(any())).thenReturn(registeredProcessors);
		//
		EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
		filter.setDescription("description");
		//
		List<EntityEventProcessorDto> results = eventManager.find(filter);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentitySaveProcessor.PROCESSOR_NAME)));
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentityDeleteProcessor.PROCESSOR_NAME)));
		//
		filter.setDescription("one");
		results = eventManager.find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentitySaveProcessor.PROCESSOR_NAME)));
	}
	
	@Test
	public void testModuleFilter() {
		Map<String, Object> registeredProcessors = new HashMap<>();
		registeredProcessors.put(IdentitySaveProcessor.PROCESSOR_NAME, new MockIdentityProcessor(
				IdentitySaveProcessor.PROCESSOR_NAME, CoreModuleDescriptor.MODULE_ID, "description one"));
		registeredProcessors.put(IdentityDeleteProcessor.PROCESSOR_NAME, new MockIdentityProcessor(
				IdentityDeleteProcessor.PROCESSOR_NAME, "custom", "description two"));
		when(enabledEvaluator.isEnabled(any(Object.class))).thenReturn(true);
		when(context.getBeansOfType(any())).thenReturn(registeredProcessors);
		//
		EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
		filter.setModule("wrong");
		//
		List<EntityEventProcessorDto> results = eventManager.find(filter);
		Assert.assertTrue(results.isEmpty());
		//
		filter.setModule(CoreModuleDescriptor.MODULE_ID);
		results = eventManager.find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentitySaveProcessor.PROCESSOR_NAME)));
	}
	
	@Test
	public void testEventTypeFilter() {
		Map<String, Object> registeredProcessors = new HashMap<>();
		registeredProcessors.put(IdentitySaveProcessor.PROCESSOR_NAME, new MockIdentityProcessor(
				IdentitySaveProcessor.PROCESSOR_NAME, CoreModuleDescriptor.MODULE_ID, "description one", CoreEventType.CREATE, CoreEventType.DELETE));
		registeredProcessors.put(IdentityDeleteProcessor.PROCESSOR_NAME, new MockIdentityProcessor(
				IdentityDeleteProcessor.PROCESSOR_NAME, "custom", "description two", CoreEventType.UPDATE, CoreEventType.DELETE));
		when(enabledEvaluator.isEnabled(any(Object.class))).thenReturn(true);
		when(context.getBeansOfType(any())).thenReturn(registeredProcessors);
		//
		EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
		filter.setEventTypes(Lists.newArrayList("wrong"));
		//
		List<EntityEventProcessorDto> results = eventManager.find(filter);
		Assert.assertTrue(results.isEmpty());
		//
		filter.setEventTypes(Lists.newArrayList(CoreEventType.DELETE.name()));
		results = eventManager.find(filter);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentitySaveProcessor.PROCESSOR_NAME)));
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentityDeleteProcessor.PROCESSOR_NAME)));
		//
		filter.setEventTypes(Lists.newArrayList(CoreEventType.DELETE.name(), CoreEventType.UPDATE.name()));
		results = eventManager.find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentityDeleteProcessor.PROCESSOR_NAME)));
	}
	
	@Test
	public void testEntityTypeFilter() {
		Map<String, Object> registeredProcessors = new HashMap<>();
		registeredProcessors.put(IdentitySaveProcessor.PROCESSOR_NAME, new MockIdentityProcessor(
				IdentitySaveProcessor.PROCESSOR_NAME, CoreModuleDescriptor.MODULE_ID, "description one", CoreEventType.CREATE, CoreEventType.DELETE));
		registeredProcessors.put(IdentityDeleteProcessor.PROCESSOR_NAME, new MockRoleProcessor(
				IdentityDeleteProcessor.PROCESSOR_NAME, "custom", "description two", CoreEventType.UPDATE, CoreEventType.DELETE));
		when(enabledEvaluator.isEnabled(any(Object.class))).thenReturn(true);
		when(context.getBeansOfType(any())).thenReturn(registeredProcessors);
		//
		EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
		filter.setEntityType("wrong");
		//
		List<EntityEventProcessorDto> results = eventManager.find(filter);
		Assert.assertTrue(results.isEmpty());
		//
		filter.setEntityType(IdmIdentityDto.class.getSimpleName());
		results = eventManager.find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentitySaveProcessor.PROCESSOR_NAME)));
		//
		filter.setEntityType(IdmRoleDto.class.getSimpleName());
		results = eventManager.find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentityDeleteProcessor.PROCESSOR_NAME)));
	}
	
	@Test
	public void testContentTypeFilter() {
		Map<String, Object> registeredProcessors = new HashMap<>();
		registeredProcessors.put(IdentitySaveProcessor.PROCESSOR_NAME, new MockIdentityProcessor(
				IdentitySaveProcessor.PROCESSOR_NAME, CoreModuleDescriptor.MODULE_ID, "description one", CoreEventType.CREATE, CoreEventType.DELETE));
		registeredProcessors.put(IdentityDeleteProcessor.PROCESSOR_NAME, new MockRoleProcessor(
				IdentityDeleteProcessor.PROCESSOR_NAME, "custom", "description two", CoreEventType.UPDATE, CoreEventType.DELETE));
		when(enabledEvaluator.isEnabled(any(Object.class))).thenReturn(true);
		when(context.getBeansOfType(any())).thenReturn(registeredProcessors);
		//
		EntityEventProcessorFilter filter = new EntityEventProcessorFilter();
		filter.setContentClass(IdmIdentityContractDto.class);
		//
		List<EntityEventProcessorDto> results = eventManager.find(filter);
		Assert.assertTrue(results.isEmpty());
		//
		filter.setContentClass(IdmIdentityDto.class);
		results = eventManager.find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentitySaveProcessor.PROCESSOR_NAME)));
		//
		filter.setContentClass(IdmRoleDto.class);
		results = eventManager.find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentityDeleteProcessor.PROCESSOR_NAME)));
		//
		filter.setContentClass(AbstractDto.class);
		results = eventManager.find(filter);
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentitySaveProcessor.PROCESSOR_NAME)));
		Assert.assertTrue(results.stream().anyMatch(e -> e.getName().equals(IdentityDeleteProcessor.PROCESSOR_NAME)));
	}
	
	private class MockIdentityProcessor extends AbstractEntityEventProcessor<IdmIdentityDto> {

		private String name;
		private String module;
		private String description;
		
		public MockIdentityProcessor(String name, String module, String description, EventType... types) {
			super(types);
			//
			this.name = name;
			this.module = module;
			this.description = description;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public String getDescription() {
			return description;
		}
		
		@Override
		public String getModule() {
			return module;
		}
		
		@Override
		public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
			return null;
		}

		@Override
		public int getOrder() {
			return 0;
		}
	}
	
	private class MockRoleProcessor extends AbstractEntityEventProcessor<IdmRoleDto> {

		private String name;
		private String module;
		private String description;
		
		public MockRoleProcessor(String name, String module, String description, EventType... types) {
			super(types);
			//
			this.name = name;
			this.module = module;
			this.description = description;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public String getDescription() {
			return description;
		}
		
		@Override
		public String getModule() {
			return module;
		}
		
		@Override
		public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
			return null;
		}

		@Override
		public int getOrder() {
			return 0;
		}
	}
}
