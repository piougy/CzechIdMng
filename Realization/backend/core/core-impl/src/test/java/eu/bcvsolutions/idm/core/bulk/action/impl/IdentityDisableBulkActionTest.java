package eu.bcvsolutions.idm.core.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.task.SelfLongRunningTaskEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration test for {@link IdentityDisableBulkAction}
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdentityDisableBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmNotificationLogService notificationLogService;
	
	private IdmIdentityDto loginIdentity;
	
	@Before
	public void login() {
		loginIdentity = this.createUserWithAuthorities(IdentityBasePermission.MANUALLYDISABLE, IdmBasePermission.READ);
		loginAsNoAdmin(loginIdentity.getUsername());
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void processBulkActionByIds() {
		List<IdmIdentityDto> identities = this.createIdentities(5);
		
		for (IdmIdentityDto identity : identities) {
			assertTrue(identity.getState() != IdentityState.DISABLED_MANUALLY);
		}
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityDisableBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(identities);
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 5l, null, null);
		
		for (UUID id : ids) {
			IdmIdentityDto identityDto = identityService.get(id);
			assertNotNull(identityDto);
			assertTrue(identityDto.getState() == IdentityState.DISABLED_MANUALLY);
		}
	}

	@Test
	public void processBulkActionByFilter() {
		String testFirstName = "bulkActionFirstName" + System.currentTimeMillis();
		List<IdmIdentityDto> identities = this.createIdentities(5);
		
		for (IdmIdentityDto identity : identities) {
			identity.setFirstName(testFirstName);
			identity = identityService.save(identity);
			assertTrue(identity.getState() != IdentityState.DISABLED_MANUALLY);
		}
		
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setFirstName(testFirstName);

		List<IdmIdentityDto> checkIdentities = identityService.find(filter, null).getContent();
		assertEquals(5, checkIdentities.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityDisableBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 5l, null, null);
		
		for (IdmIdentityDto identity : identities) {
			IdmIdentityDto dto = identityService.get(identity.getId());
			assertNotNull(dto);
			assertTrue(dto.getState() == IdentityState.DISABLED_MANUALLY);
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
			assertTrue(identity.getState() != IdentityState.DISABLED_MANUALLY);
		}
		
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setLastName(testLastName);
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityDisableBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		bulkAction.setRemoveIdentifiers(Sets.newHashSet(removedIdentity.getId(), removedIdentity2.getId()));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 3l, null, null);
		
		for (IdmIdentityDto identity : identities) {
			IdmIdentityDto dto = identityService.get(identity.getId());
			assertNotNull(dto);
			if (dto.getId().equals(removedIdentity.getId()) || dto.getId().equals(removedIdentity2.getId())) {
				assertTrue(dto.getState() != IdentityState.DISABLED_MANUALLY);
				continue;
			}
			assertTrue(dto.getState() == IdentityState.DISABLED_MANUALLY);
		}
	}
	
	@Test
	public void processBulkActionWithoutPermission() {
		// user hasn't permission for update identity
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
		
		List<IdmIdentityDto> identities = this.createIdentities(5);
		
		for (IdmIdentityDto identity : identities) {
			assertTrue(identity.getState() != IdentityState.DISABLED_MANUALLY);
		}
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityDisableBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(identities);
		bulkAction.setIdentifiers(this.getIdFromList(identities));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 0l, 0l, 5l);
		
		for (UUID id : ids) {
			IdmIdentityDto identityDto = identityService.get(id);
			assertNotNull(identityDto);
			assertTrue(identityDto.getState() != IdentityState.DISABLED_MANUALLY);
		}
	}
	
	@Test
	public void checkNotification() {
		List<IdmIdentityDto> identities = this.createIdentities(5);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityDisableBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(identities);
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 5l, null, null);
		
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(loginIdentity.getUsername());
		filter.setNotificationType(IdmEmailLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		
		assertEquals(1, notifications.size());
		
		IdmNotificationLogDto notificationLogDto = notifications.get(0);
		assertEquals(IdmEmailLog.NOTIFICATION_TYPE, notificationLogDto.getType());
		assertTrue(notificationLogDto.getMessage().getHtmlMessage().contains(bulkAction.getName()));
	}

	@Test
	public void checkEvaluatorForLrt() {
		IdmIdentityDto identity = getHelper().createIdentity();
		
		IdmRoleDto createRole = getHelper().createRole();
		getHelper().createBasePolicy(createRole.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, IdmBasePermission.READ, IdentityBasePermission.MANUALLYDISABLE);
		
		getHelper().createIdentityRole(identity, createRole);
		loginAsNoAdmin(identity.getUsername());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityDisableBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(this.createIdentities(5));
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		IdmLongRunningTaskDto lrt = checkResultLrt(processAction, 5l, null, null);
		
		try {
			longRunningTaskService.get(lrt.getId(), IdmBasePermission.READ);
			fail("User hasn't permission for read the long running task.");
		} catch (ForbiddenEntityException ex) {
			assertTrue(ex.getMessage().contains(lrt.getId().toString()));
			assertTrue(ex.getMessage().contains(IdmBasePermission.READ.toString()));
		} catch (Exception ex) {
			fail("Bad exception: " + ex.getMessage());
		}

		// create authorization with SelfLongRunningTaskEvaluator
		getHelper().createAuthorizationPolicy(createRole.getId(), CoreGroupPermission.SCHEDULER, IdmLongRunningTask.class, SelfLongRunningTaskEvaluator.class, IdmBasePermission.READ);
		
		try {
			IdmLongRunningTaskDto longRunningTaskDto = longRunningTaskService.get(lrt.getId(), IdmBasePermission.READ);
			assertNotNull(longRunningTaskDto);
			assertEquals(lrt, longRunningTaskDto);
		} catch (ForbiddenEntityException ex) {
			fail("User has permission for read the long running task. " + ex.getMessage());
		} catch (Exception ex) {
			fail("Bad exception: " + ex.getMessage());
		}
	}
}
