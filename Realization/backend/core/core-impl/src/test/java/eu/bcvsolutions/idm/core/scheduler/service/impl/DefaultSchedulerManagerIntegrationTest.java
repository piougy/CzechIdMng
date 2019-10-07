package eu.bcvsolutions.idm.core.scheduler.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import java.time.ZonedDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.ObserveLongRunningTaskEndProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.dto.CronTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.DependentTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.SimpleTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.event.LongRunningTaskEvent;
import eu.bcvsolutions.idm.core.scheduler.api.event.LongRunningTaskEvent.LongRunningTaskEventType;
import eu.bcvsolutions.idm.core.scheduler.api.exception.DryRunNotSupportedException;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmScheduledTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.event.processor.LongRunningTaskExecuteDependentProcessor;
import eu.bcvsolutions.idm.core.scheduler.exception.InvalidCronExpressionException;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmDependentTaskTriggerRepository;
import eu.bcvsolutions.idm.core.scheduler.task.impl.IdentityRoleExpirationTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
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
	@Autowired private IdmScheduledTaskService scheduledTaskService;
	@Autowired private IdmDependentTaskTriggerRepository dependentTaskTriggerRepository;
	@Autowired private IdmIdentityService identityService;
	//
	private DefaultSchedulerManager manager;
	
	@Before
	public void init() {		
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultSchedulerManager.class);
	}
	
	@Test
	public void testAsynchronousTasks() {
		// we are testing scheduler, not async lrts
		Assert.assertFalse(longRunningTaskManager.isAsynchronous());
	}
	
	@Test
	public void testReferentialIntegrityAfterInitiatorDelete() throws Exception {
		Task initiatorTask = createTask("mock");
		Task dependentTask = createTask("mock");
		DependentTaskTrigger trigger = new DependentTaskTrigger();
		trigger.setInitiatorTaskId(initiatorTask.getId());
		//
		manager.createTrigger(dependentTask.getId(), trigger);
		//
		Assert.assertFalse(dependentTaskTriggerRepository.findByDependentTaskId(dependentTask.getId()).isEmpty());
		// delete initiator
		manager.deleteTask(initiatorTask.getId());
		//
		Assert.assertTrue(dependentTaskTriggerRepository.findByDependentTaskId(dependentTask.getId()).isEmpty());
	}
	
	@Test
	public void testReferentialIntegrityAfterDependentDelete() throws Exception {
		Task initiatorTask = createTask("mock");
		Task dependentTask = createTask("mock");
		DependentTaskTrigger trigger = new DependentTaskTrigger();
		trigger.setInitiatorTaskId(initiatorTask.getId());
		//
		manager.createTrigger(dependentTask.getId(), trigger);
		//
		Assert.assertFalse(dependentTaskTriggerRepository.findByInitiatorTaskId(initiatorTask.getId()).isEmpty());
		// delete initiator
		manager.deleteTask(dependentTask.getId());
		//
		Assert.assertTrue(dependentTaskTriggerRepository.findByInitiatorTaskId(initiatorTask.getId()).isEmpty());
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
	public void testCreateTaskWithEmptyDescription() {
		Task task = new Task();
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(TestRegistrableSchedulableTask.class);
		//
		task = manager.createTask(task);
		//
		Assert.assertEquals(TestRegistrableSchedulableTask.DESCRIPTION, task.getDescription());
	}
	
	@Test
	public void testUpdateTaskWithEmptyDescription() {
		Task task = new Task();
		task.setDescription("mock");
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(TestRegistrableSchedulableTask.class);
		//
		task = manager.createTask(task);
		Assert.assertEquals("mock", task.getDescription());
		//
		task.setDescription(null);
		task = manager.updateTask(task.getId(), task);
		Assert.assertEquals(TestRegistrableSchedulableTask.DESCRIPTION, task.getDescription());
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
	public void testDependentTaskExecution() throws Exception {
		String resultValue = "dependent-task-initiator";
		String resultValueDependent = "dependended-taskr";
		//
		Task initiatorTask = createTask(resultValue);
		Task dependentTask = createTask(resultValueDependent);
		ObserveLongRunningTaskEndProcessor.listenTask(initiatorTask.getId());
		ObserveLongRunningTaskEndProcessor.listenTask(dependentTask.getId());
		DependentTaskTrigger trigger = new DependentTaskTrigger();
		trigger.setInitiatorTaskId(initiatorTask.getId());
		// 
		// execute initiator
		manager.createTrigger(dependentTask.getId(), trigger);
		manager.runTask(initiatorTask.getId());
		ObserveLongRunningTaskEndProcessor.waitForEnd(initiatorTask.getId());
		ObserveLongRunningTaskEndProcessor.waitForEnd(dependentTask.getId());
		//
		assertEquals(OperationState.EXECUTED, ObserveLongRunningTaskEndProcessor.getResult(initiatorTask.getId()).getState());
		assertEquals(resultValue, ObserveLongRunningTaskEndProcessor.getResultValue(initiatorTask.getId()));
		assertEquals(OperationState.EXECUTED, ObserveLongRunningTaskEndProcessor.getResult(dependentTask.getId()).getState());
		assertEquals(resultValueDependent, ObserveLongRunningTaskEndProcessor.getResultValue(dependentTask.getId()));
	}
	
	/**
	 * TODO: this test execute task standardly and then mock results => execute the first task with exception instead ...
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDependentTaskNoExecutionAfterInitiatorFails() throws Exception {
		LongRunningTaskExecuteDependentProcessor processor = context.getBean(LongRunningTaskExecuteDependentProcessor.class);
		//
		String resultValue = "dependent-task-initiator";
		String resultValueDependent = "dependent-taskr";
		//
		Task initiatorTask = createTask(resultValue);
		Task dependentTask = createTask(resultValueDependent);
		ObserveLongRunningTaskEndProcessor.listenTask(initiatorTask.getId());
		ObserveLongRunningTaskEndProcessor.listenTask(dependentTask.getId());
		DependentTaskTrigger trigger = new DependentTaskTrigger();
		trigger.setInitiatorTaskId(initiatorTask.getId());
		// 
		// execute initiator
		manager.createTrigger(dependentTask.getId(), trigger);
		manager.runTask(initiatorTask.getId());
		ObserveLongRunningTaskEndProcessor.waitForEnd(initiatorTask.getId());
		ObserveLongRunningTaskEndProcessor.waitForEnd(dependentTask.getId());
		
	    IdmLongRunningTaskDto lrt = ObserveLongRunningTaskEndProcessor.getLongRunningTask(initiatorTask.getId());
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
	}
	
	@Test(expected = DryRunNotSupportedException.class)
	public void testDryRunNotSupportedException() {
		Task task = createTask(null);
		//
		manager.runTask(task.getId(), true);
	}
	
	@Test
	public void testDependentTaskInDryModeExecution() throws Exception {
		String resultValue = "dependent-task-initiator";
		String resultValueDependent = "dependended-taskr";
		//
		Task initiatorTask = createDryRunTask(resultValue);
		Task dependentTask = createDryRunTask(resultValueDependent);
		ObserveLongRunningTaskEndProcessor.listenTask(initiatorTask.getId());
		ObserveLongRunningTaskEndProcessor.listenTask(dependentTask.getId());
		DependentTaskTrigger trigger = new DependentTaskTrigger();
		trigger.setInitiatorTaskId(initiatorTask.getId());
		// 
		// execute initiator
		manager.createTrigger(dependentTask.getId(), trigger);
		manager.runTask(initiatorTask.getId(), true);
		ObserveLongRunningTaskEndProcessor.waitForEnd(initiatorTask.getId());
		ObserveLongRunningTaskEndProcessor.waitForEnd(dependentTask.getId());
		//
		assertEquals(resultValue, ObserveLongRunningTaskEndProcessor.getResultValue(initiatorTask.getId()));
		assertTrue(ObserveLongRunningTaskEndProcessor.getLongRunningTask(initiatorTask.getId()).isDryRun());
		assertEquals(resultValueDependent, ObserveLongRunningTaskEndProcessor.getResultValue(dependentTask.getId()));
		assertTrue(ObserveLongRunningTaskEndProcessor.getLongRunningTask(dependentTask.getId()).isDryRun());
	}
	
	@Test
	public void testGetAllTasksByType() {
		Task taskOne = createTask(null);
		Task taskTwo = createTask(null);
		//
		List<Task> tasks = manager.getAllTasksByType(taskOne.getTaskType());
		Assert.assertTrue(tasks.size() >= 2);
		Assert.assertTrue(tasks.stream().allMatch(t -> t.getTaskType().equals(taskTwo.getTaskType())));
		Assert.assertTrue(tasks.stream().anyMatch(t -> t.getId().equals(taskOne.getId())));
		Assert.assertTrue(tasks.stream().anyMatch(t -> t.getId().equals(taskOne.getId())));
	}
	
	@Test
	public void testUpdateTask() {
		Task taskOne = createTask("one");
		Assert.assertEquals("one", taskOne.getParameters().get(ObserveLongRunningTaskEndProcessor.RESULT_PROPERTY));
		//
		taskOne.getParameters().put(ObserveLongRunningTaskEndProcessor.RESULT_PROPERTY, "update");
		manager.updateTask(taskOne.getId(), taskOne);
		//
		taskOne = manager.getTask(taskOne.getId());
		Assert.assertEquals("update", taskOne.getParameters().get(ObserveLongRunningTaskEndProcessor.RESULT_PROPERTY));
	}
	
	/**
	 * Disabled task can be loaded, but cannot be executed 
	 * @throws InterruptedException 
	 */
	@Test
	public void testDisabledTask() throws InterruptedException {
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		Task createTask = new Task();
		createTask.setInstanceId(configurationService.getInstanceId());
		createTask.setTaskType(TestUpdateIdentityTask.class);
		createTask.setDescription("test");
		createTask.getParameters().put(ObserveLongRunningTaskEndProcessor.RESULT_PROPERTY, identityOne.getUsername());
		//
		Task taskOne = manager.createTask(createTask);
		Task task = manager.getTask(taskOne.getId());
		//
		Assert.assertFalse(task.isDisabled());
		//
		TestUpdateIdentityTask disabledTaskExecutor = AutowireHelper.createBean(TestUpdateIdentityTask.class);
		try {
			configurationService.setBooleanValue(
					disabledTaskExecutor.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED), 
					false
			);
			//
			task = manager.getTask(taskOne.getId());
			Assert.assertTrue(task.isDisabled());
			//
			List<Task> supportedTasks = manager.getSupportedTasks();
			Assert.assertTrue(supportedTasks.stream().anyMatch(t -> t.isDisabled() && t.getTaskType().equals(TestUpdateIdentityTask.class)));
			//
			// Schedule task and execute => lastName should not be changed.
			manager.createTrigger(taskOne.getId(), getSimpleTrigger(taskOne));
			// 
			Function<String, Boolean> continueFunction = res -> {
				return !manager.getTask(taskOne.getId()).getTriggers().isEmpty();
			};
			getHelper().waitForResult(continueFunction);
			//
			identityOne = identityService.get(identityOne);
			Assert.assertNotEquals(identityOne.getUsername(), identityOne.getLastName());
			//
			configurationService.setBooleanValue(
					disabledTaskExecutor.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED), 
					true
			);
			//
			manager.createTrigger(taskOne.getId(), getSimpleTrigger(taskOne));
			//
			getHelper().waitForResult(continueFunction);
			//
			identityOne = identityService.get(identityOne);
			Assert.assertEquals(identityOne.getUsername(), identityOne.getLastName());
		} finally {
			configurationService.setBooleanValue(
					disabledTaskExecutor.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED), 
					true
			);
		}
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
		trigger.setFireTime(ZonedDateTime.now());
		return trigger;
	}	
}
