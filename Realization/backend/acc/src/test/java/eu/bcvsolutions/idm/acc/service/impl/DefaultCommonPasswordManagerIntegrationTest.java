package eu.bcvsolutions.idm.acc.service.impl;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncContractConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.TestContractResource;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.event.processor.IdentityInitCommonPasswordProcessor;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.SynchronizationSchedulableTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.mock.MockIdentityInitCommonPasswordProcessor;
import eu.bcvsolutions.idm.core.api.CoreModule;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmScheduledTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.hr.HrContractExclusionProcess;
import eu.bcvsolutions.idm.core.scheduler.task.impl.hr.HrEnableContractProcess;
import eu.bcvsolutions.idm.core.scheduler.task.impl.hr.HrEndContractProcess;
import eu.bcvsolutions.idm.core.security.api.service.CommonPasswordManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for manager for a common password of identity.
 *
 * @author Vít Švanda
 * @since 11.0.0
 */
public class DefaultCommonPasswordManagerIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired
	private TestHelper helper;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemAttributeMappingService schemaAttributeMappingService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private SysSyncConfigService syncConfigService;
	@Autowired
	private SysSyncLogService syncLogService;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private SchedulerManager schedulerService;
	@Autowired
	private IdmScheduledTaskService scheduledService;
	@Autowired
	private LongRunningTaskManager longRunningTaskManager;
	@Autowired
	private IdmLongRunningTaskService longRunningTaskService;
	@Autowired
	private CommonPasswordManager commonPasswordManager;
	@Autowired
	private SchedulerManager schedulerManager;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private MockIdentityInitCommonPasswordProcessor mockIdentityInitCommonPasswordProcessor;
	@Autowired
	private IdmNotificationLogService notificationLogService;

	@Before
	public void init() {

		if (findTask(HrEnableContractProcess.class) == null) {
			createTask(HrEnableContractProcess.class);
		}
		if (findTask(HrContractExclusionProcess.class) == null) {
			createTask(HrContractExclusionProcess.class);
		}
		if (findTask(HrEndContractProcess.class) == null) {
			createTask(HrEndContractProcess.class);
		}

		Task task = findTask(HrEnableContractProcess.class);
		IdmScheduledTaskDto scheduledTask = null;
		if (scheduledService.findByQuartzTaskName(task.getId()) == null) {
			scheduledTask = new IdmScheduledTaskDto();
			scheduledTask.setQuartzTaskName(task.getId());
			scheduledService.save(scheduledTask);
		}

		task = findTask(HrEndContractProcess.class);
		if (scheduledService.findByQuartzTaskName(task.getId()) == null) {
			scheduledTask = new IdmScheduledTaskDto();
			scheduledTask.setQuartzTaskName(task.getId());
			scheduledService.save(scheduledTask);
		}

		task = findTask(HrContractExclusionProcess.class);
		if (scheduledService.findByQuartzTaskName(task.getId()) == null) {
			scheduledTask = new IdmScheduledTaskDto();
			scheduledTask.setQuartzTaskName(task.getId());
			scheduledService.save(scheduledTask);
		}
	}

	@Test
	public void testCreateEntityState() {
		try {
			// Turn on an async execution.
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
			getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, true);

			SysSystemDto system = initData();
			Assert.assertNotNull(system);
			AbstractSysSyncConfigDto config = doCreateSyncConfig(system, null);
			Assert.assertTrue(config instanceof SysSyncContractConfigDto);

			IdmIdentityDto ownerOne = helper.createIdentity();
			IdmIdentityDto ownerTwo = helper.createIdentity();

			List<TestContractResource> contractResources = Lists.newArrayList(
					this.createContract("1", ownerOne.getUsername(), null, "true", null, null, null, null),
					this.createContract("2", ownerTwo.getUsername(), null, "false", null, null, null, null)
			);
			this.getBean().initContractData(contractResources);

			IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
			contractFilter.setIdentity(ownerOne.getId());
			contractService.find(contractFilter, null)
					.getContent()
					.forEach(contract -> contractService.delete(contract));
			Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());

			contractFilter.setIdentity(ownerTwo.getId());
			contractService.find(contractFilter, null)
					.getContent()
					.forEach(contract -> contractService.delete(contract));
			Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());

			ownerOne = identityService.get(ownerOne.getId());
			ownerTwo = identityService.get(ownerTwo.getId());
			// Identities should be in the CREATED state.
			Assert.assertEquals(IdentityState.NO_CONTRACT, ownerOne.getState());
			Assert.assertEquals(IdentityState.NO_CONTRACT, ownerTwo.getState());

			SynchronizationSchedulableTaskExecutor lrt = new SynchronizationSchedulableTaskExecutor(config.getId());
			LongRunningFutureTask<Boolean> longRunningFutureTask = longRunningTaskManager.execute(lrt);
			UUID transactionIdLrt = longRunningTaskService.get(longRunningFutureTask.getExecutor().getLongRunningTaskId()).getTransactionId();

			// Enable test processor only for this transaction.
			mockIdentityInitCommonPasswordProcessor.setEnableTestForTransaction(transactionIdLrt);

			// Waiting for the LRT will be running.
			getHelper().waitForResult(res -> {
				return !longRunningTaskService.get(longRunningFutureTask.getExecutor().getLongRunningTaskId()).isRunning();
			}, 50, 40);

			// Waiting for the LRT will be EXECUTED.
			getHelper().waitForResult(res -> {
				return longRunningTaskService.get(longRunningFutureTask.getExecutor().getLongRunningTaskId()).getResultState() != OperationState.EXECUTED;
			}, 250, 100);

			Assert.assertEquals(longRunningTaskService.get(longRunningFutureTask.getExecutor().getLongRunningTaskId()).getResultState(), OperationState.EXECUTED);
			SysSyncLogDto log = helper.checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 2, OperationResultType.SUCCESS);

			Assert.assertFalse(log.isRunning());
			Assert.assertFalse(log.isContainsError());
			UUID transactionId = log.getTransactionId();
			Assert.assertNotNull(transactionId);
			Assert.assertEquals(transactionIdLrt, transactionId);

			contractFilter.setIdentity(ownerOne.getId());
			Assert.assertEquals(1, contractService.count(contractFilter));

			contractFilter.setIdentity(ownerTwo.getId());
			Assert.assertEquals(1, contractService.count(contractFilter));

			ownerOne = identityService.get(ownerOne.getId());
			ownerTwo = identityService.get(ownerTwo.getId());
			// Identities should have a valid state.
			Assert.assertEquals(IdentityState.VALID, ownerOne.getState());
			Assert.assertEquals(IdentityState.VALID, ownerTwo.getState());

			// LRT ended, entityStates must be removed.
			IdmEntityStateDto entityStateDtoOwnerOne = commonPasswordManager.getEntityState(ownerOne.getId(), IdmIdentityDto.class, transactionId);
			Assert.assertNull(entityStateDtoOwnerOne);
			IdmEntityStateDto entityStateDtoOwnerTwo = commonPasswordManager.getEntityState(ownerTwo.getId(), IdmIdentityDto.class, transactionId);
			Assert.assertNull(entityStateDtoOwnerTwo);

			contractFilter.setValue("1");
			Assert.assertEquals(1, contractService.find(contractFilter, null).getTotalElements());
			contractFilter.setValue("2");
			List<IdmIdentityContractDto> contractsTwo = contractService.find(contractFilter, null).getContent();
			Assert.assertEquals(1, contractsTwo.size());

			// Delete log
			syncLogService.delete(log);
			// Delete identities.
			identityService.delete(ownerOne);
			identityService.delete(ownerTwo);
		} finally {
			// Turn off an async execution.
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, false);
			getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, false);
			// Disable test processor.
			mockIdentityInitCommonPasswordProcessor.setEnableTestForTransaction(null);
		}
	}


	@Test
	public void testCommonPassword() {
		try {
			// Turn on an async execution.
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
			getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, true);

			SysSystemDto contractSystem = initData();
			Assert.assertNotNull(contractSystem);
			IdmTreeTypeDto treeType = helper.createTreeType();
			AbstractSysSyncConfigDto config = doCreateSyncConfig(contractSystem, treeType);
			Assert.assertTrue(config instanceof SysSyncContractConfigDto);

			SysSystemDto targetSystemOne = helper.createTestResourceSystem(true);
			// Create system two with account suffix "_targetSystemTwo".
			String targetSystemTwoSuffix = "_targetSystemTwo";
			SysSystemDto targetSystemTwo = helper.createTestResourceSystem(true);
			SysSystemMappingDto provisioningMapping = systemMappingService.findProvisioningMapping(targetSystemTwo.getId(), SystemEntityType.IDENTITY);
			List<SysSystemAttributeMappingDto> attributeMappingDtos = schemaAttributeMappingService.findBySystemMapping(provisioningMapping);
			SysSystemAttributeMappingDto uidAttribute = schemaAttributeMappingService.getUidAttribute(attributeMappingDtos, targetSystemTwo);
			uidAttribute.setTransformToResourceScript("return attributeValue + \"" + targetSystemTwoSuffix + "\"");
			schemaAttributeMappingService.save(uidAttribute);

			IdmRoleDto automaticRoleTreeOne = helper.createRole();
			helper.createRoleSystem(automaticRoleTreeOne, targetSystemOne);
			IdmTreeNodeDto treeNodeOne = helper.createTreeNode(treeType, null);
			helper.createAutomaticRole(automaticRoleTreeOne, treeNodeOne);

			IdmRoleDto automaticRoleTreeTwo = helper.createRole();
			helper.createRoleSystem(automaticRoleTreeTwo, targetSystemTwo);
			IdmTreeNodeDto treeNodeTwo = helper.createTreeNode(treeType, null);
			helper.createAutomaticRole(automaticRoleTreeTwo, treeNodeTwo);

			IdmIdentityDto ownerOne = helper.createIdentityOnly();

			List<TestContractResource> contractResources = Lists.newArrayList(
					this.createContract("1", ownerOne.getUsername(), null, "true", treeNodeOne.getCode(), null, null, null),
					this.createContract("2", ownerOne.getUsername(), null, "false", treeNodeTwo.getCode(), null, null, null)
			);
			this.getBean().initContractData(contractResources);

			IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
			contractFilter.setIdentity(ownerOne.getId());
			contractService.find(contractFilter, null)
					.getContent()
					.forEach(contract -> contractService.delete(contract));
			Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());

			ownerOne = identityService.get(ownerOne.getId());
			// Identities should be in the CREATED state.
			Assert.assertEquals(IdentityState.CREATED, ownerOne.getState());

			SynchronizationSchedulableTaskExecutor lrt = new SynchronizationSchedulableTaskExecutor(config.getId());
			LongRunningFutureTask<Boolean> longRunningFutureTask = longRunningTaskManager.execute(lrt);
			UUID transactionIdLrt = longRunningTaskService.get(longRunningFutureTask.getExecutor().getLongRunningTaskId()).getTransactionId();

			// Enable test processor only for this transaction.
			mockIdentityInitCommonPasswordProcessor.setEnableTestForTransaction(transactionIdLrt);

			// Waiting for the LRT will be running.
			getHelper().waitForResult(res -> {
				return !longRunningTaskService.get(longRunningFutureTask.getExecutor().getLongRunningTaskId()).isRunning();
			}, 50, 40);

			// Waiting for the LRT will be EXECUTED.
			getHelper().waitForResult(res -> {
				return longRunningTaskService.get(longRunningFutureTask.getExecutor().getLongRunningTaskId()).getResultState() != OperationState.EXECUTED;
			}, 250, 100);

			Assert.assertEquals(longRunningTaskService.get(longRunningFutureTask.getExecutor().getLongRunningTaskId()).getResultState(), OperationState.EXECUTED);
			SysSyncLogDto log = helper.checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 2, OperationResultType.SUCCESS);

			Assert.assertFalse(log.isRunning());
			Assert.assertFalse(log.isContainsError());
			UUID transactionId = log.getTransactionId();
			Assert.assertNotNull(transactionId);
			Assert.assertEquals(transactionIdLrt, transactionId);

			contractFilter.setIdentity(ownerOne.getId());
			Assert.assertEquals(2, contractService.count(contractFilter));

			ownerOne = identityService.get(ownerOne.getId());
			// Identities should have a valid state.
			Assert.assertEquals(IdentityState.VALID, ownerOne.getState());

			// Waiting for removing entity state.
			IdmIdentityDto finalOwnerOne = ownerOne;
			getHelper().waitForResult(res -> {
				return commonPasswordManager.getEntityState(finalOwnerOne.getId(), IdmIdentityDto.class, transactionId) != null;
			}, 50, 100);
			// LRT ended, entityStates must be removed.
			IdmEntityStateDto entityStateDtoOwnerOne = commonPasswordManager.getEntityState(ownerOne.getId(), IdmIdentityDto.class, transactionId);
			Assert.assertNull(entityStateDtoOwnerOne);

			TestResource resourceOwnerOne = helper.findResource(ownerOne.getUsername());
			Assert.assertNotNull(resourceOwnerOne);
			TestResource resourceOwnerTwo = helper.findResource(ownerOne.getUsername() + targetSystemTwoSuffix);
			Assert.assertNotNull(resourceOwnerTwo);

			String passwordOwnerOne = resourceOwnerOne.getPassword();
			String passwordOwnerTwo = resourceOwnerTwo.getPassword();
			Assert.assertNotNull(passwordOwnerOne);
			Assert.assertNotNull(passwordOwnerTwo);
			Assert.assertEquals(passwordOwnerOne, passwordOwnerTwo);

			// One common password notification was send.
			IdmNotificationFilter notificationFilter = new IdmNotificationFilter();
			notificationFilter.setRecipient(ownerOne.getUsername());
			notificationFilter.setNotificationType(IdmEmailLog.class);
			notificationFilter.setTopic(CoreModule.TOPIC_COMMON_PASSWORD_SET);
			List<IdmNotificationLogDto> notificationLogDtos = notificationLogService.find(notificationFilter, null).getContent();
			Assert.assertEquals(1, notificationLogDtos.size());

			// None a new password notification was send.
			notificationFilter.setTopic(AccModuleDescriptor.TOPIC_NEW_PASSWORD);
			notificationLogDtos = notificationLogService.find(notificationFilter, null).getContent();
			Assert.assertEquals(0, notificationLogDtos.size());

			// None password set notification was send.
			notificationFilter.setTopic(CoreModule.TOPIC_PASSWORD_SET);
			notificationLogDtos = notificationLogService.find(notificationFilter, null).getContent();
			Assert.assertEquals(0, notificationLogDtos.size());

			// None password change notification was send.
			notificationFilter.setTopic(CoreModule.TOPIC_PASSWORD_CHANGED);
			notificationLogDtos = notificationLogService.find(notificationFilter, null).getContent();
			Assert.assertEquals(0, notificationLogDtos.size());

			// Delete log
			syncLogService.delete(log);
			// Delete identities.
			identityService.delete(ownerOne);
		} finally {
			// Turn off an async execution.
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, false);
			getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, false);
			// Disable test processor.
			mockIdentityInitCommonPasswordProcessor.setEnableTestForTransaction(null);
		}
	}
	
	@Test
	public void testDisableCommonPassword() {
		try {
			// Disable the IdentityInitCommonPasswordProcessor processor -> state will be not created -> feature common password have to be disabled.
			getHelper().disableProcessor(IdentityInitCommonPasswordProcessor.PROCESSOR_NAME);
			// Turn on an async execution.
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
			getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, true);

			SysSystemDto contractSystem = initData();
			Assert.assertNotNull(contractSystem);
			IdmTreeTypeDto treeType = helper.createTreeType();
			AbstractSysSyncConfigDto config = doCreateSyncConfig(contractSystem, treeType);
			Assert.assertTrue(config instanceof SysSyncContractConfigDto);

			SysSystemDto targetSystemOne = helper.createTestResourceSystem(true);
			// Create system two with account suffix "_targetSystemTwo".
			String targetSystemTwoSuffix = "_targetSystemTwo";
			SysSystemDto targetSystemTwo = helper.createTestResourceSystem(true);
			SysSystemMappingDto provisioningMapping = systemMappingService.findProvisioningMapping(targetSystemTwo.getId(), SystemEntityType.IDENTITY);
			List<SysSystemAttributeMappingDto> attributeMappingDtos = schemaAttributeMappingService.findBySystemMapping(provisioningMapping);
			SysSystemAttributeMappingDto uidAttribute = schemaAttributeMappingService.getUidAttribute(attributeMappingDtos, targetSystemTwo);
			uidAttribute.setTransformToResourceScript("return attributeValue + \"" + targetSystemTwoSuffix + "\"");
			schemaAttributeMappingService.save(uidAttribute);

			IdmRoleDto automaticRoleTreeOne = helper.createRole();
			helper.createRoleSystem(automaticRoleTreeOne, targetSystemOne);
			IdmTreeNodeDto treeNodeOne = helper.createTreeNode(treeType, null);
			helper.createAutomaticRole(automaticRoleTreeOne, treeNodeOne);

			IdmRoleDto automaticRoleTreeTwo = helper.createRole();
			helper.createRoleSystem(automaticRoleTreeTwo, targetSystemTwo);
			IdmTreeNodeDto treeNodeTwo = helper.createTreeNode(treeType, null);
			helper.createAutomaticRole(automaticRoleTreeTwo, treeNodeTwo);

			IdmIdentityDto ownerOne = helper.createIdentityOnly();

			List<TestContractResource> contractResources = Lists.newArrayList(
					this.createContract("1", ownerOne.getUsername(), null, "true", treeNodeOne.getCode(), null, null, null),
					this.createContract("2", ownerOne.getUsername(), null, "false", treeNodeTwo.getCode(), null, null, null)
			);
			this.getBean().initContractData(contractResources);

			IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
			contractFilter.setIdentity(ownerOne.getId());
			contractService.find(contractFilter, null)
					.getContent()
					.forEach(contract -> contractService.delete(contract));
			Assert.assertEquals(0, contractService.find(contractFilter, null).getTotalElements());

			ownerOne = identityService.get(ownerOne.getId());
			// Identities should be in the CREATED state.
			Assert.assertEquals(IdentityState.CREATED, ownerOne.getState());

			SynchronizationSchedulableTaskExecutor lrt = new SynchronizationSchedulableTaskExecutor(config.getId());
			LongRunningFutureTask<Boolean> longRunningFutureTask = longRunningTaskManager.execute(lrt);
			UUID transactionIdLrt = longRunningTaskService.get(longRunningFutureTask.getExecutor().getLongRunningTaskId()).getTransactionId();

			// Waiting for the LRT will be running.
			getHelper().waitForResult(res -> {
				return !longRunningTaskService.get(longRunningFutureTask.getExecutor().getLongRunningTaskId()).isRunning();
			}, 50, 40);

			// Waiting for the LRT will be EXECUTED.
			getHelper().waitForResult(res -> {
				return longRunningTaskService.get(longRunningFutureTask.getExecutor().getLongRunningTaskId()).getResultState() != OperationState.EXECUTED;
			}, 250, 100);

			Assert.assertEquals(longRunningTaskService.get(longRunningFutureTask.getExecutor().getLongRunningTaskId()).getResultState(), OperationState.EXECUTED);
			SysSyncLogDto log = helper.checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 2, OperationResultType.SUCCESS);

			Assert.assertFalse(log.isRunning());
			Assert.assertFalse(log.isContainsError());
			UUID transactionId = log.getTransactionId();
			Assert.assertNotNull(transactionId);
			Assert.assertEquals(transactionIdLrt, transactionId);

			contractFilter.setIdentity(ownerOne.getId());
			Assert.assertEquals(2, contractService.count(contractFilter));

			ownerOne = identityService.get(ownerOne.getId());
			// Identities should have a valid state.
			Assert.assertEquals(IdentityState.VALID, ownerOne.getState());

			// Common password feature is disabled -> password could be not same.
			IdmEntityStateDto entityStateDtoOwnerOne = commonPasswordManager.getEntityState(ownerOne.getId(), IdmIdentityDto.class, transactionId);
			Assert.assertNull(entityStateDtoOwnerOne);

			TestResource resourceOwnerOne = helper.findResource(ownerOne.getUsername());
			Assert.assertNotNull(resourceOwnerOne);
			TestResource resourceOwnerTwo = helper.findResource(ownerOne.getUsername() + targetSystemTwoSuffix);
			Assert.assertNotNull(resourceOwnerTwo);

			String passwordOwnerOne = resourceOwnerOne.getPassword();
			String passwordOwnerTwo = resourceOwnerTwo.getPassword();
			Assert.assertNotNull(passwordOwnerOne);
			Assert.assertNotNull(passwordOwnerTwo);
			// Common password feature is disabled -> password cannot be not same.
			Assert.assertNotEquals(passwordOwnerOne, passwordOwnerTwo);

			// None a common password notification was send.
			IdmNotificationFilter notificationFilter = new IdmNotificationFilter();
			notificationFilter.setRecipient(ownerOne.getUsername());
			notificationFilter.setNotificationType(IdmEmailLog.class);
			notificationFilter.setTopic(CoreModule.TOPIC_COMMON_PASSWORD_SET);
			List<IdmNotificationLogDto> notificationLogDtos = notificationLogService.find(notificationFilter, null).getContent();
			Assert.assertEquals(0, notificationLogDtos.size());

			// None a new password notification was send.
			notificationFilter.setTopic(AccModuleDescriptor.TOPIC_NEW_PASSWORD);
			notificationLogDtos = notificationLogService.find(notificationFilter, null).getContent();
			Assert.assertEquals(2, notificationLogDtos.size());

			// None a password change notification was send.
			notificationFilter.setTopic(CoreModule.TOPIC_PASSWORD_SET);
			notificationLogDtos = notificationLogService.find(notificationFilter, null).getContent();
			Assert.assertEquals(0, notificationLogDtos.size());

			// Delete log
			syncLogService.delete(log);
			// Delete identities.
			identityService.delete(ownerOne);
		} finally {
			// Turn off an async execution.
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, false);
			getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, false);
			// Enable processor.
			getHelper().enableProcessor(IdentityInitCommonPasswordProcessor.PROCESSOR_NAME);
		}
	}

	public AbstractSysSyncConfigDto doCreateSyncConfig(SysSystemDto system, IdmTreeTypeDto treeType) {

		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setEntityType(SystemEntityType.CONTRACT);
		mappingFilter.setSystemId(system.getId());
		mappingFilter.setOperationType(SystemOperationType.SYNCHRONIZATION);
		List<SysSystemMappingDto> mappings = systemMappingService.find(mappingFilter, null).getContent();
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto mapping = mappings.get(0);
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mapping.getId());

		List<SysSystemAttributeMappingDto> attributes = schemaAttributeMappingService.find(attributeMappingFilter, null)
				.getContent();
		SysSystemAttributeMappingDto uidAttribute = attributes.stream().filter(attribute -> {
			return attribute.isUid();
		}).findFirst().orElse(null);

		// Create default synchronization config
		SysSyncContractConfigDto syncConfigCustom = new SysSyncContractConfigDto();
		syncConfigCustom.setReconciliation(true);
		syncConfigCustom.setCustomFilter(false);
		syncConfigCustom.setSystemMapping(mapping.getId());
		syncConfigCustom.setCorrelationAttribute(uidAttribute.getId());
		syncConfigCustom.setName(this.getHelper().createName());
		syncConfigCustom.setLinkedAction(SynchronizationLinkedActionType.UPDATE_ENTITY);
		syncConfigCustom.setUnlinkedAction(SynchronizationUnlinkedActionType.LINK_AND_UPDATE_ENTITY);
		syncConfigCustom.setMissingEntityAction(SynchronizationMissingEntityActionType.CREATE_ENTITY);
		syncConfigCustom.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);
		syncConfigCustom.setStartOfHrProcesses(true);
		syncConfigCustom.setStartAutoRoleRec(true);
		if (treeType != null) {
			syncConfigCustom.setDefaultTreeType(treeType.getId());
		}

		syncConfigCustom = (SysSyncContractConfigDto) syncConfigService.save(syncConfigCustom);

		SysSyncConfigFilter configFilter = new SysSyncConfigFilter();
		configFilter.setSystemId(system.getId());
		Assert.assertEquals(1, syncConfigService.find(configFilter, null).getTotalElements());
		return syncConfigCustom;
	}

	private TestContractResource createContract(
			String code, String owner, String leader, String main,
			String workposition, String state, String disabled,
			String positions) {
		TestContractResource contract = new TestContractResource();
		contract.setId(code);
		contract.setName(code);
		contract.setOwner(owner);
		contract.setState(state);
		contract.setDisabled(disabled);
		contract.setLeader(leader);
		contract.setMain(main);
		contract.setWorkposition(workposition);
		contract.setDescription(code);
		contract.setPositions(positions);
		return contract;
	}

	private SysSystemDto initData() {
		// create test system
		SysSystemDto system = helper.createSystem(TestContractResource.TABLE_NAME, null, null, "ID");
		Assert.assertNotNull(system);

		// generate schema for system
		List<SysSchemaObjectClassDto> objectClasses = systemService.generateSchema(system);

		// Create synchronization mapping
		SysSystemMappingDto syncSystemMapping = new SysSystemMappingDto();
		syncSystemMapping.setName("default_" + System.currentTimeMillis());
		syncSystemMapping.setEntityType(SystemEntityType.CONTRACT);
		syncSystemMapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		syncSystemMapping.setObjectClass(objectClasses.get(0).getId());
		final SysSystemMappingDto syncMapping = systemMappingService.save(syncSystemMapping);

		createMapping(system, syncMapping);
		return system;

	}

	@Transactional
	public void createContractData(
			String code, String owner, String leader, String main,
			String workposition, String state, String disabled) {
		if (code == null) {
			code = String.valueOf(System.currentTimeMillis());
		}
		entityManager.persist(this.createContract(code, owner, leader, main, workposition, state, disabled, null));
	}

	@Transactional
	public void createContractData(TestContractResource contract) {
		entityManager.persist(contract);
	}

	@Transactional
	public void initContractData(List<TestContractResource> contractResources) {
		deleteAllResourceData();
		contractResources.forEach(contractResource -> {
			entityManager.persist(contractResource);
		});
	}

	private void createMapping(SysSystemDto system, final SysSystemMappingDto entityHandlingResult) {
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());

		Page<SysSchemaAttributeDto> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		schemaAttributesPage.forEach(schemaAttr -> {
			if ("id".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setUid(true);
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				attributeHandlingName.setIdmPropertyName(IdmIdentityContract_.description.getName()); // it is for link and update situation
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("name".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName("position");
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("owner".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName(ContractSynchronizationExecutor.CONTRACT_IDENTITY_FIELD);
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("workposition".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName(ContractSynchronizationExecutor.CONTRACT_WORK_POSITION_FIELD);
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("leader".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName(ContractSynchronizationExecutor.CONTRACT_GUARANTEES_FIELD);
				attributeHandlingName.setName(schemaAttr.getName().toLowerCase());
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("positions".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName(ContractSynchronizationExecutor.CONTRACT_POSITIONS_FIELD);
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("modified".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setEntityAttribute(false);
				attributeHandlingName.setExtendedAttribute(false);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);
			} else if ("validfrom".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setIdmPropertyName("validFrom");
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setExtendedAttribute(false);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				attributeHandlingName.setTransformFromResourceScript(
						"return attributeValue == null ? null : java.time.LocalDate.parse(attributeValue);");
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("validtill".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setIdmPropertyName("validTill");
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setExtendedAttribute(false);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				attributeHandlingName.setTransformFromResourceScript(
						"return attributeValue == null ? null : java.time.LocalDate.parse(attributeValue);");
				schemaAttributeMappingService.save(attributeHandlingName);

			} else if ("description".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeHandlingName = new SysSystemAttributeMappingDto();
				attributeHandlingName.setIdmPropertyName("description");
				attributeHandlingName.setName(schemaAttr.getName());
				attributeHandlingName.setEntityAttribute(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingName.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeHandlingName);
			}
		});
	}

	@Transactional
	public void deleteAllResourceData() {
		// Delete all
		Query q = entityManager.createNativeQuery("DELETE FROM " + TestContractResource.TABLE_NAME);
		q.executeUpdate();
	}

	private DefaultCommonPasswordManagerIntegrationTest getBean() {
		return applicationContext.getAutowireCapableBeanFactory().createBean(this.getClass());
	}

	private void createTask(Class hrClass) {
		Task task = new Task();
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(hrClass);
		task.setDescription("test");
		schedulerManager.createTask(task);
	}

	private Task findTask(Class<? extends SchedulableTaskExecutor<?>> taskType) {
		List<Task> tasks = schedulerService.getAllTasksByType(taskType);
		if (tasks.size() == 1) {
			return tasks.get(0);
		}
		if (tasks.isEmpty()) {
			return null;
		}

		Task defaultTask = tasks.stream().filter(task -> {
			return task.getDescription().equals("Default");
		}).findFirst().orElse(null);
		if (defaultTask != null) {
			return defaultTask;
		}
		return tasks.get(0);
	}

}
