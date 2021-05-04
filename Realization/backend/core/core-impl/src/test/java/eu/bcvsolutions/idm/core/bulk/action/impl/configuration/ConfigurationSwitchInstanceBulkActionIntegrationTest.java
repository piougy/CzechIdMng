package eu.bcvsolutions.idm.core.bulk.action.impl.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.core.scheduler.service.impl.TestSchedulableTask;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Switch asynchronous instance test.
 * 
 * @author Radek Tomiška
 *
 */
public class ConfigurationSwitchInstanceBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmEntityEventService entityEventService;
	@Autowired private SchedulerManager schedulerManager;
	@Autowired private IdmLongRunningTaskService longRunningTaskService;
	@Autowired private IdmConfigurationService configurationService;
	@Autowired private EntityEventManager eventManager;
	@Autowired private LongRunningTaskManager taskManager;
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testSwitchSuccess() {
		// prepare all data
		String previousInstanceIdOne = getHelper().createName();
		String previousInstanceIdTwo = getHelper().createName();
		String newInstanceId = getHelper().createName();
		// 
		// create events
		IdmEntityEventDto eventOne = createEvent(previousInstanceIdOne);
		IdmEntityEventDto eventTwo = createEvent(previousInstanceIdTwo);
		//
		// create LRT
		IdmLongRunningTaskDto taskOne = createTask(previousInstanceIdOne);
		IdmLongRunningTaskDto taskTwo = createTask(previousInstanceIdTwo);
		//
		// create scheduledTasks
		Task scheduledTaskOne = createScheduledTask(previousInstanceIdOne);
		Task scheduledTaskTwo = createScheduledTask(previousInstanceIdTwo);
		//
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmConfiguration.class, ConfigurationSwitchInstanceBulkAction.NAME);
		Map<String, Object> properties = new HashMap<>();
		properties.put(ConfigurationSwitchInstanceBulkAction.PROPERTY_PREVIOUS_INSTANCE_ID, previousInstanceIdOne);
		properties.put(ConfigurationSwitchInstanceBulkAction.PROPERTY_NEW_INSTANCE_ID, newInstanceId);
		bulkAction.setProperties(properties);
		// prevalidate
		ResultModels models = bulkActionManager.prevalidate(bulkAction);
		Assert.assertFalse(models.getInfos().isEmpty());
		// change
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		//
		Assert.assertEquals(newInstanceId, entityEventService.get(eventOne).getInstanceId());
		Assert.assertEquals(previousInstanceIdTwo, entityEventService.get(eventTwo).getInstanceId());
		//
		Assert.assertEquals(newInstanceId, schedulerManager.getTask(scheduledTaskOne.getId()).getInstanceId());
		Assert.assertEquals(previousInstanceIdTwo, schedulerManager.getTask(scheduledTaskTwo.getId()).getInstanceId());
		//
		Assert.assertEquals(newInstanceId, longRunningTaskService.get(taskOne.getId()).getInstanceId());
		Assert.assertEquals(previousInstanceIdTwo, longRunningTaskService.get(taskTwo.getId()).getInstanceId());
		//
		// clean up created, just for sure
		entityEventService.delete(eventOne);
		entityEventService.delete(eventTwo);
		schedulerManager.deleteTask(scheduledTaskOne.getId());
		schedulerManager.deleteTask(scheduledTaskTwo.getId());
		longRunningTaskService.delete(taskOne);
		longRunningTaskService.delete(taskTwo);
	}
	
	@Test
	public void testNotChangeSameInstances() {
		String newInstanceId = getHelper().createName();
		//
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmConfiguration.class, ConfigurationSwitchInstanceBulkAction.NAME);
		Map<String, Object> properties = new HashMap<>();
		properties.put(ConfigurationSwitchInstanceBulkAction.PROPERTY_PREVIOUS_INSTANCE_ID, newInstanceId);
		properties.put(ConfigurationSwitchInstanceBulkAction.PROPERTY_NEW_INSTANCE_ID, newInstanceId);
		bulkAction.setProperties(properties);
		// prevalidate
		bulkActionManager.prevalidate(bulkAction);
		// change
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, null, null, 1l);
	}
	
	@Test
	public void testProcessWithoutAuthority() {
		logout();
		loginWithout(TestHelper.ADMIN_USERNAME, IdmGroupPermission.APP_ADMIN, CoreGroupPermission.SCHEDULER_ADMIN, CoreGroupPermission.SCHEDULER_EXECUTE);
		//
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmConfiguration.class, ConfigurationSwitchInstanceBulkAction.NAME);
		Map<String, Object> properties = new HashMap<>();
		properties.put(ConfigurationSwitchInstanceBulkAction.PROPERTY_PREVIOUS_INSTANCE_ID, getHelper().createName());
		properties.put(ConfigurationSwitchInstanceBulkAction.PROPERTY_NEW_INSTANCE_ID, getHelper().createName());
		bulkAction.setProperties(properties);
		// prevalidate
		bulkActionManager.prevalidate(bulkAction);
		// change
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, null, null, 1l);
	}
	
	@Test
	public void testReuseAsynchronousEventInstanceId() {
		String eventInstanceId = getHelper().createName();
		IdmConfigurationDto instanceId = configurationService.getByCode(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_INSTANCE_ID);
		if (instanceId == null) {
			configurationService.setValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_INSTANCE_ID, eventInstanceId);
			instanceId = configurationService.getByCode(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_INSTANCE_ID);
		} else {
			eventInstanceId = instanceId.getValue();
		}
		//
		String newInstanceId = getHelper().createName();
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmConfiguration.class, ConfigurationSwitchInstanceBulkAction.NAME);
		Map<String, Object> properties = new HashMap<>();
		properties.put(ConfigurationSwitchInstanceBulkAction.PROPERTY_PREVIOUS_INSTANCE_ID, newInstanceId);
		properties.put(ConfigurationSwitchInstanceBulkAction.PROPERTY_NEW_INSTANCE_ID, newInstanceId);
		bulkAction.setProperties(properties);
		// prevalidate
		bulkActionManager.prevalidate(bulkAction);
		// change
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, null, null, 1l);
		//
		IdmConfigurationDto afterActionInstanceId = configurationService.getByCode(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_INSTANCE_ID);
		Assert.assertNotNull(afterActionInstanceId);
		Assert.assertEquals(instanceId.getId(), afterActionInstanceId.getId());
		Assert.assertEquals(eventInstanceId, afterActionInstanceId.getValue());
	}
	
	@Test
	public void testNotReuseAsynchronousEventInstanceId() {
		IdmConfigurationDto instanceId = configurationService.getByCode(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_INSTANCE_ID);
		if (instanceId != null) {
			configurationService.delete(instanceId);
		}
		//
		String newInstanceId = getHelper().createName();
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmConfiguration.class, ConfigurationSwitchInstanceBulkAction.NAME);
		Map<String, Object> properties = new HashMap<>();
		properties.put(ConfigurationSwitchInstanceBulkAction.PROPERTY_PREVIOUS_INSTANCE_ID, newInstanceId);
		properties.put(ConfigurationSwitchInstanceBulkAction.PROPERTY_NEW_INSTANCE_ID, newInstanceId);
		bulkAction.setProperties(properties);
		// prevalidate
		bulkActionManager.prevalidate(bulkAction);
		// change
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, null, null, 1l);
		//
		Assert.assertNull(configurationService.getByCode(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_INSTANCE_ID));
	}
	
	@Test
	public void testDefaultInstanceId() {
		String previousInstanceId = configurationService.getInstanceId();
		configurationService.setValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_INSTANCE_ID, previousInstanceId);
		//
		Assert.assertEquals(0, schedulerManager.switchInstanceId(previousInstanceId, null));
		Assert.assertEquals(0, taskManager.switchInstanceId(previousInstanceId, null));
		Assert.assertEquals(0, eventManager.switchInstanceId(previousInstanceId, null));
	}
	
	private IdmEntityEventDto createEvent(String instanceId) {
		IdmEntityEventDto dto = new IdmEntityEventDto();
		dto.setOwnerId(UUID.randomUUID());
		dto.setOwnerType("mock");
		dto.setInstanceId(instanceId);
		dto.setPriority(PriorityType.NORMAL);
		dto.setResult(new OperationResultDto(OperationState.CREATED));
		//
		return entityEventService.save(dto);
	}
	
	private Task createScheduledTask(String instanceId) {
		Task task = new Task();
		task.setInstanceId(instanceId);
		task.setTaskType(TestSchedulableTask.class);
		task.setDescription("test");
		//
		return schedulerManager.createTask(task);
	}
	
	private IdmLongRunningTaskDto createTask(String instanceId) {
		IdmLongRunningTaskDto dto = new IdmLongRunningTaskDto();
		dto.setTaskType("mock");
		dto.setInstanceId(instanceId);
		dto.setResult(new OperationResult(OperationState.CREATED));
		//
		return longRunningTaskService.save(dto);
	}
}
