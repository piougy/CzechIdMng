package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncIdentityConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * LRT integration test.
 * 
 * @author Radek Tomiška
 *
 */
public class DeleteSynchronizationLogTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private SysSyncLogService service;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	@Autowired private SysSyncConfigService syncConfigService;
	@Autowired private SysSystemAttributeMappingService attributeMappingService;
	
	@Test
	public void testDeleteOldSynchronizationLogs() {
		// prepare provisioning operations
		SysSystemDto systemOne = getHelper().createTestResourceSystem(true);
		AbstractSysSyncConfigDto syncConfigOne = createSyncConfig(systemOne);
		SysSystemDto systemOther = getHelper().createTestResourceSystem(true);
		AbstractSysSyncConfigDto syncConfigOther = createSyncConfig(systemOther);
		//
		ZonedDateTime createdOne = ZonedDateTime.now().minusDays(2);
		SysSyncLogDto logOne = createDto(syncConfigOne, createdOne);
		// all other variants for not removal
		createDto(syncConfigOne, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusMinutes(1));
		createDto(syncConfigOne, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusHours(23));
		SysSyncLogDto logOther = createDto(syncConfigOther, ZonedDateTime.now().minusDays(2));
		//
		Assert.assertEquals(createdOne, logOne.getCreated());
		SysSyncLogFilter filter = new SysSyncLogFilter();
		filter.setSystemId(systemOne.getId());
		List<SysSyncLogDto> logs = service.find(filter, null).getContent();
		Assert.assertEquals(3, logs.size());
		filter.setSystemId(systemOther.getId());
		logs = service.find(filter, null).getContent();
		Assert.assertEquals(1, logs.size());
		//
		DeleteSynchronizationLogTaskExecutor taskExecutor = new DeleteSynchronizationLogTaskExecutor();
		Map<String, Object> properties = new HashMap<>();
		properties.put(DeleteSynchronizationLogTaskExecutor.PARAMETER_NUMBER_OF_DAYS, 1);
		properties.put(DeleteSynchronizationLogTaskExecutor.PARAMETER_SYSTEM, systemOne.getId());
		AutowireHelper.autowire(taskExecutor);
		taskExecutor.init(properties);
		//
		longRunningTaskManager.execute(taskExecutor);
		//
		filter.setSystemId(systemOne.getId());
		logs = service.find(filter, null).getContent();
		Assert.assertEquals(2, logs.size());
		Assert.assertTrue(logs.stream().allMatch(a -> !a.getId().equals(logOne.getId())));
		//
		filter.setSystemId(systemOther.getId());
		logs = service.find(filter, null).getContent();
		Assert.assertEquals(1, logs.size());
		Assert.assertTrue(logs.stream().allMatch(a -> a.getId().equals(logOther.getId())));
		//
		taskExecutor = new DeleteSynchronizationLogTaskExecutor();
		properties = new HashMap<>();
		properties.put(DeleteSynchronizationLogTaskExecutor.PARAMETER_NUMBER_OF_DAYS, 1);
		properties.put(DeleteSynchronizationLogTaskExecutor.PARAMETER_SYSTEM, systemOther.getId());
		AutowireHelper.autowire(taskExecutor);
		taskExecutor.init(properties);
		//
		filter.setSystemId(systemOne.getId());
		longRunningTaskManager.execute(taskExecutor);
		logs = service.find(filter, null).getContent();
		Assert.assertEquals(2, logs.size());
		Assert.assertTrue(logs.stream().allMatch(a -> !a.getId().equals(logOne.getId())));
		//
		filter.setSystemId(systemOther.getId());
		logs = service.find(filter, null).getContent();
		Assert.assertTrue(logs.isEmpty());
	}
	
	private SysSyncLogDto createDto(
			AbstractSysSyncConfigDto syncConfig, 
			ZonedDateTime created) {
		SysSyncLogDto dto = new SysSyncLogDto();
		dto.setCreated(created);
		dto.setSynchronizationConfig(syncConfig.getId());
		//
		return service.save(dto);
	}
	
	private AbstractSysSyncConfigDto createSyncConfig(SysSystemDto system) {
		SysSystemMappingDto mapping = getHelper().getDefaultMapping(system);
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mapping.getId());

		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(attributeMappingFilter, null)
				.getContent();
		SysSystemAttributeMappingDto nameAttribute = attributes.stream().filter(attribute -> {
			return attribute.getName().equals(TestHelper.ATTRIBUTE_MAPPING_NAME);
		}).findFirst().get();
		//
		// Create default synchronization config
		AbstractSysSyncConfigDto syncConfig = new SysSyncIdentityConfigDto();
		syncConfig.setSystemMapping(mapping.getId());
		syncConfig.setCorrelationAttribute(nameAttribute.getId());
		syncConfig.setReconciliation(true);
		syncConfig.setName(getHelper().createName());
		syncConfig.setLinkedAction(SynchronizationLinkedActionType.IGNORE);
		syncConfig.setUnlinkedAction(SynchronizationUnlinkedActionType.IGNORE);
		syncConfig.setMissingEntityAction(SynchronizationMissingEntityActionType.IGNORE);
		syncConfig.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);
		//
		return syncConfigService.save(syncConfig);
	}
	
	@Override
	protected TestHelper getHelper() {
		return (TestHelper) super.getHelper();
	}
}
