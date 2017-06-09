package eu.bcvsolutions.idm.core.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractLongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.service.impl.DefaultLongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Long running tasks test
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultLongRunningTaskManagerIntegrationTest extends AbstractIntegrationTest {

	@Autowired private ApplicationContext context;
	@Autowired private IdmLongRunningTaskService service;
	@Autowired private ConfigurationService configurationService;
	//
	private LongRunningTaskManager manager;
	
	@Before
	public void init() {		
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultLongRunningTaskManager.class);
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
		Long count = 100L;
		LongRunningTaskExecutor<String> taskExecutor = new TestStopableLongRunningTaskExecutor(result, count);
		//
		LongRunningFutureTask<String> futureTask = manager.execute(taskExecutor);
		//
		Function<String, Boolean> continueFunction = res -> {
			return !service.get(taskExecutor.getLongRunningTaskId()).isRunning();
		};
		waitForResult(continueFunction);
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
		//
		Function<String, Boolean> continueFunction = res -> {
			return !service.get(taskExecutor.getLongRunningTaskId()).isRunning();
		};
		waitForResult(continueFunction);
		//
		IdmLongRunningTaskDto longRunningTask = service.get(taskExecutor.getLongRunningTaskId());
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
					throw new CoreException("text executor was interruped", ex);
				}
			}
			return result;
		}
		
	}
	
	protected static void waitForResult(Function<String, Boolean> continueFunction) {
		int counter = 0;
		while(continueFunction.apply(null) && (counter < 25)) {
			counter++;
			try {
				Thread.sleep(300);
			} catch (InterruptedException ex) {
				throw new CoreException(ex);
			}
		};
	}
}
