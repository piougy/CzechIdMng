package eu.bcvsolutions.idm.core.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmScheduledTaskService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Processed tasks service test.
 * @author Jan Helbich
 *
 */
public class DefaultIdmProcessedTaskItemDtoServiceTest extends AbstractIntegrationTest {

	@Autowired
	private IdmProcessedTaskItemService service;

	@Autowired
	private IdmScheduledTaskService scheduledTaskService;

	@Autowired
	private IdmLongRunningTaskService longrunningService;
	
	@Test
	public void testCreateItem() {
		IdmScheduledTaskDto d = createSchedulableTask();
		IdmLongRunningTaskDto lrt = createLongRunningTask(d);
		IdmProcessedTaskItemDto item = getProcessedItem(lrt);
		//
		IdmProcessedTaskItemDto retrieved = service.get(service.saveInternal(item).getId());
		//
		assertNotNull(retrieved);
		assertEquals(item.getReferencedDtoType(), retrieved.getReferencedDtoType());
		assertEquals(item.getReferencedEntityId(), retrieved.getReferencedEntityId());
		assertEquals(item.getLongRunningTask(), retrieved.getLongRunningTask());
	}
	
	@Test
	public void testImmutable() {
		IdmScheduledTaskDto d = createSchedulableTask();
		IdmLongRunningTaskDto lrt = createLongRunningTask(d);
		IdmProcessedTaskItemDto item = getProcessedItem(lrt);
		IdmProcessedTaskItemDto saved = service.get(service.saveInternal(item).getId());
		// set fields to new value
		saved.setReferencedDtoType(IdmIdentityContractDto.class.getCanonicalName());
		saved.setReferencedEntityId(UUID.randomUUID());
		saved.setLongRunningTask(createLongRunningTask(d).getId());
		//
		IdmProcessedTaskItemDto retrieved = service.get(service.saveInternal(saved).getId());
		// fields must not update
		assertEquals(item.getLongRunningTask(), retrieved.getLongRunningTask());
		assertEquals(item.getReferencedDtoType(), retrieved.getReferencedDtoType());
		assertEquals(item.getReferencedEntityId(), retrieved.getReferencedEntityId());
		//
		assertNotEquals(item.getLongRunningTask(), saved.getLongRunningTask());
		assertNotEquals(item.getReferencedDtoType(), saved.getReferencedDtoType());
		assertNotEquals(item.getReferencedEntityId(), saved.getReferencedEntityId());
	}
	
	@Test
	public void testItemTypeReference() {
		IdmScheduledTaskDto d = createSchedulableTask();
		IdmLongRunningTaskDto lrt = createLongRunningTask(d);
		IdmProcessedTaskItemDto item = getProcessedItem(lrt);
		//
		try {
			item.setScheduledTaskQueueOwner(d.getId());
			service.get(service.saveInternal(item).getId());
			fail("Both log and queue association is forbidden.");
		} catch (CoreException e) {
			assertNotNull(e.getMessage());
		}
		//
		try {
			item.setScheduledTaskQueueOwner(null);
			item.setLongRunningTask(null);
			service.get(service.saveInternal(item).getId());
			fail("Empty log and queue association is forbidden.");
		} catch (CoreException e) {
			assertNotNull(e.getMessage());
		}
	}
	
	@Test
	public void testDeleteLogItemIntegrity() {
		IdmScheduledTaskDto d = createSchedulableTask();
		IdmLongRunningTaskDto lrt = createLongRunningTask(d);
		IdmProcessedTaskItemDto item = service.saveInternal(getProcessedItem(lrt));
		//
		longrunningService.deleteInternal(lrt);
		//
		assertNull(longrunningService.get(lrt.getId()));
		assertNull(service.get(item.getId()));
		assertNotNull(scheduledTaskService.get(d.getId()));
	}
	
	@Test
	public void testDeleteQueueItemIntegrity() {
		IdmScheduledTaskDto d = createSchedulableTask();
		IdmProcessedTaskItemDto item = service.saveInternal(getProcessedItem(d));
		//
		scheduledTaskService.deleteInternal(d);
		//
		assertNull(scheduledTaskService.get(d.getId()));
		assertNull(service.get(item.getId()));
	}

	private IdmProcessedTaskItemDto getProcessedItem(IdmLongRunningTaskDto lrt) {
		IdmProcessedTaskItemDto item = new IdmProcessedTaskItemDto();
		item.setReferencedDtoType(IdmIdentityDto.class.getCanonicalName());
		item.setReferencedEntityId(UUID.randomUUID());
		item.setLongRunningTask(lrt.getId());
		item.setOperationResult(new OperationResult.Builder(OperationState.EXECUTED).build());
		return item;
	}
	
	private IdmProcessedTaskItemDto getProcessedItem(IdmScheduledTaskDto d) {
		IdmProcessedTaskItemDto item = new IdmProcessedTaskItemDto();
		item.setReferencedDtoType(IdmIdentityDto.class.getCanonicalName());
		item.setReferencedEntityId(UUID.randomUUID());
		item.setScheduledTaskQueueOwner(d.getId());
		item.setOperationResult(new OperationResult.Builder(OperationState.EXECUTED).build());
		return item;
	}

	private IdmLongRunningTaskDto createLongRunningTask(IdmScheduledTaskDto d) {
		IdmLongRunningTaskDto lrt = new IdmLongRunningTaskDto();
		lrt.setTaskDescription("task description");
		lrt.setResult(new OperationResult.Builder(OperationState.CREATED).build());
		lrt.setInstanceId("test instance");
		lrt.setScheduledTask(d.getId());
		lrt.setTaskType(TestSchedulableTask.class.getCanonicalName());
		return longrunningService.saveInternal(lrt);
	}

	private IdmScheduledTaskDto createSchedulableTask() {
		IdmScheduledTaskDto d = new IdmScheduledTaskDto();
		d.setQuartzTaskName(UUID.randomUUID().toString());
		d.setDryRun(false);
		d = scheduledTaskService.saveInternal(d);
		return d;
	}

}
