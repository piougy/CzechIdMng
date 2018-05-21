package eu.bcvsolutions.idm.example.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmLoggingEventFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Test for {@link IdentityLogExampleBulkAction}
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdentityLogExampleBulkActionTest extends AbstractBulkActionTest {
	
	@Autowired
	private IdmLoggingEventService loggingEventService;
	
	@Before
	public void login() {
		IdmIdentityDto adminIdentity = this.getTransactionTemplate().execute(new TransactionCallback<IdmIdentityDto>() {
			@Override
			public IdmIdentityDto doInTransaction(TransactionStatus arg0) {
				return getHelper().createIdentity();
			}
		});
		this.loginAsAdmin(adminIdentity.getUsername());
		setSynchronousLrt(false);
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processTask() throws InterruptedException {
		IdmBulkActionDto exampleAction = findBulkAction(IdmIdentity.class, IdentityLogExampleBulkAction.BULK_ACTION_NAME);
		
		assertNotNull(exampleAction);
		
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityDto identity2 = getHelper().createIdentity();
		IdmIdentityDto identity3 = getHelper().createIdentity();
		
		exampleAction.setIdentifiers(Sets.newHashSet(identity.getId(), identity2.getId(), identity3.getId()));
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(exampleAction);
		assertNotNull(processAction.getLongRunningTaskId());
		IdmLongRunningTaskDto longRunningTask = longRunningTaskService.get(processAction.getLongRunningTaskId());
		
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
		
		// check logs
		IdmLoggingEventFilter filter = new IdmLoggingEventFilter();
		filter.setText("Log identity with username: " + identity.getUsername());
		List<IdmLoggingEventDto> logs = loggingEventService.find(filter, null).getContent();
		assertEquals(1, logs.size());
	}
}
