package eu.bcvsolutions.idm.core.scheduler.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.bulk.action.impl.IdentityDeleteBulkAction;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmProcessedTaskItemFilter;
import eu.bcvsolutions.idm.core.scheduler.api.exception.ConcurrentExecutionException;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractLongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.exception.TaskNotRecoverableException;
import eu.bcvsolutions.idm.core.scheduler.task.impl.TestTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Long running tasks test.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultLongRunningTaskManagerIntegrationTest extends AbstractBulkActionTest {

	@Autowired private ApplicationContext context;
	@Autowired private IdmLongRunningTaskService service;
	@Autowired private ConfigurationService configurationService;
	@Autowired private IdmProcessedTaskItemService itemService;
	@Autowired private IdmIdentityService identityService;
	//
	private DefaultLongRunningTaskManager manager;
	
	@Before
	public void init() {
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultLongRunningTaskManager.class);
		getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, true);
	}
	
	@After
	public void after() {
		getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, false);
	}
	
	@Test
	public void testRunSimpleTaskAsync() throws InterruptedException, ExecutionException {
		String result = "TEST_SUCCESS_01";
		LongRunningTaskExecutor<String> taskExecutor = new TestSimpleLongRunningTaskExecutor(result);
		assertNull(taskExecutor.getLongRunningTaskId());
		//
		LongRunningFutureTask<String> futureTask = manager.execute(taskExecutor);
		//
		IdmLongRunningTaskDto longRunningTask = service.get(taskExecutor.getLongRunningTaskId());
		assertNotNull(longRunningTask);
		assertEquals(result, longRunningTask.getTaskDescription());
		assertEquals(taskExecutor.getClass().getCanonicalName(), longRunningTask.getTaskType());
		assertEquals(configurationService.getInstanceId(), longRunningTask.getInstanceId());
		//
		assertEquals(result, futureTask.getFutureTask().get());
		//
		longRunningTask = service.get(longRunningTask.getId());
		assertEquals(OperationState.EXECUTED, longRunningTask.getResult().getState());
	}
	
	@Test
	public void testRunSimpleTaskSync() throws InterruptedException, ExecutionException {
		String expectedResult = "TEST_SUCCESS_01_S";
		LongRunningTaskExecutor<String> taskExecutor = new TestSimpleLongRunningTaskExecutor(expectedResult);
		assertNull(taskExecutor.getLongRunningTaskId());
		//
		String result = manager.executeSync(taskExecutor);
		//
		IdmLongRunningTaskDto longRunningTask = service.get(taskExecutor.getLongRunningTaskId());
		assertNotNull(longRunningTask);
		assertEquals(expectedResult, longRunningTask.getTaskDescription());
		assertEquals(taskExecutor.getClass().getCanonicalName(), longRunningTask.getTaskType());
		assertEquals(configurationService.getInstanceId(), longRunningTask.getInstanceId());
		//
		assertEquals(expectedResult, result);
		//
		longRunningTask = service.get(longRunningTask.getId());
		assertEquals(OperationState.EXECUTED, longRunningTask.getResult().getState());
	}
	
	@Test
	public void testRunCountableTask() throws InterruptedException, ExecutionException {
		String result = "TEST_SUCCESS_02";
		Long count = 10L;
		LongRunningTaskExecutor<String> taskExecutor = new TestCountableLongRunningTaskExecutor(result, count);
		//
		LongRunningFutureTask<String> futureTask = manager.execute(taskExecutor);
		//
		assertEquals(result, futureTask.getFutureTask().get());
		//
		IdmLongRunningTaskDto longRunningTask = service.get(taskExecutor.getLongRunningTaskId());
		assertEquals(OperationState.EXECUTED, longRunningTask.getResult().getState());
		assertEquals(count, longRunningTask.getCount());
		assertEquals(count, longRunningTask.getCounter());
	}
	
	// TODO: locking - start event override canceled state
	// @Test
	public void testCancelTaskBeforeStart() throws InterruptedException, ExecutionException {
		String result = "TEST_SUCCESS_03";
		Long count = 50L;
		LongRunningTaskExecutor<String> taskExecutor = new TestStopableLongRunningTaskExecutor(result, count);
		//
		LongRunningFutureTask<String> futureTask = manager.execute(taskExecutor);
		manager.cancel(taskExecutor.getLongRunningTaskId());
		//	
		assertEquals(result, futureTask.getFutureTask().get());
		//
		IdmLongRunningTaskDto longRunningTask = service.get(taskExecutor.getLongRunningTaskId());
		assertEquals(OperationState.CANCELED, longRunningTask.getResult().getState());
		assertEquals(count, longRunningTask.getCount());
		assertNotEquals(count, longRunningTask.getCounter());
	}
	
	@Test
	public void testCancelRunningTask() throws InterruptedException, ExecutionException {
		String result = "TEST_SUCCESS_04";
		Long count = 30L;
		LongRunningTaskExecutor<String> taskExecutor = new TestStopableLongRunningTaskExecutor(result, count);
		//
		LongRunningFutureTask<String> futureTask = manager.execute(taskExecutor);
		//
		Function<String, Boolean> continueFunction = res -> {
			IdmLongRunningTaskDto longRunningTask = manager.getLongRunningTask(futureTask);
			return !longRunningTask.isRunning() || !longRunningTask.getResultState().isRunnable();
		};
		getHelper().waitForResult(continueFunction);
		//
		manager.cancel(taskExecutor.getLongRunningTaskId());
		//
		assertEquals(result, futureTask.getFutureTask().get());
		//
		IdmLongRunningTaskDto longRunningTask = service.get(taskExecutor.getLongRunningTaskId());
		assertEquals(OperationState.CANCELED, longRunningTask.getResult().getState());
		assertEquals(count, longRunningTask.getCount());
		assertNotEquals(count, longRunningTask.getCounter());
	}
	
	@Test
	public void testInterruptRunningTask() throws InterruptedException, ExecutionException {
		String result = "TEST_SUCCESS_05";
		Long count = 100L;
		LongRunningTaskExecutor<String> taskExecutor = new TestStopableLongRunningTaskExecutor(result, count);
		//
		manager.execute(taskExecutor);
		IdmLongRunningTaskDto longRunningTask = manager.getLongRunningTask(taskExecutor);
		// task has to be marked as running immediately
		Assert.assertEquals(OperationState.RUNNING, longRunningTask.getResult().getState());
		//
		Function<String, Boolean> continueFunction = res -> {
			return !manager.getLongRunningTask(taskExecutor).isRunning();
		};
		getHelper().waitForResult(continueFunction);
		//
		longRunningTask = service.get(taskExecutor.getLongRunningTaskId());
		assertEquals(OperationState.RUNNING, longRunningTask.getResult().getState());
		assertEquals(count, longRunningTask.getCount());
		assertTrue(longRunningTask.isRunning());
		//
		assertTrue(manager.interrupt(taskExecutor.getLongRunningTaskId()));
		//
		longRunningTask = service.get(taskExecutor.getLongRunningTaskId());
		assertNotEquals(OperationState.RUNNING, longRunningTask.getResult().getState());
		assertEquals(count, longRunningTask.getCount());
		assertNotEquals(count, longRunningTask.getCounter());
		assertFalse(longRunningTask.isRunning());
	}
	
	@Test
	public void testCancelPreviouslyRunnedTask() {
		IdmLongRunningTaskDto taskOne = new IdmLongRunningTaskDto();
		taskOne.setResult(new OperationResult.Builder(OperationState.RUNNING).build());
		taskOne.setInstanceId(configurationService.getInstanceId());
		taskOne.setTaskType(TestSimpleLongRunningTaskExecutor.class.getCanonicalName());
		taskOne.setRunning(true);
		taskOne = service.save(taskOne);
		//
		IdmLongRunningTaskDto taskTwo = new IdmLongRunningTaskDto();
		taskTwo.setResult(new OperationResult.Builder(OperationState.RUNNING).build());
		taskTwo.setInstanceId("different-instance");
		taskTwo.setTaskType(TestSimpleLongRunningTaskExecutor.class.getCanonicalName());
		taskTwo.setRunning(true);
		taskTwo = service.save(taskTwo);
		//
		manager.init();
		//
		taskOne = service.get(taskOne.getId());
		taskTwo = service.get(taskTwo.getId());
		//
		assertEquals(OperationState.CANCELED, taskOne.getResultState());
		assertFalse(taskOne.isRunning());
		assertEquals(OperationState.RUNNING, taskTwo.getResultState());
		assertTrue(taskTwo.isRunning());
	}
	
	@Test(expected = ConcurrentExecutionException.class)
	public void testDisallowConcurrentExecution() {
		TestTaskExecutor executorOne = new TestTaskExecutor();
		executorOne.setCount(50L);
		LongRunningFutureTask<Boolean> longRunningFutureTask = manager.execute(executorOne);
		getHelper().waitForResult(res -> {
			return !service.get(longRunningFutureTask.getExecutor().getLongRunningTaskId()).isRunning();
		});		
		TestTaskExecutor executorTwo = new TestTaskExecutor();
		executorTwo.setCount(10L);
		manager.executeSync(executorTwo);
	}
	
	@Test(expected = AcceptedException.class)
	public void testCheckConcurrentExecution() {
		TestCheckConcurrentTaskOne executorOne = new TestCheckConcurrentTaskOne();
		executorOne.setSleep(1500L);
		LongRunningFutureTask<String> longRunningFutureTask = manager.execute(executorOne);
		getHelper().waitForResult(res -> {
			return !service.get(longRunningFutureTask.getExecutor().getLongRunningTaskId()).isRunning();
		}, 150, 100);		
		TestCheckConcurrentTaskOne executorTwo = new TestCheckConcurrentTaskOne();
		manager.executeSync(executorTwo);
	}
	
	@Test
	public void testCheckConcurrentExecutionProcessCreated() {
		getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, false);
		TestCheckConcurrentTaskTwo executorOne = new TestCheckConcurrentTaskTwo();
		executorOne.setSleep(500L);
		//
		manager.resolveLongRunningTask(executorOne, null, null);
		UUID taskOneId = executorOne.getLongRunningTaskId();
		executorOne.setLongRunningTaskId(null);
		manager.resolveLongRunningTask(executorOne, null, OperationState.CREATED);
		UUID taskOneTwo = executorOne.getLongRunningTaskId();
		executorOne.setLongRunningTaskId(null);
		manager.resolveLongRunningTask(executorOne, null, OperationState.CREATED);
		UUID taskOneThree = executorOne.getLongRunningTaskId();
		TestCheckOtherConcurrentTask executorOther = new TestCheckOtherConcurrentTask();
		manager.resolveLongRunningTask(executorOther, null, null);
		UUID taskOtherOne = executorOther.getLongRunningTaskId();
		executorOther.setLongRunningTaskId(null);
		manager.resolveLongRunningTask(executorOther, null, null);
		UUID taskOtherTwo = executorOther.getLongRunningTaskId();
		//
		manager.processCreated();
		//
		Assert.assertEquals(OperationState.EXECUTED, manager.getLongRunningTask(taskOneId).getResultState());
		Assert.assertEquals(OperationState.CREATED, manager.getLongRunningTask(taskOneTwo).getResultState());
		Assert.assertEquals(OperationState.CREATED, manager.getLongRunningTask(taskOneThree).getResultState());
		Assert.assertEquals(OperationState.CREATED, manager.getLongRunningTask(taskOtherOne).getResultState());
		Assert.assertEquals(OperationState.CREATED, manager.getLongRunningTask(taskOtherTwo).getResultState());
		//
		manager.processCreated();
		//
		Assert.assertEquals(OperationState.EXECUTED, manager.getLongRunningTask(taskOneId).getResultState());
		Assert.assertEquals(OperationState.EXECUTED, manager.getLongRunningTask(taskOneTwo).getResultState());
		Assert.assertEquals(OperationState.CREATED, manager.getLongRunningTask(taskOneThree).getResultState());
		Assert.assertEquals(OperationState.CREATED, manager.getLongRunningTask(taskOtherOne).getResultState());
		Assert.assertEquals(OperationState.CREATED, manager.getLongRunningTask(taskOtherTwo).getResultState());
		//
		manager.processCreated();
		//
		Assert.assertEquals(OperationState.EXECUTED, manager.getLongRunningTask(taskOneId).getResultState());
		Assert.assertEquals(OperationState.EXECUTED, manager.getLongRunningTask(taskOneTwo).getResultState());
		Assert.assertEquals(OperationState.EXECUTED, manager.getLongRunningTask(taskOneThree).getResultState());
		Assert.assertEquals(OperationState.CREATED, manager.getLongRunningTask(taskOtherOne).getResultState());
		Assert.assertEquals(OperationState.CREATED, manager.getLongRunningTask(taskOtherTwo).getResultState());
		//
		manager.processCreated();
		//
		Assert.assertEquals(OperationState.EXECUTED, manager.getLongRunningTask(taskOneId).getResultState());
		Assert.assertEquals(OperationState.EXECUTED, manager.getLongRunningTask(taskOneTwo).getResultState());
		Assert.assertEquals(OperationState.EXECUTED, manager.getLongRunningTask(taskOneThree).getResultState());
		Assert.assertEquals(OperationState.EXECUTED, manager.getLongRunningTask(taskOtherOne).getResultState());
		Assert.assertEquals(OperationState.EXECUTED, manager.getLongRunningTask(taskOtherTwo).getResultState());
	}
	
	@Test
	public void testCheckLogs() throws InterruptedException, ExecutionException {
		IdmIdentityDto identity1 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity2 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity3 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity4 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity5 = getHelper().createIdentity((GuardedString) null);
		//
		TestLogItemLongRunningTaskExecutor taskExecutor = new TestLogItemLongRunningTaskExecutor();
		taskExecutor.addIdentityToProcess(identity1, identity2, identity3, identity4, identity5);
		taskExecutor.addRemovedIdentity(identity2, identity5);
		assertNull(taskExecutor.getLongRunningTaskId());
		//
		LongRunningFutureTask<Boolean> futureTask = manager.execute(taskExecutor);
		//
		IdmLongRunningTaskDto longRunningTask = service.get(taskExecutor.getLongRunningTaskId());
		assertNotNull(longRunningTask);
		assertEquals(taskExecutor.getClass().getCanonicalName(), longRunningTask.getTaskType());
		assertEquals(configurationService.getInstanceId(), longRunningTask.getInstanceId());
		//
		assertEquals(Boolean.TRUE, futureTask.getFutureTask().get());
		List<IdmProcessedTaskItemDto> content = itemService.findLogItems(longRunningTask, null).getContent();
		//
		longRunningTask = service.get(longRunningTask.getId());
		assertEquals(OperationState.EXECUTED, longRunningTask.getResult().getState());
		//
		assertEquals(5, content.size());
		//
		IdmProcessedTaskItemFilter filter = new IdmProcessedTaskItemFilter();
		filter.setLongRunningTaskId(taskExecutor.getLongRunningTaskId());
		filter.setOperationState(OperationState.EXECUTED);
		content = itemService.find(filter, null).getContent();
		assertEquals(3, content.size());
		//
		filter.setOperationState(OperationState.NOT_EXECUTED);
		content = itemService.find(filter, null).getContent();
		assertEquals(2, content.size());
		//
		content = itemService.findLogItems(longRunningTask, null).getContent();
		Set<UUID> entityIdsList = content.stream().map(IdmProcessedTaskItemDto::getReferencedEntityId).collect(Collectors.toSet());
		assertEquals(5, entityIdsList.size());
		assertTrue(entityIdsList.contains(identity1.getId()));
		assertTrue(entityIdsList.contains(identity2.getId()));
		assertTrue(entityIdsList.contains(identity3.getId()));
		assertTrue(entityIdsList.contains(identity4.getId()));
		assertTrue(entityIdsList.contains(identity5.getId()));
	}
	
	@Test
	public void testCheckDisableLogs() throws InterruptedException, ExecutionException {
		IdmIdentityDto identity1 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity2 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity3 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity4 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity5 = getHelper().createIdentity((GuardedString) null);
		//
		TestLogItemLongRunningTaskExecutor taskExecutor = new TestLogItemLongRunningTaskExecutor();
		taskExecutor.addIdentityToProcess(identity1, identity2, identity3, identity4, identity5);
		taskExecutor.addRemovedIdentity(identity2, identity5);
		taskExecutor.setLog(false);
		assertNull(taskExecutor.getLongRunningTaskId());
		//
		LongRunningFutureTask<Boolean> futureTask = manager.execute(taskExecutor);
		//
		IdmLongRunningTaskDto longRunningTask = service.get(taskExecutor.getLongRunningTaskId());
		assertNotNull(longRunningTask);
		assertEquals(taskExecutor.getClass().getCanonicalName(), longRunningTask.getTaskType());
		assertEquals(configurationService.getInstanceId(), longRunningTask.getInstanceId());
		//
		assertEquals(Boolean.TRUE, futureTask.getFutureTask().get());
		List<IdmProcessedTaskItemDto> content = itemService.findLogItems(longRunningTask, null).getContent();
		//
		longRunningTask = service.get(longRunningTask.getId());
		assertEquals(OperationState.EXECUTED, longRunningTask.getResult().getState());
		//
		assertEquals(0, content.size());
	}
	
	@Test
	public void testProcessCreatedLrtUnderNewTransactionId() {
		TestRegistrableSchedulableTask executorOne = new TestRegistrableSchedulableTask();
		executorOne.setDescription(getHelper().createName());
		executorOne.setCount(1L);
		//
		executorOne.setLongRunningTaskId(null);
		manager.resolveLongRunningTask(executorOne, null, null);
		executorOne.setLongRunningTaskId(null);
		manager.resolveLongRunningTask(executorOne, null, OperationState.CREATED);
		executorOne.setLongRunningTaskId(null);
		manager.resolveLongRunningTask(executorOne, null, OperationState.CREATED);
		//
		manager.processCreated();
		//
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setTaskType(AutowireHelper.getTargetType(executorOne));
		filter.setText(executorOne.getDescription());
		filter.setOperationState(OperationState.EXECUTED);
		getHelper().waitForResult(res -> {
			return service.find(filter, null).getContent().size() != 3;
		}, 500, 20);
		//
		List<IdmLongRunningTaskDto> ltrs = manager.findLongRunningTasks(filter, null).getContent();
		//
		Assert.assertEquals(3, ltrs.size());
		Assert.assertNotEquals(ltrs.get(0).getTransactionId(), ltrs.get(1).getTransactionId());
		Assert.assertNotEquals(ltrs.get(0).getTransactionId(), ltrs.get(2).getTransactionId());
		Assert.assertNotEquals(ltrs.get(1).getTransactionId(), ltrs.get(2).getTransactionId());
	}
	
	@Test
	public void testRecoverableTask() {
		getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, false);
		//
		try {
			TestTaskExecutor executorOne = new TestTaskExecutor();
			executorOne.setDescription(getHelper().createName());
			executorOne.setCount(1L);
			// first process
			manager.execute(executorOne);
			//
			IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
			filter.setTaskType(TestTaskExecutor.class.getCanonicalName());
			filter.setText(executorOne.getDescription());
			filter.setOperationState(OperationState.EXECUTED);
			List<IdmLongRunningTaskDto> ltrs = service.find(filter, null).getContent();
			Assert.assertEquals(1, ltrs.size());
			//
			IdmLongRunningTaskDto task = ltrs.get(0);
			manager.recover(task.getId());
			//
			ltrs = service.find(filter, null).getContent();
			Assert.assertEquals(2, ltrs.size());
		} finally {
			getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, true);
		}
	}
	
	@Test(expected = ResultCodeException.class)
	public void testRecoverableTaskIsRunningState() {
		IdmLongRunningTaskDto task = new IdmLongRunningTaskDto();
		task.setResult(new OperationResult.Builder(OperationState.RUNNING).build());
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(TestTaskExecutor.class.getCanonicalName());
		task.setRunning(false);
		task = service.save(task);
		//
		manager.recover(task.getId());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testRecoverableTaskIsRunningFlag() {
		IdmLongRunningTaskDto task = new IdmLongRunningTaskDto();
		task.setResult(new OperationResult.Builder(OperationState.RUNNING).build());
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(TestTaskExecutor.class.getCanonicalName());
		task.setRunning(true);
		task = service.save(task);
		//
		manager.recover(task.getId());
	}
	
	@Test(expected = TaskNotRecoverableException.class)
	public void testNotRecoverableTask() {
		IdmLongRunningTaskDto task = new IdmLongRunningTaskDto();
		task.setResult(new OperationResult.Builder(OperationState.EXECUTED).build());
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(TestSimpleLongRunningTaskExecutor.class.getCanonicalName());
		task.setRunning(false);
		task = service.save(task);
		//
		manager.recover(task.getId());
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void testRecoverableTaskNotExists() {
		manager.recover(UUID.randomUUID());
	}
	
	@Test
	public void testExecutePersistedBulkAction() {
		getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, false);
		try {
			getHelper().loginAdmin();
			IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
			IdmIdentityDto identityOther = getHelper().createIdentity((GuardedString) null);
			// filter setting
			IdmIdentityFilter filter = new IdmIdentityFilter();
			filter.setUsername(identityOne.getUsername());
			// test before
			List<IdmIdentityDto> identities = identityService.find(filter, null).getContent();
			Assert.assertEquals(1, identities.size());
			// prepare bulk action
			IdmBulkActionDto bulkAction = findBulkAction(IdmIdentity.class, IdentityDeleteBulkAction.NAME);
			bulkAction.setTransformedFilter(filter);
			bulkAction.setFilter(toMap(filter));
			// prepare and persist LRT
			IdentityDeleteBulkAction identityDeleteBulkAction = new IdentityDeleteBulkAction();
			identityDeleteBulkAction.setAction(bulkAction);
			IdmLongRunningTaskDto task = manager.resolveLongRunningTask(identityDeleteBulkAction, null, OperationState.CREATED);
			manager.processCreated(task.getId());
			//
			identities = identityService.find(filter, null).getContent();
			Assert.assertTrue(identities.isEmpty());
			Assert.assertNull(identityService.get(identityOne));
			Assert.assertNotNull(identityService.get(identityOther));
		} finally {
			getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, true);
			getHelper().logout();
		}
	}
	
	private class TestLogItemLongRunningTaskExecutor extends AbstractLongRunningTaskExecutor<Boolean> {
		
		private final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
				.getLogger(DefaultLongRunningTaskManagerIntegrationTest.TestLogItemLongRunningTaskExecutor.class);
		
		List<IdmIdentityDto> identities = new ArrayList<>();
		List<IdmIdentityDto> removedIdentities = new ArrayList<>();
		boolean log = true;
		
		@Override
		public Boolean process() {
			for (IdmIdentityDto identity : identities) {
				LOGGER.debug("Execute identity username: {} and id: {}" + identity.getUsername(), identity.getId());
				OperationResult result = new OperationResult();
				if (removedIdentities.contains(identity)) {
					result.setState(OperationState.NOT_EXECUTED);
				} else {
					result.setState(OperationState.EXECUTED);
				}
				if(this.log) {
					this.logItemProcessed(identity, result);
				}
			}
			return Boolean.TRUE;
		}

		public void addIdentityToProcess(IdmIdentityDto ...identities) {
			for (IdmIdentityDto identity : identities) {
				this.identities.add(identity);
			}
		}
		
		public void addRemovedIdentity(IdmIdentityDto ...identities) {
			for (IdmIdentityDto identity : identities) {
				this.removedIdentities.add(identity);
			}
		}
		
		public void setLog(boolean log) {
			this.log = log;
		}
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
		
		@Override
		public boolean isRecoverable() {
			return false;
		}
	}
	
	private class TestCountableLongRunningTaskExecutor extends AbstractLongRunningTaskExecutor<String> {

		private final String result;
		
		public TestCountableLongRunningTaskExecutor(String result, Long count) {
			this.result = result;
			this.count = count;
			counter = 0L;
		}
		
		@Override
		public String process() {
			for (long i = 0; i < count; i++) {
				counter++;
				if(!updateState()) {
					break;
				}
			}
			return result;
		}
		
	}
	
	private class TestStopableLongRunningTaskExecutor extends AbstractLongRunningTaskExecutor<String> {

		private final String result;
		
		public TestStopableLongRunningTaskExecutor(String result, Long count) {
			this.result = result;
			this.count = count;
			counter = 0L;
		}
		
		@Override
		public String process() {
			for (long i = 0; i < count; i++) {
				counter++;
				if(!updateState()) {
					break;
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException ex) {
					throw new CoreException("Test executor was interruped", ex);
				}
			}
			return result;
		}
		
	}
}
