package eu.bcvsolutions.idm.core.scheduler.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.PasswordGenerator;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractLongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.TestTaskExecutor;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Long running tasks filter test.
 * 
 * Lookout - @Transactional is not used - long running task is saved in new transaction (~log with exception, prevent to rollback).
 * 
 * TODO: move filter tests to rest test
 *
 * @author Marek Klement
 * @author Radek Tomiška
 */
public class DefaultIdmLongRunningTaskServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdmLongRunningTaskService service;
	@Autowired private IdmProcessedTaskItemService itemService;
	@Autowired private LongRunningTaskManager manager;
	@Autowired private AttachmentManager attachmentManager;
	
	@Test
	public void testReferentialIntegrity() {
		TestTaskExecutor taskExecutor = new TestTaskExecutor(); 
		IdmLongRunningTaskDto task = new IdmLongRunningTaskDto();
		task.setTaskType(taskExecutor.getClass().getCanonicalName());
		task.setTaskProperties(taskExecutor.getProperties());
		task.setTaskDescription(taskExecutor.getDescription());	
		task.setInstanceId("mock");
		task.setResult(new OperationResult.Builder(OperationState.NOT_EXECUTED).build());
		task = service.save(task);
		//
		IdmProcessedTaskItemDto processedItem = new IdmProcessedTaskItemDto();
		processedItem.setLongRunningTask(task.getId());
		processedItem.setReferencedDtoType(IdmIdentityDto.class.getCanonicalName());
		processedItem.setReferencedEntityId(UUID.randomUUID());
		processedItem.setOperationResult(new OperationResult.Builder(OperationState.NOT_EXECUTED).build());
		processedItem = itemService.save(processedItem);
		//
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setName("mock");
		attachment.setMimetype("text/plain");
		attachment.setInputData(IOUtils.toInputStream("mock content"));
		attachment = attachmentManager.saveAttachment(task, attachment);
		//
		Assert.assertNotNull(service.get(task));
		Assert.assertNotNull(itemService.get(processedItem));
		Assert.assertNotNull(attachmentManager.get(attachment));
		//
		service.delete(task);
		//
		Assert.assertNull(service.get(task));
		Assert.assertNull(itemService.get(processedItem));
		Assert.assertNull(attachmentManager.get(attachment));
	}
	
	@Test
	public void testDeleteRunningTask() {
		TestTaskExecutor taskExecutor = new TestTaskExecutor(); 
		IdmLongRunningTaskDto task = new IdmLongRunningTaskDto();
		task.setTaskType(taskExecutor.getClass().getCanonicalName());
		task.setTaskProperties(taskExecutor.getProperties());
		task.setTaskDescription(taskExecutor.getDescription());	
		task.setInstanceId("mock");
		task.setRunning(true);
		task.setResult(new OperationResult.Builder(OperationState.RUNNING).build());
		task = service.save(task);
		//
		try {
			service.delete(task);
			//
			fail();
		} catch (ResultCodeException ex) {
			// ok
			task.setRunning(false);
			task = service.save(task);
			// now can be deleted
			service.delete(task);
		}
	}

	@Test
	public void statefulFilterTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setFrom(now);
		String expectedResult = getHelper().createName();
		// set tasks
		LongRunningTaskExecutor<String> taskExecutor = new TestSimpleLongRunningTaskExecutor(expectedResult);
		assertNull(taskExecutor.getLongRunningTaskId());
		manager.executeSync(taskExecutor);
		IdmLongRunningTaskDto task1 = service.get(taskExecutor.getLongRunningTaskId());
		task1.setStateful(false);
		service.save(task1);
		//
		LongRunningTaskExecutor<String> taskExecutor2 = new TestSimpleLongRunningTaskExecutor(expectedResult);
		assertNull(taskExecutor2.getLongRunningTaskId());
		manager.executeSync(taskExecutor2);
		service.get(taskExecutor2.getLongRunningTaskId());
		//
		LongRunningTaskExecutor<String> taskExecutor3 = new TestSimpleLongRunningTaskExecutor(expectedResult);
		assertNull(taskExecutor3.getLongRunningTaskId());
		manager.executeSync(taskExecutor3);
		service.get(taskExecutor3.getLongRunningTaskId());
		// set filter
		filter.setTaskType(task1.getTaskType());
		filter.setStateful(true);
		Page<IdmLongRunningTaskDto> result = service.find(filter, null);
		assertEquals("Wrong StateFul true",2, result.getTotalElements());
		filter.setStateful(false);
		result = service.find(filter, null);
		assertEquals("Wrong StateFul false",1, result.getTotalElements());
	}

	@Test
	public void runningFilterTest() {
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		String expectedResult = getHelper().createName();
		// set tasks
		LongRunningTaskExecutor<String> taskExecutor = new TestSimpleLongRunningTaskExecutor(expectedResult);
		assertNull(taskExecutor.getLongRunningTaskId());
		manager.executeSync(taskExecutor);
		IdmLongRunningTaskDto task1 = service.get(taskExecutor.getLongRunningTaskId());
		task1.setRunning(true);
		service.save(task1);
		// set filter
		filter.setTaskType(task1.getTaskType());
		filter.setRunning(task1.isRunning());
		Page<IdmLongRunningTaskDto> result = service.find(filter, null);
		assertEquals("Wrong Running",task1.getId(), result.getContent().get(0).getId());
	}

	@Test
	public void typeFilterTest() {
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		String expectedResult = getHelper().createName();
		// set tasks
		LongRunningTaskExecutor<String> taskExecutor = new TestSimpleLongRunningTaskExecutor(expectedResult);
		assertNull(taskExecutor.getLongRunningTaskId());
		manager.executeSync(taskExecutor);
		IdmLongRunningTaskDto task1 = service.get(taskExecutor.getLongRunningTaskId());
		task1.setTaskType(getHelper().createName());
		service.save(task1);
		filter.setTaskType(task1.getTaskType());
		Page<IdmLongRunningTaskDto> result = service.find(filter, null);
		assertEquals("Wrong TaskType",task1.getId(), result.getContent().get(0).getId());
	}

	@Test
	public void textFilterTest(){
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		String expectedResult = getHelper().createName();
		String type1 = getHelper().createName();
		String type2 = getHelper().createName();
		// set tasks
		LongRunningTaskExecutor<String> taskExecutor = new TestSimpleLongRunningTaskExecutor(expectedResult);
		assertNull(taskExecutor.getLongRunningTaskId());
		manager.executeSync(taskExecutor);
		IdmLongRunningTaskDto task1 = service.get(taskExecutor.getLongRunningTaskId());
		task1.setTaskType(type1);
		service.save(task1);

		LongRunningTaskExecutor<String> taskExecutor2 = new TestSimpleLongRunningTaskExecutor(expectedResult);
		assertNull(taskExecutor2.getLongRunningTaskId());
		manager.executeSync(taskExecutor2);
		IdmLongRunningTaskDto task2 = service.get(taskExecutor2.getLongRunningTaskId());
		task2.setTaskType(type1);
		service.save(task2);

		LongRunningTaskExecutor<String> taskExecutor3 = new TestSimpleLongRunningTaskExecutor(expectedResult);
		assertNull(taskExecutor3.getLongRunningTaskId());
		manager.executeSync(taskExecutor3);
		IdmLongRunningTaskDto task3 = service.get(taskExecutor3.getLongRunningTaskId());
		task3.setTaskType(type2);
		service.save(task3);

		LongRunningTaskExecutor<String> taskExecutor4 = new TestSimpleLongRunningTaskExecutor(expectedResult);
		assertNull(taskExecutor4.getLongRunningTaskId());
		manager.executeSync(taskExecutor4);
		IdmLongRunningTaskDto task4 = service.get(taskExecutor4.getLongRunningTaskId());
		task4.setTaskDescription(type1);
		service.save(task4);
		// set filter
		filter.setText(type1);
		Page<IdmLongRunningTaskDto> result = service.find(filter, null);
		assertEquals("Wrong Text Type",3, result.getTotalElements());
		assertEquals("Wrong Text Type",true, result.getContent().contains(task4));
		assertEquals("Wrong Text Description",true, result.getContent().contains(task1));
	}

	@Test
	@Transactional
	public void datesFilterTest() {
		// set tasks
		LongRunningTaskExecutor<String> taskExecutor = new TestSimpleLongRunningTaskExecutor("one");
		assertNull(taskExecutor.getLongRunningTaskId());
		manager.executeSync(taskExecutor);
		IdmLongRunningTaskDto task1 = service.get(taskExecutor.getLongRunningTaskId());

		getHelper().waitForResult(null, 1, 1); // created is filled automatically
		
		LongRunningTaskExecutor<String> taskExecutor2 = new TestSimpleLongRunningTaskExecutor("two");
		assertNull(taskExecutor2.getLongRunningTaskId());
		manager.executeSync(taskExecutor2);
		IdmLongRunningTaskDto task2 = service.get(taskExecutor2.getLongRunningTaskId());
		//
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setTaskType(TestSimpleLongRunningTaskExecutor.class.getCanonicalName());
		//
		filter.setFrom(task1.getCreated());
		Page<IdmLongRunningTaskDto> result = service.find(filter, null);
		assertEquals("Wrong From Date", 2, result.getTotalElements());

		filter.setTill(task2.getCreated());
		result = service.find(filter, null);
		assertEquals("Wrong Till Date", 2, result.getTotalElements());
	}

	@Test
	public void operationStateFilterTest(){
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		String expectedResult = getHelper().createName();
		// set tasks
		LongRunningTaskExecutor<String> taskExecutor = new TestSimpleLongRunningTaskExecutor(expectedResult);
		assertNull(taskExecutor.getLongRunningTaskId());
		manager.executeSync(taskExecutor);
		IdmLongRunningTaskDto task1 = service.get(taskExecutor.getLongRunningTaskId());
		// set filter
		filter.setOperationState(task1.getResultState());
		Page<IdmLongRunningTaskDto> result = service.find(filter, null);
		assertEquals("Wrong operationState id",true, result.getContent().contains(task1));
	}

	@Test
	public void descriptionLengthTest2000() {
		IdmLongRunningTaskDto task = new IdmLongRunningTaskDto();
		task.setTaskType(getHelper().createName());
		task.setInstanceId(getHelper().createName());
		task.setResult(new OperationResult.Builder(OperationState.EXECUTED).build());
		
		// this must past
		PasswordGenerator generator = new PasswordGenerator();
		String random = generator.generateRandom(2000, 2000, null, null, null, null);
		assertEquals(2000, random.length());
		task.setTaskDescription(random);

		IdmLongRunningTaskDto newSaved = service.save(task);
		assertNotNull(newSaved);
		assertEquals(random, newSaved.getTaskDescription());
		assertEquals(2000, newSaved.getTaskDescription().length());
	}

	@Test
	public void descriptionLengthTest2050() {
		IdmLongRunningTaskDto task = new IdmLongRunningTaskDto();
		task.setTaskType(getHelper().createName());
		task.setInstanceId(getHelper().createName());
		task.setResult(new OperationResult.Builder(OperationState.EXECUTED).build());
		
		// this must also past, but description will be cutoff
		PasswordGenerator generator = new PasswordGenerator();
		String random = generator.generateRandom(2001, 2050, null, null, null, null);
		if (random.length() <= 2000) {
			fail();
		}
		task.setTaskDescription(random);

		IdmLongRunningTaskDto newSaved = service.save(task);
		assertNotNull(newSaved);
		assertNotEquals(random, newSaved.getTaskDescription());
		assertEquals(2000, newSaved.getTaskDescription().length());
		assertTrue(newSaved.getTaskDescription().endsWith("..."));
	}
	
	private class TestSimpleLongRunningTaskExecutor extends AbstractLongRunningTaskExecutor<String> {

		private final String result;

		public TestSimpleLongRunningTaskExecutor(String result) {
			this.result = result;
		}

		@Override
		public String getDescription() {
			return result;
		}

		@Override
		public String process() {
			return result;
		}

	}
}
