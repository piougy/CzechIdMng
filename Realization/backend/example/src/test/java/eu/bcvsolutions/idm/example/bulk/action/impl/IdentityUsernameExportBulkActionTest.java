package eu.bcvsolutions.idm.example.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Test for bulk action {@link IdentityUsernameExportBulkAction}
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdentityUsernameExportBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private AttachmentManager attachmentManager;
	@Autowired
	private LongRunningTaskManager longRunningTaskManager;

	@Test
	public void processBulkAction() throws IOException {
		IdmBulkActionDto exampleAction = findBulkAction(IdmIdentity.class, IdentityUsernameExportBulkAction.BULK_ACTION_NAME);
		
		assertNotNull(exampleAction);
		
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

		OperationResult result = longRunningTask.getResult();
		ResultModel model = result.getModel();
		assertNotNull(model);
		assertEquals(206, model.getStatusCode());
		assertEquals(CoreResultCode.LONG_RUNNING_TASK_PARTITIAL_DOWNLOAD.getCode(), model.getStatusEnum());

		Map<String, Object> parameters = model.getParameters();
		assertNotNull(parameters);
		Object object = parameters.get(AttachableEntity.PARAMETER_ATTACHMENT_ID);
		assertNotNull(object);
		UUID fromString = UUID.fromString(object.toString());
		assertNotNull(fromString);

		IdmAttachmentDto attachmentDto = attachmentManager.get(fromString);
		assertNotNull(attachmentDto);
		assertEquals(longRunningTask.getId(), attachmentDto.getOwnerId());

		IdmAttachmentDto attachmentForLongRunningTask = longRunningTaskManager.getAttachment(longRunningTask.getId(), attachmentDto.getId());
		assertNotNull(attachmentForLongRunningTask);
		assertEquals(attachmentDto.getId(), attachmentForLongRunningTask.getId());
		
		InputStream is = attachmentManager.getAttachmentData(attachmentDto.getId());
		try {
			String string = IOUtils.toString(is);
			assertTrue(string.contains(identity.getUsername()));
			assertTrue(string.contains(identity2.getUsername()));
			assertTrue(string.contains(identity3.getUsername()));
	
			assertTrue(string.contains(IdmIdentity_.username.getName().toUpperCase()));
			assertTrue(string.contains(IdmIdentity_.externalCode.getName().toUpperCase()));
			assertTrue(string.contains(IdmIdentity_.state.getName().toUpperCase()));
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	@Test
	public void checkPermission() {
		IdmBulkActionDto exampleAction = findBulkAction(IdmIdentity.class, IdentityUsernameExportBulkAction.BULK_ACTION_NAME);
		
		assertNotNull(exampleAction);
		
		IdmIdentityDto identity = getHelper().createIdentity();
		
		exampleAction.setIdentifiers(Sets.newHashSet(identity.getId()));
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(exampleAction);
		assertNotNull(processAction.getLongRunningTaskId());
		IdmLongRunningTaskDto longRunningTask = longRunningTaskService.get(processAction.getLongRunningTaskId());

		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(CoreGroupPermission.IDENTITY, CoreGroupPermission.SCHEDULER);
		loginAsNoAdmin(adminIdentity.getUsername());

		processAction = bulkActionManager.processAction(exampleAction);
		assertNotNull(processAction.getLongRunningTaskId());
		IdmLongRunningTaskDto longRunningTask2 = longRunningTaskService.get(processAction.getLongRunningTaskId());

		assertFalse(longRunningTask.isRunning());
		assertFalse(longRunningTask2.isRunning());

		Assert.notNull(longRunningTask);
		Assert.notNull(longRunningTask2);

		UUID attachmentOneId = UUID.fromString(longRunningTask.getResult().getModel().getParameters().get(AttachableEntity.PARAMETER_ATTACHMENT_ID).toString());
		try {
			longRunningTaskManager.getAttachment(longRunningTask2.getId(), attachmentOneId, IdmBasePermission.READ);
			fail();
		} catch (ForbiddenEntityException e) {
			// Correct behavior
		} catch (Exception e) {
			fail();
		}

		try {
			longRunningTaskManager.getAttachment(UUID.randomUUID(), attachmentOneId, IdmBasePermission.READ);
			fail();
		} catch (EntityNotFoundException e) {
			// Correct behavior
		} catch (Exception e) {
			fail();
		}

		UUID attachmentTwoId = UUID.fromString(longRunningTask2.getResult().getModel().getParameters().get(AttachableEntity.PARAMETER_ATTACHMENT_ID).toString());
		IdmAttachmentDto attachmentDto = longRunningTaskManager.getAttachment(longRunningTask2.getId(), attachmentTwoId);
		assertNotNull(attachmentDto);
	}
}
