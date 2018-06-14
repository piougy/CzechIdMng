package eu.bcvsolutions.idm.core.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.scheduler.api.dto.CronTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.DependentTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.dto.SimpleTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.event.LongRunningTaskEvent;
import eu.bcvsolutions.idm.core.scheduler.api.event.LongRunningTaskEvent.LongRunningTaskEventType;
import eu.bcvsolutions.idm.core.scheduler.api.exception.DryRunNotSupportedException;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmScheduledTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.event.processor.LongRunningTaskExecuteDependentProcessor;
import eu.bcvsolutions.idm.core.scheduler.exception.InvalidCronExpressionException;
import eu.bcvsolutions.idm.core.scheduler.service.impl.DefaultSchedulerManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.IdentityRoleExpirationTaskExecutor;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Scheduler tests
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultSchedulerManagerIntegrationTest extends AbstractIntegrationTest {

	@Autowired private ApplicationContext context;
	@Autowired private ConfigurationService configurationService;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	@Autowired private IdmLongRunningTaskService longRunningTaskService;
	@Autowired private IdmScheduledTaskService scheduledTaskService;
	//
	private DefaultSchedulerManager manager;
	
	@Before
	public void init() {		
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultSchedulerManager.class);
		getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, true);
	}
	
	@After
	public void after() {
		getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, false);
	}
	
	@Test
	public void testAsynchronousTasks() {
		Assert.assertTrue(longRunningTaskManager.isAsynchronous());
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
		ObserveLongRunningTaskEndProcessor.listenTask(task.getId());
		//
		manager.createTrigger(task.getId(), getSimpleTrigger(task));
		//
		ObserveLongRunningTaskEndProcessor.waitForEnd(task.getId());
		//
		assertEquals(OperationState.EXECUTED, ObserveLongRunningTaskEndProcessor.getResult(task.getId()).getState());
		assertEquals(result, ObserveLongRunningTaskEndProcessor.getResultValue(task.getId()));
		//
		IdmScheduledTaskDto scheduledTask = scheduledTaskService.findByQuartzTaskName(task.getId());
		assertNotNull(scheduledTask);
		assertEquals(task.getId(), scheduledTask.getQuartzTaskName());
	}

	@Test
	public void testCreateAndRunRoleExpirationTask() throws Exception {
		Task task = createRoleExpirationTask();
		//
		ObserveLongRunningTaskEndProcessor.listenTask(task.getId());
		//
		manager.createTrigger(task.getId(), getSimpleTrigger(task));
		//
		ObserveLongRunningTaskEndProcessor.waitForEnd(task.getId());
		//
		assertEquals(OperationState.EXECUTED, ObserveLongRunningTaskEndProcessor.getResult(task.getId()).getState());
		//
		IdmScheduledTaskDto scheduledTask = scheduledTaskService.findByQuartzTaskName(task.getId());
		assertNotNull(scheduledTask);
		assertEquals(task.getId(), scheduledTask.getQuartzTaskName());
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
	
	@Test
	@SuppressWarnings("unchecked")
	public void testDependentTaskExecution() throws Exception {
		String resultValue = "dependent-task-initiator";
		//
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setOperationState(OperationState.CREATED);
		filter.setTaskType(TestSchedulableTask.class.getCanonicalName());
		filter.setFrom(new DateTime());
		List<IdmLongRunningTaskDto> createdLrts = longRunningTaskService.find(filter, null).getContent();
		Assert.assertEquals(0L, createdLrts.size());
		//
		Task task = createTask("dependent-task-initiator");
		DependentTaskTrigger trigger = new DependentTaskTrigger();
		trigger.setInitiatorTaskId(task.getId());
		// 
		// initiator = dependent task => circular execution
		manager.createTrigger(task.getId(), trigger);
		manager.runTask(task.getId());
		getHelper().waitForResult(getContinueFunction());
		createdLrts = longRunningTaskService.find(filter, null).getContent();
		Assert.assertEquals(1L, createdLrts.size());
		// execute first task
		LongRunningFutureTask<String> futureTask = (LongRunningFutureTask<String>) longRunningTaskManager.processCreated(createdLrts.get(0).getId());
		Assert.assertEquals(resultValue, futureTask.getFutureTask().get());
		//
		getHelper().waitForResult(getContinueFunction());
		createdLrts = longRunningTaskService.find(filter, null).getContent();
		Assert.assertEquals(1L, longRunningTaskService.find(filter, null).getTotalElements());
		//
		longRunningTaskManager.cancel(createdLrts.get(0).getId());
		//
		longRunningTaskService.find(filter, null).getContent();
		Assert.assertEquals(0L, longRunningTaskService.find(filter, null).getTotalElements()); // cancel - clean up
	}
	
	@Test
	public void testDependentTaskNoExecutionAfterInitiatorFails() throws Exception {
		LongRunningTaskExecuteDependentProcessor processor = context.getBean(LongRunningTaskExecuteDependentProcessor.class);
		//
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setOperationState(OperationState.CREATED);
		filter.setTaskType(TestSchedulableTask.class.getCanonicalName());
		filter.setFrom(new DateTime());
		List<IdmLongRunningTaskDto> createdLrts = longRunningTaskService.find(filter, null).getContent();
		Assert.assertEquals(0L, createdLrts.size());
		//
		Task task = createTask("dependent-task-initiator");
		DependentTaskTrigger trigger = new DependentTaskTrigger();
		trigger.setInitiatorTaskId(task.getId());
		// 
		// initiator = dependent task => circular execution
		manager.createTrigger(task.getId(), trigger);
		manager.runTask(task.getId());
		getHelper().waitForResult(getContinueFunction());
		createdLrts = longRunningTaskService.find(filter, null).getContent();
		Assert.assertEquals(1L, createdLrts.size());
		
	    IdmLongRunningTaskDto lrt = createdLrts.get(0);
	    lrt.setResult(new OperationResult(OperationState.EXCEPTION));
		// not executed
		EventResult<IdmLongRunningTaskDto> result = processor.process(new LongRunningTaskEvent(LongRunningTaskEventType.END, lrt));
		Assert.assertEquals(OperationState.NOT_EXECUTED, result.getResults().get(0).getState());
		lrt.setResult(new OperationResult(OperationState.BLOCKED));
		result = processor.process(new LongRunningTaskEvent(LongRunningTaskEventType.END, lrt));
		Assert.assertEquals(OperationState.NOT_EXECUTED, result.getResults().get(0).getState());
		lrt.setResult(new OperationResult(OperationState.NOT_EXECUTED));
		result = processor.process(new LongRunningTaskEvent(LongRunningTaskEventType.END, lrt));
		Assert.assertEquals(OperationState.NOT_EXECUTED, result.getResults().get(0).getState());
		lrt.setResult(new OperationResult(OperationState.CANCELED));
		result = processor.process(new LongRunningTaskEvent(LongRunningTaskEventType.END, lrt));
		Assert.assertEquals(OperationState.NOT_EXECUTED, result.getResults().get(0).getState());
		lrt.setResult(new OperationResult(OperationState.CREATED));
		result = processor.process(new LongRunningTaskEvent(LongRunningTaskEventType.END, lrt));
		Assert.assertEquals(OperationState.NOT_EXECUTED, result.getResults().get(0).getState());
		lrt.setResult(new OperationResult(OperationState.RUNNING));
		result = processor.process(new LongRunningTaskEvent(LongRunningTaskEventType.END, lrt));
		Assert.assertEquals(OperationState.NOT_EXECUTED, result.getResults().get(0).getState());
		//
		longRunningTaskManager.cancel(lrt.getId());
		//
		longRunningTaskService.find(filter, null).getContent();
		Assert.assertEquals(0L, longRunningTaskService.find(filter, null).getTotalElements()); // cancel - clean up
	}
	
	@Test(expected = DryRunNotSupportedException.class)
	public void testDryRunNotSupportedException() {
		Task task = createTask(null);
		//
		manager.runTask(task.getId(), true);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testDependentTaskInDryModeExecution() throws Exception {
		String resultValue = "dependent-task-initiator-dry-run";
		//
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setOperationState(OperationState.CREATED);
		filter.setTaskType(TestSchedulableDryRunTask.class.getCanonicalName());
		filter.setFrom(new DateTime());
		List<IdmLongRunningTaskDto> createdLrts = longRunningTaskService.find(filter, null).getContent();
		Assert.assertEquals(0L, createdLrts.size());
		//
		Task task = createDryRunTask(resultValue);
		DependentTaskTrigger trigger = new DependentTaskTrigger();
		trigger.setInitiatorTaskId(task.getId());
		// 
		// initiator = dependent task => circular execution
		manager.createTrigger(task.getId(), trigger);
		manager.runTask(task.getId(), true);
		getHelper().waitForResult(getContinueFunction());
		createdLrts = longRunningTaskService.find(filter, null).getContent();
		Assert.assertEquals(1L, createdLrts.size());
		Assert.assertTrue(createdLrts.get(0).isDryRun());
		UUID firstTaskId = createdLrts.get(0).getId();
		// execute first task
		LongRunningFutureTask<String> futureTask = (LongRunningFutureTask<String>) longRunningTaskManager.processCreated(createdLrts.get(0).getId());
		Assert.assertEquals(resultValue, futureTask.getFutureTask().get());
		//
		getHelper().waitForResult(getContinueFunction());
		createdLrts = longRunningTaskService.find(filter, null).getContent();
		Assert.assertEquals(1L, longRunningTaskService.find(filter, null).getTotalElements());
		Assert.assertTrue(createdLrts.get(0).isDryRun());
		Assert.assertNotEquals(firstTaskId, createdLrts.get(0).getId());
		//
		longRunningTaskManager.cancel(createdLrts.get(0).getId());
		//
		longRunningTaskService.find(filter, null).getContent();
		Assert.assertEquals(0L, longRunningTaskService.find(filter, null).getTotalElements()); // cancel - clean up
	}
	
	private Task createTask(String result) {
		Task task = new Task();
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(TestSchedulableTask.class);
		task.setDescription("test");
		task.getParameters().put(ObserveLongRunningTaskEndProcessor.RESULT_PROPERTY, result);
		//
		return manager.createTask(task);
	}
	
	private Task createDryRunTask(String result) {
		Task task = new Task();
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(TestSchedulableDryRunTask.class);
		task.setDescription("test");
		task.getParameters().put(ObserveLongRunningTaskEndProcessor.RESULT_PROPERTY, result);
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
}
