package eu.bcvsolutions.idm.core.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.scheduler.api.dto.AbstractTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.CronTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.SimpleTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.dto.TaskTriggerState;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.TestTaskExecutor;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test check initialized test task and his parameters and triggers.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class AbstractScheduledTaskInitializerTest extends AbstractIntegrationTest {
	
	public static String TEST_DESCRIPTION = "TEST DESCRIPTION";
	public static String TEST_INSTANCE = "idm-test";
	
	private static String KEY_1 = "core:instanceId";
	private static String KEY_2 = "count";
	private static String KEY_3 = "test";
	
	private static String VALUE_1 = "idm-test";
	private static String VALUE_2 = "5";
	private static String VALUE_3 = "test";
	
	private static String TEST_CRON = "0 0 1 ? * *";
	private static DateTime TEST_FIRE_TIME = new DateTime(4102441200000l);
	
	@Autowired
	private SchedulerManager schedulerService;
	
	@Test
	public void testInitTask() {
		List<Task> tasks = schedulerService.getAllTasks();
		//
		// found test task, other will be skip
		for (Task task : tasks) {
			if (task.getTaskType().equals(TestTaskExecutor.class)) {
				assertEquals(TEST_DESCRIPTION, task.getDescription());
				assertEquals(TEST_INSTANCE, task.getInstanceId());
				// assertEquals(InitTestScheduledTask.TEST_MODULE, task.getModule()); // module not working in Task
				
				Map<String, String> parameters = task.getParameters();
				assertEquals(3, parameters.size());
				
				String value1 = parameters.get(KEY_1);
				assertEquals(VALUE_1, value1);
				
				String value2 = parameters.get(KEY_2);
				assertEquals(VALUE_2, value2);
				
				String value3 = parameters.get(KEY_3);
				assertEquals(VALUE_3, value3);
				
				List<AbstractTaskTrigger> triggers = task.getTriggers();
				assertEquals(2, triggers.size());
				for (AbstractTaskTrigger trigger : triggers) {
					assertEquals(TaskTriggerState.ACTIVE, trigger.getState());
					if (trigger.getClass().equals(CronTaskTrigger.class)) {
						CronTaskTrigger cronTrigger = (CronTaskTrigger)trigger;
						assertEquals(TEST_CRON, cronTrigger.getCron());
					} else if (trigger.getClass().equals(SimpleTaskTrigger.class)){
						SimpleTaskTrigger simpleTrigger = (SimpleTaskTrigger)trigger;
						assertEquals(TEST_FIRE_TIME, simpleTrigger.getFireTime());
					} else {
						fail();
					}
				}
				// success
				return;
			}
		}
		// test task is not initialized
		fail();
	}

}
