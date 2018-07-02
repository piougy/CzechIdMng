package eu.bcvsolutions.idm.core.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link IdentitySaveBulkAction}
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdentitySaveBulkActionTest extends AbstractBulkActionTest {

	private String DELETE_PROCESSOR_KEY = "idm.sec.core.processor.entity-event-delete-executed-processor.enabled";
	private boolean originalAsynchronousValue = EventConfiguration.DEFAULT_EVENT_ASYNCHRONOUS_ENABLED;

	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmEntityEventService entityEventService;
	@Autowired
	private IdmConfigurationService configurationService;
	@Autowired
	private EventConfiguration eventConfiguration;

	@Before
	public void login() {
		IdmIdentityDto identity = getHelper().createIdentity();

		IdmRoleDto createRole = getHelper().createRole();
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class,
				IdmBasePermission.READ, IdmBasePermission.UPDATE);

		getHelper().createIdentityRole(identity, createRole);
		loginAsNoAdmin(identity.getUsername());

		originalAsynchronousValue = eventConfiguration.isAsynchronous();
	}

	@After
	public void logout() {
		configurationService.setBooleanValue(DELETE_PROCESSOR_KEY, Boolean.TRUE);
		configurationService.setBooleanValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED,
				originalAsynchronousValue);
		super.logout();
	}

	@Test
	public void processBulkActionByIds() {
		List<IdmIdentityDto> identities = this.createIdentities(5);

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentitySaveBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(identities);
		bulkAction.setIdentifiers(this.getIdFromList(identities));

		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentitySaveBulkAction.ONLY_NOTIFY_CODE, Boolean.TRUE);
		bulkAction.setProperties(properties);

		// turn off remove events
		configurationService.setBooleanValue(DELETE_PROCESSOR_KEY, Boolean.FALSE);

		// events must be asynchronous -> we want put into queue
		configurationService.setBooleanValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, Boolean.TRUE);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);

		checkResultLrt(processAction, 5l, null, null);

		for (UUID id : ids) {
			IdmIdentityDto identityDto = identityService.get(id);
			assertNotNull(identityDto);

			IdmEntityEventFilter filter = new IdmEntityEventFilter();
			filter.setOwnerId(id);
			List<IdmEntityEventDto> events = entityEventService.find(filter, null).getContent();
			assertEquals(1, events.size());
			IdmEntityEventDto eventDto = events.get(0);
			assertNull(eventDto.getParentEventType());
			assertNull(eventDto.getParent());
		}
	}

	@Test
	public void processBulkActionByFilter() {
		String testFirstName = "bulkActionFirstName" + System.currentTimeMillis();
		List<IdmIdentityDto> identities = this.createIdentities(5);

		for (IdmIdentityDto identity : identities) {
			identity.setFirstName(testFirstName);
			identity = identityService.save(identity);
		}

		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setFirstName(testFirstName);

		List<IdmIdentityDto> checkIdentities = identityService.find(filter, null).getContent();
		assertEquals(5, checkIdentities.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentitySaveBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));

		// turn off remove events
		configurationService.setBooleanValue(DELETE_PROCESSOR_KEY, Boolean.FALSE);

		// events must be asynchronous -> we want put into queue
		configurationService.setBooleanValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, Boolean.TRUE);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 5l, null, null);

		for (IdmIdentityDto identity : identities) {
			IdmIdentityDto dto = identityService.get(identity.getId());
			assertNotNull(dto);
			IdmEntityEventFilter filterEvent = new IdmEntityEventFilter();
			filterEvent.setOwnerId(identity.getId());
			List<IdmEntityEventDto> events = entityEventService.find(filterEvent, null).getContent();
			assertEquals(1, events.size());
			IdmEntityEventDto eventDto = events.get(0);
			assertEquals(IdentityEvent.IdentityEventType.UPDATE.name(), eventDto.getParentEventType());
		}
	}

	@Test
	public void processBulkActionByFilterWithRemove() {
		String testLastName = "bulkActionLastName" + System.currentTimeMillis();
		List<IdmIdentityDto> identities = this.createIdentities(5);
		IdmIdentityDto removedIdentity = identities.get(0);
		IdmIdentityDto removedIdentity2 = identities.get(1);

		for (IdmIdentityDto identity : identities) {
			identity.setLastName(testLastName);
			identity = identityService.save(identity);
			identity = identityService.disable(identity.getId());
			assertTrue(identity.getState() == IdentityState.DISABLED_MANUALLY);
		}

		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setLastName(testLastName);
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentitySaveBulkAction.NAME);

		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentitySaveBulkAction.ONLY_NOTIFY_CODE, Boolean.TRUE);
		bulkAction.setProperties(properties);

		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		bulkAction.setRemoveIdentifiers(Sets.newHashSet(removedIdentity.getId(), removedIdentity2.getId()));

		// turn off remove events
		configurationService.setBooleanValue(DELETE_PROCESSOR_KEY, Boolean.FALSE);

		// events must be asynchronous -> we want put into queue
		configurationService.setBooleanValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, Boolean.TRUE);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 3l, null, null);

		for (IdmIdentityDto identity : identities) {
			IdmIdentityDto dto = identityService.get(identity.getId());
			assertNotNull(dto);
			IdmEntityEventFilter filterEvent = new IdmEntityEventFilter();
			filterEvent.setOwnerId(identity.getId());
			List<IdmEntityEventDto> events = entityEventService.find(filterEvent, null).getContent();

			if (identity.getId().equals(removedIdentity.getId()) || identity.getId().equals(removedIdentity2.getId())) {
				assertEquals(0, events.size());
			} else {
				assertEquals(1, events.size());
				IdmEntityEventDto eventDto = events.get(0);
				assertNull(eventDto.getParentEventType());
				assertNull(eventDto.getParent());
			}
		}
	}

	@Test
	public void processBulkActionWithoutPermission() {
		// user hasn't permission for update identity
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());

		List<IdmIdentityDto> identities = this.createIdentities(5);

		for (IdmIdentityDto identity : identities) {
			identity = identityService.disable(identity.getId());
			assertTrue(identity.getState() == IdentityState.DISABLED_MANUALLY);
		}

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentitySaveBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(identities);

		Map<String, Object> properties = new HashMap<>();
		properties.put(IdentitySaveBulkAction.ONLY_NOTIFY_CODE, Boolean.TRUE);
		bulkAction.setProperties(properties);

		bulkAction.setIdentifiers(this.getIdFromList(identities));

		// turn off remove events
		configurationService.setBooleanValue(DELETE_PROCESSOR_KEY, Boolean.FALSE);

		// events must be asynchronous -> we want put into queue
		configurationService.setBooleanValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, Boolean.TRUE);

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);

		checkResultLrt(processAction, 0l, 5l, 0l);

		for (UUID id : ids) {
			IdmIdentityDto identityDto = identityService.get(id);
			assertNotNull(identityDto);
			IdmEntityEventFilter filterEvent = new IdmEntityEventFilter();
			filterEvent.setOwnerId(id);
			List<IdmEntityEventDto> events = entityEventService.find(filterEvent, null).getContent();
			assertEquals(0, events.size());
		}
	}
}
