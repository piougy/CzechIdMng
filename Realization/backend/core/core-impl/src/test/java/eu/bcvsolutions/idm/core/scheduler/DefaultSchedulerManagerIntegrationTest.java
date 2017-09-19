package eu.bcvsolutions.idm.core.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.CronTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.dto.SimpleTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmScheduledTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.exception.InvalidCronExpressionException;
import eu.bcvsolutions.idm.core.scheduler.service.impl.DefaultSchedulerManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.IdentityRoleExpirationTaskExecutor;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Scheduler tests
 * 
 *  TODO: use futures instead wait - its unstable
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultSchedulerManagerIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private ApplicationContext context;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private LongRunningTaskManager longRunningTaskManager;
	@Autowired
	private IdmLongRunningTaskService longRunningTaskService;
	@Autowired
	private IdmScheduledTaskService scheduledTaskService;
	//
	private DefaultSchedulerManager manager;
	protected final static String RESULT_PROPERTY = "result";
	
	@Before
	public void init() {		
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultSchedulerManager.class);
	}
	
	@Test
	public void testTaskRegistration() {
		List<Task> tasks = manager.getSupportedTasks();
		//
		assertTrue(tasks.size() > 0);
		boolean testTaskIsRegisterd = false;
		for (Task task : tasks) {
			if (TestRegistrableSchedulableTask.class.equals(task.getTaskType())) {
				testTaskIsRegisterd = true;
				break;
			}
		}
		assertTrue(testTaskIsRegisterd);
	}
	
	@Test
	public void testCreateTask() {
		String result = "TEST_SCHEDULER_ONE";
		Task task = createTask(result);
		//
		assertNotNull(task.getId());
		assertEquals(task.getId(), manager.getTask(task.getId()).getId());
		//
		manager.deleteTask(task.getId());
		//
		assertNull(manager.getTask(task.getId()));
	}
	
	@Test
	public void testCreateAndRunSimpleTrigger() throws InterruptedException, ExecutionException {
		String result = "TEST_SCHEDULER_TWO";
		Task task = createTask(result);
		//
		manager.createTrigger(task.getId(), getSimpleTrigger(task));
		//
		DefaultLongRunningTaskManagerIntegrationTest.waitForResult(getContinueFunction());
		//
		List<FutureTask<?>> taskList = getFutureTaskList(TestSchedulableTask.class);
		assertEquals(result, taskList.get(0).get());
		//
		checkScheduledTask(task);
	}

	@Test
	public void testCreateAndRunRoleExpirationTask() throws Exception {
		Task task = createRoleExpirationTask();
		//
		manager.createTrigger(task.getId(), getSimpleTrigger(task));
		//
		DefaultLongRunningTaskManagerIntegrationTest.waitForResult(getContinueFunction());
		//
		List<FutureTask<?>> taskList = getFutureTaskList(IdentityRoleExpirationTaskExecutor.class);
		assertEquals(Boolean.TRUE, taskList.get(0).get());
		//
		checkScheduledTask(task);
	}

	@Test
	public void testCreateAndDeleteCronTrigger() {
		Task task = createTask(null);
		//
		CronTaskTrigger trigger = new CronTaskTrigger();
		trigger.setTaskId(task.getId());
		trigger.setCron("5 * * * * ?");
		//
		manager.createTrigger(task.getId(), trigger);
		//
		task = manager.getTask(task.getId());
		//
		assertEquals(1, task.getTriggers().size());
		assertEquals(CronTaskTrigger.class, task.getTriggers().get(0).getClass());
		assertEquals(task.getId(), task.getTriggers().get(0).getTaskId());
		//
		manager.deleteTrigger(task.getId(), task.getTriggers().get(0).getId());
		//
		task = manager.getTask(task.getId());
		assertEquals(0, task.getTriggers().size());
	}
	
	@Test(expected = InvalidCronExpressionException.class)
	public void testCreateInvalidCronTrigger() {
		Task task = createTask(null);
		//
		CronTaskTrigger trigger = new CronTaskTrigger();
		trigger.setTaskId(task.getId());
		trigger.setCron("not-valid");
		//
		manager.createTrigger(task.getId(), trigger);
	}
	
	private Task createTask(String result) {
		Task task = new Task();
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(TestSchedulableTask.class);
		task.setDescription("test");
		task.getParameters().put(RESULT_PROPERTY, result);
		//
		return manager.createTask(task);
	}
	
	private Task createRoleExpirationTask() {
		Task task = new Task();
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(IdentityRoleExpirationTaskExecutor.class);
		task.setDescription("test role expiration task");
		//
		return manager.createTask(task);
	}

	private List<FutureTask<?>> getFutureTaskList(Class<?> clazz) {
		List<FutureTask<?>> taskList = new ArrayList<>();
		for (LongRunningFutureTask<?> longRunningFutureTask : longRunningTaskManager.processCreated()) {
			if (longRunningFutureTask.getExecutor().getClass().equals(clazz)) {
				taskList.add(longRunningFutureTask.getFutureTask());
			}
		}
		return taskList;
	}

	private SimpleTaskTrigger getSimpleTrigger(Task task) {
		SimpleTaskTrigger trigger = new SimpleTaskTrigger();
		trigger.setTaskId(task.getId());
		trigger.setFireTime(new DateTime());
		return trigger;
	}

	private Function<String, Boolean> getContinueFunction() {
		Function<String, Boolean> continueFunction = res -> {
			return longRunningTaskService.findAllByInstance(configurationService.getInstanceId(),
					OperationState.CREATED).size() == 0;
		};
		return continueFunction;
	}
	
	private void checkScheduledTask(Task task) {
		IdmScheduledTaskDto scheduledTask = scheduledTaskService.findByQuartzTaskName(task.getId());
		assertNotNull(scheduledTask);
		assertEquals(false, scheduledTask.isDryRun());
		assertEquals(task.getId(), scheduledTask.getQuartzTaskName());
	}
	
}
