package eu.bcvsolutions.idm.core.scheduler.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.scheduler.ObserveLongRunningTaskEndProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.scheduler.api.dto.CronTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmScheduledTaskService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * @author Petr Hanak
 *
 */
public class CronTaskTriggerExecutionDateIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private ApplicationContext context;
	@Autowired private ConfigurationService configurationService;
	@Autowired private IdmScheduledTaskService scheduledTaskService;
	
	private DefaultSchedulerManager manager;
	
	@Before
	public void init() {		
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultSchedulerManager.class);
//		MAYBE
		getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, true);
	}
	
	@After
	public void end() {
//		MAYBE
		getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, false);
	}
	
	@Test
	public void testCreateAndRunCronTrigger() throws InterruptedException {
		String result = "TEST_CRON_ONE";
		Task task = createTask(result);
		//
		ObserveLongRunningTaskEndProcessor.listenTask(task.getId());
		//
		manager.createTrigger(task.getId(), getCronTrigger(task));
		//
		ObserveLongRunningTaskEndProcessor.waitForEnd(task.getId());
		//
		assertEquals(OperationState.NOT_EXECUTED, ObserveLongRunningTaskEndProcessor.getResult(task.getId()).getState());
		assertEquals(result, ObserveLongRunningTaskEndProcessor.getResultValue(task.getId()));
		//
		IdmScheduledTaskDto scheduledTask = scheduledTaskService.findByQuartzTaskName(task.getId());
		assertNotNull(scheduledTask);
		assertEquals(task.getId(), scheduledTask.getQuartzTaskName());
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
	
	private CronTaskTrigger getCronTrigger(Task task) {
		CronTaskTrigger trigger = new CronTaskTrigger();
		trigger.setTaskId(task.getId());
		// run every minute
		trigger.setCron("0 * * * * ?");
		
		DateTime date = DateTime.now().plusMinutes(2);
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		String strDate = dateFormat.format(date);
		trigger.setExecuteDate(strDate);
		return trigger;
	}

}
