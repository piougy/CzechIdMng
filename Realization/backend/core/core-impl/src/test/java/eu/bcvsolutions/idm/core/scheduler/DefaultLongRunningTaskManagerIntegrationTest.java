package eu.bcvsolutions.idm.core.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executor;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
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

	@Autowired
	private IdmLongRunningTaskService service;
	@Autowired
	private Executor executor;
	@Autowired
	private ConfigurationService configurationService;
	//
	private DefaultLongRunningTaskManager manager;
	private String processed = null;
	
	@Before
	public void init() {		
		manager = new DefaultLongRunningTaskManager(
				service,
				executor, 
				configurationService);
	}
	
	@Test
	public void testRunSimpleTask() {
		String result = "TEST_SUCCESS_01";
		LongRunningTaskExecutor taskExecutor = new TestSimpleLongRunningTaskExecutor(result);
		assertNull(taskExecutor.getLongRunningTaskId());
		//
		manager.execute(taskExecutor);
		//
		IdmLongRunningTask longRunningTask = service.get(taskExecutor.getLongRunningTaskId());
		assertNotNull(longRunningTask);
		assertEquals(result, longRunningTask.getTaskDescription());
		assertEquals(taskExecutor.getClass().getCanonicalName(), longRunningTask.getTaskType());
		assertEquals(configurationService.getInstanceId(), longRunningTask.getInstanceId());
		//
		Function<String, Boolean> continueFunction = res -> {
			return !result.equals(processed);
		};
		DefaultSchedulerManagerIntegrationTest.waitForResult(continueFunction);
		//
		longRunningTask = service.get(longRunningTask.getId());
		assertEquals(OperationState.EXECUTED, longRunningTask.getResult().getState());
	}
	
	@Test
	public void testRunCountableTask() {
		String result = "TEST_SUCCESS_02";
		Long count = 10L;
		LongRunningTaskExecutor taskExecutor = new TestCountableLongRunningTaskExecutor(result, count);
		//
		manager.execute(taskExecutor);
		//	
		Function<String, Boolean> continueFunction = res -> {
			return !result.equals(processed);
		};
		DefaultSchedulerManagerIntegrationTest.waitForResult(continueFunction);
		//
		IdmLongRunningTask longRunningTask = service.get(taskExecutor.getLongRunningTaskId());
		assertEquals(OperationState.EXECUTED, longRunningTask.getResult().getState());
		assertEquals(count, longRunningTask.getCount());
		assertEquals(count, longRunningTask.getCounter());
	}
	
	// TODO: Locking
	// @Test
	public void testCancelTaskBeforeStart() {
		String result = "TEST_SUCCESS_03";
		Long count = 50L;
		LongRunningTaskExecutor taskExecutor = new TestStopableLongRunningTaskExecutor(result, count);
		//
		manager.execute(taskExecutor);
		manager.cancel(taskExecutor.getLongRunningTaskId());
		//	
		Function<String, Boolean> continueFunction = res -> {
			return !result.equals(processed);
		};
		DefaultSchedulerManagerIntegrationTest.waitForResult(continueFunction);
		//
		IdmLongRunningTask longRunningTask = service.get(taskExecutor.getLongRunningTaskId());
		assertEquals(OperationState.CANCELED, longRunningTask.getResult().getState());
		assertEquals(count, longRunningTask.getCount());
		assertNotEquals(count, longRunningTask.getCounter());
	}
	
	@Test
	public void testCancelRunningTask() {
		String result = "TEST_SUCCESS_04";
		Long count = 100L;
		LongRunningTaskExecutor taskExecutor = new TestStopableLongRunningTaskExecutor(result, count);
		//
		manager.execute(taskExecutor);
		//
		Function<String, Boolean> continueFunction = res -> {
			return !service.get(taskExecutor.getLongRunningTaskId()).isRunning();
		};
		DefaultSchedulerManagerIntegrationTest.waitForResult(continueFunction);
		//
		manager.cancel(taskExecutor.getLongRunningTaskId());
		//
		continueFunction = res -> {
			return !result.equals(processed);
		};
		DefaultSchedulerManagerIntegrationTest.waitForResult(continueFunction);
		//
		IdmLongRunningTask longRunningTask = service.get(taskExecutor.getLongRunningTaskId());
		assertEquals(OperationState.CANCELED, longRunningTask.getResult().getState());
		assertEquals(count, longRunningTask.getCount());
		assertNotEquals(count, longRunningTask.getCounter());
	}
	
	@Test
	public void testInterruptRunningTask() {
		String result = "TEST_SUCCESS_05";
		Long count = 100L;
		LongRunningTaskExecutor taskExecutor = new TestStopableLongRunningTaskExecutor(result, count);
		//
		manager.execute(taskExecutor);
		//
		Function<String, Boolean> continueFunction = res -> {
			return !service.get(taskExecutor.getLongRunningTaskId()).isRunning();
		};
		DefaultSchedulerManagerIntegrationTest.waitForResult(continueFunction);
		//
		manager.interrupt(taskExecutor.getLongRunningTaskId());
		//
		continueFunction = res -> {
			return !service.get(taskExecutor.getLongRunningTaskId()).isRunning();
		};
		DefaultSchedulerManagerIntegrationTest.waitForResult(continueFunction);
		//
		IdmLongRunningTask longRunningTask = service.get(taskExecutor.getLongRunningTaskId());
		assertNotEquals(OperationState.RUNNING, longRunningTask.getResult().getState());
		assertEquals(count, longRunningTask.getCount());
		assertNotEquals(count, longRunningTask.getCounter());
		assertFalse(longRunningTask.isRunning());
	}
	
	@Test
	public void testCancelPreviouslyRunnedTask() {
		IdmLongRunningTask taskOne = new IdmLongRunningTask();
		taskOne.setResult(new OperationResult.Builder(OperationState.RUNNING).build());
		taskOne.setInstanceId(configurationService.getInstanceId());
		taskOne.setTaskType(TestSimpleLongRunningTaskExecutor.class.getCanonicalName());
		taskOne.setRunning(true);
		taskOne = service.save(taskOne);
		//
		IdmLongRunningTask taskTwo = new IdmLongRunningTask();
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
	
	private class TestSimpleLongRunningTaskExecutor extends AbstractLongRunningTaskExecutor {

		private final String result;
		
		public TestSimpleLongRunningTaskExecutor(String result) {
			this.result = result;
		}
		
		@Override
		public String getDescription() {
			return result;
		}
		
		@Override
		public void process() {
			processed = result;
		}
		
	}
	
	private class TestCountableLongRunningTaskExecutor extends AbstractLongRunningTaskExecutor {

		private final String result;
		private final Long count;
		private Long counter;
		
		public TestCountableLongRunningTaskExecutor(String result, Long count) {
			this.result = result;
			this.count = count;
			counter = 0L;
		}
		
		@Override
		public Long getCount() {
			return count;
		}
		@Override
		public Long getCounter() {
			return counter;
		}
		
		@Override
		public void process() {
			for (long i = 0; i < count; i++) {
				counter++;
				if(!updateState()) {
					break;
				}
			}			
			processed = result;
		}
		
	}
	
	private class TestStopableLongRunningTaskExecutor extends AbstractLongRunningTaskExecutor {

		private final String result;
		private final Long count;
		private Long counter;
		
		public TestStopableLongRunningTaskExecutor(String result, Long count) {
			this.result = result;
			this.count = count;
			counter = 0L;
		}
		
		@Override
		public Long getCount() {
			return count;
		}
		@Override
		public Long getCounter() {
			return counter;
		}
		
		@Override
		public void process() {
			for (long i = 0; i < count; i++) {
				counter++;
				if(!updateState()) {
					break;
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException ex) {
					throw new CoreException(ex);
				}
			}			
			processed = result;
		}
		
	}
}
