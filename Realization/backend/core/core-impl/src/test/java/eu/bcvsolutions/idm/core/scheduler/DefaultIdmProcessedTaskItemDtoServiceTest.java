package eu.bcvsolutions.idm.core.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.entity.OperationResult_;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmProcessedTaskItemFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmScheduledTaskService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

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

	@Autowired
	private TestHelper helper;

	@Test
	public void testCreateItem() {
		IdmScheduledTaskDto d = helper.createSchedulableTask();
		IdmLongRunningTaskDto lrt = this.createLongRunningTask(d);
		IdmProcessedTaskItemDto item = helper.getProcessedItem(lrt);
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
		IdmScheduledTaskDto d = helper.createSchedulableTask();
		IdmLongRunningTaskDto lrt = this.createLongRunningTask(d);
		IdmProcessedTaskItemDto item = helper.getProcessedItem(lrt);
		IdmProcessedTaskItemDto saved = service.get(service.saveInternal(item).getId());
		// set fields to new value
		saved.setReferencedDtoType(IdmIdentityContractDto.class.getCanonicalName());
		saved.setReferencedEntityId(UUID.randomUUID());
		saved.setLongRunningTask(this.createLongRunningTask(d).getId());
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
		IdmScheduledTaskDto d = helper.createSchedulableTask();
		IdmLongRunningTaskDto lrt = this.createLongRunningTask(d);
		IdmProcessedTaskItemDto item = helper.getProcessedItem(lrt);
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
		IdmScheduledTaskDto d = helper.createSchedulableTask();
		IdmLongRunningTaskDto lrt = this.createLongRunningTask(d);
		IdmProcessedTaskItemDto item = service.saveInternal(helper.getProcessedItem(lrt));
		//
		longrunningService.deleteInternal(lrt);
		//
		assertNull(longrunningService.get(lrt.getId()));
		assertNull(service.get(item.getId()));
		assertNotNull(scheduledTaskService.get(d.getId()));
	}

	@Test
	public void testDeleteQueueItemIntegrity() {
		IdmScheduledTaskDto d = helper.createSchedulableTask();
		IdmProcessedTaskItemDto item = service.saveInternal(helper.getProcessedItem(d));
		//
		scheduledTaskService.deleteInternal(d);
		//
		assertNull(scheduledTaskService.get(d.getId()));
		assertNull(service.get(item.getId()));
	}

	@Test
	public void textFilter(){
		// find number of all processed items in old tests
		IdmProcessedTaskItemFilter filter = new IdmProcessedTaskItemFilter();
		filter.setText(IdmIdentityDto.class.getCanonicalName());
		Page<IdmProcessedTaskItemDto> result = service.find(filter,null);
		long count = result.getTotalElements();
		//
		IdmScheduledTaskDto d = helper.createSchedulableTask();
		IdmProcessedTaskItemDto item = service.saveInternal(helper.getProcessedItem(d));
		IdmProcessedTaskItemDto item2 = service.saveInternal(helper.getProcessedItem(d));
		//
		filter.setText(item.getReferencedDtoType());
		result = service.find(filter,null);
		assertEquals("Wrong number of items!",2+count,result.getTotalElements());
		assertTrue(result.getContent().contains(item));
		assertTrue(result.getContent().contains(item2));
	}

	@Test
	public void scheduledTaskIdFilter(){
		IdmScheduledTaskDto d = helper.createSchedulableTask();
		IdmProcessedTaskItemDto item = service.saveInternal(helper.getProcessedItem(d));
		IdmProcessedTaskItemDto item2 = service.saveInternal(helper.getProcessedItem(d));
		//
		IdmProcessedTaskItemFilter filter = new IdmProcessedTaskItemFilter();
		filter.setScheduledTaskId(d.getId());
		Page<IdmProcessedTaskItemDto> result = service.find(filter,null);
		assertEquals("Wrong number of items!",2,result.getTotalElements());
		assertTrue(result.getContent().contains(item));
		assertTrue(result.getContent().contains(item2));
	}

	@Test
	public void datesFilter(){
		IdmScheduledTaskDto d = helper.createSchedulableTask();
		IdmProcessedTaskItemDto item = service.saveInternal(helper.getProcessedItem(d));
		IdmProcessedTaskItemDto item2 = helper.getProcessedItem(d);
		item2.setCreated(item.getCreated());
		item2 = service.saveInternal(item2);
		//
		IdmProcessedTaskItemDto item3 = service.saveInternal(helper.getProcessedItem(d));
		//
		IdmProcessedTaskItemFilter filter = new IdmProcessedTaskItemFilter();
		filter.setFrom(item.getCreated());
		Page<IdmProcessedTaskItemDto> result = service.find(filter,null);
		assertTrue("#1",result.getContent().contains(item));
		assertTrue("#2",result.getContent().contains(item2));
		filter.setFrom(null);
		filter.setTill(item3.getCreated());
		result = service.find(filter,null);
		assertTrue("#4",result.getContent().contains(item3));
	}

	@Test
	public void referencedEntityIdFilter(){
		IdmScheduledTaskDto d = helper.createSchedulableTask();
		IdmProcessedTaskItemDto item = service.saveInternal(helper.getProcessedItem(d));
		IdmProcessedTaskItemDto item2 = service.saveInternal(helper.getProcessedItem(d));
		//
		IdmProcessedTaskItemFilter filter = new IdmProcessedTaskItemFilter();
		filter.setReferencedEntityId(item.getReferencedEntityId());
		Page<IdmProcessedTaskItemDto> result = service.find(filter,null);
		assertEquals("Wrong number of items!",1,result.getTotalElements());
		assertTrue(result.getContent().contains(item));
		assertFalse(result.getContent().contains(item2));
		//
		filter.setReferencedEntityId(item2.getReferencedEntityId());
		result = service.find(filter,null);
		assertEquals("Wrong number of items!",1,result.getTotalElements());
		assertTrue(result.getContent().contains(item2));
		assertFalse(result.getContent().contains(item));
	}

	@Test
	public void getLongRunningTaskIdFilter(){
		IdmScheduledTaskDto d = helper.createSchedulableTask();
		IdmLongRunningTaskDto lrt = this.createLongRunningTask(d);
		IdmLongRunningTaskDto lrt2 = this.createLongRunningTask(d);
		//
		IdmProcessedTaskItemDto item = service.saveInternal(helper.getProcessedItem(lrt));
		IdmProcessedTaskItemDto item2 = service.saveInternal(helper.getProcessedItem(lrt));
		IdmProcessedTaskItemDto item3 = service.saveInternal(helper.getProcessedItem(lrt2));
		//
		IdmProcessedTaskItemFilter filter = new IdmProcessedTaskItemFilter();
		filter.setLongRunningTaskId(lrt.getId());
		Page<IdmProcessedTaskItemDto> result = service.find(filter,null);
		assertEquals("Wrong number of items!",2,result.getTotalElements());
		assertTrue(result.getContent().contains(item));
		assertTrue(result.getContent().contains(item2));
		assertFalse(result.getContent().contains(item3));
	}

	@Test
	public void getOperationState(){
		IdmScheduledTaskDto d = helper.createSchedulableTask();
		//
		IdmProcessedTaskItemDto item = service.saveInternal(helper.getProcessedItem(d));
		IdmProcessedTaskItemDto item2 = service.saveInternal(helper.getProcessedItem(d));
		IdmProcessedTaskItemDto item3 = service.saveInternal(helper.getProcessedItem(d,OperationState.CANCELED));
		//
		IdmProcessedTaskItemFilter filter = new IdmProcessedTaskItemFilter();
		filter.setOperationState(item.getOperationResult().getState());
		Page<IdmProcessedTaskItemDto> result = service.find(filter,null);
		assertTrue(result.getContent().contains(item));
		assertTrue(result.getContent().contains(item2));
		assertFalse(result.getContent().contains(item3));
	}

	public IdmLongRunningTaskDto createLongRunningTask(IdmScheduledTaskDto d) {
		IdmLongRunningTaskDto lrt = new IdmLongRunningTaskDto();
		lrt.setTaskDescription("task description");
		lrt.setResult(new OperationResult.Builder(OperationState.CREATED).build());
		lrt.setInstanceId("test instance");
		lrt.setScheduledTask(d.getId());
		lrt.setTaskType(TestSchedulableTask.class.getCanonicalName());
		return longrunningService.saveInternal(lrt);
	}
}
