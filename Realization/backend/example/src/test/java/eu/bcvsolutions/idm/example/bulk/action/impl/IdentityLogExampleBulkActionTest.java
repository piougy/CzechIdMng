package eu.bcvsolutions.idm.example.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.bulk.action.impl.IdentityDeleteBulkAction;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Test for {@link IdentityLogExampleBulkAction}
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class IdentityLogExampleBulkActionTest extends AbstractBulkActionTest {
	
	@Before
	public void login() {
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkAction() {
		IdmBulkActionDto exampleAction = findBulkAction(IdmIdentity.class, IdentityLogExampleBulkAction.BULK_ACTION_NAME);
		
		assertNotNull(exampleAction);
		Assert.assertEquals(NotificationLevel.INFO, exampleAction.getLevel());
		
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityDto identity2 = getHelper().createIdentity();
		IdmIdentityDto identity3 = getHelper().createIdentity();
		
		exampleAction.setIdentifiers(Sets.newHashSet(identity.getId(), identity2.getId(), identity3.getId()));
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(exampleAction);
		assertNotNull(processAction.getLongRunningTaskId());
		
		IdmLongRunningTaskFilter context = new IdmLongRunningTaskFilter();
		context.setIncludeItemCounts(true);
		IdmLongRunningTaskDto longRunningTask = longRunningTaskService.get(processAction.getLongRunningTaskId(), context);

		assertEquals(Long.valueOf(3), longRunningTask.getCount());
		assertEquals(Long.valueOf(3), longRunningTask.getSuccessItemCount());
		assertEquals(Long.valueOf(0), longRunningTask.getFailedItemCount());
		assertEquals(Long.valueOf(0), longRunningTask.getWarningItemCount());

		List<IdmProcessedTaskItemDto> items = getItemsForLrt(longRunningTask);
		assertEquals(3, items.size());

		boolean identitySuccess = false;
		boolean identitySuccess2 = false;
		boolean identitySuccess3 = false;
		for (IdmProcessedTaskItemDto item : items) {
			if (item.getOperationResult().getState() != OperationState.EXECUTED) {
				fail("Identity " + item.getReferencedEntityId() + ", failed.");
			}
			if (item.getReferencedEntityId().equals(identity.getId())) {
				identitySuccess = true;
			}
			if (item.getReferencedEntityId().equals(identity2.getId())) {
				identitySuccess2 = true;
			}
			if (item.getReferencedEntityId().equals(identity3.getId())) {
				identitySuccess3 = true;
			}
		}
		assertTrue(identitySuccess);
		assertTrue(identitySuccess2);
		assertTrue(identitySuccess3);
	}
	
	@Test
	@Transactional
	public void testDeleteIdentityWithoutModuleIsEnabled() {
		try {
			getHelper().loginAdmin();
			getHelper().disableModule(ExampleModuleDescriptor.MODULE_ID);
			
			IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
			
			IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityDeleteBulkAction.NAME);
			Set<UUID> ids = Sets.newHashSet(identity.getId());
			bulkAction.setIdentifiers(ids);
			bulkActionManager.processAction(bulkAction);
			
			for (UUID id : ids) {
				IdmIdentityDto identityDto = getHelper().getService(IdmIdentityService.class).get(id);
				Assert.assertNull(identityDto);
			}
		} finally {
			getHelper().enableModule(ExampleModuleDescriptor.MODULE_ID);
			getHelper().logout();
		}
	}
}
