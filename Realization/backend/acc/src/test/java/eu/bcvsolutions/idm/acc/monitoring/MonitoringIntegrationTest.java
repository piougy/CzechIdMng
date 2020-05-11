package eu.bcvsolutions.idm.acc.monitoring;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncContractConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.event.processor.MonitoringSyncProcessor;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.MonitoringLevel;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringTypeDto;
import eu.bcvsolutions.idm.core.api.service.MonitoringManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.util.UUID;

/**
 * Monitoring integration test
 *
 * @author Vít Švanda
 *
 */
public class MonitoringIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private MonitoringManager monitoringManager;
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

	@Before
	public void login() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	/**
	 * Green line test for DB count.
	 */
	@Test
	public void testDatabaseCount() {
		IdmMonitoringTypeDto monitoringType = monitoringManager.check(MonitoringManager.MONITORING_TYPE_DATABASE);
		Assert.assertNotNull(monitoringType);
		List<IdmMonitoringResultDto> results = monitoringType.getResults();

		long countOfCoreTables = results.stream()
				.filter(result -> "acc".equals(result.getModule()))
				.count();

		Assert.assertEquals(2, countOfCoreTables);
	}

	/**
	 * Green line test for sync state.
	 */
	@Test
	public void testSyncState() {
		IdmMonitoringTypeDto monitoringType = monitoringManager.check(MonitoringSyncProcessor.MONITORING_TYPE_SYNC);
		Assert.assertNotNull(monitoringType);
		List<IdmMonitoringResultDto> results = monitoringType.getResults();

		long numberOfSyncBeforeTest = results.stream()
				.filter(result -> "acc".equals(result.getModule()))
				.count();

		SysSystemDto system = this.helper.createTestResourceSystem(true);
		AbstractSysSyncConfigDto syncConfig = this.createSyncConfig(system);
		SysSyncLogDto syncLogDto = new SysSyncLogDto();
		syncLogDto.setSynchronizationConfig(syncConfig.getId());
		syncLogDto.setContainsError(true);
		syncLogService.save(syncLogDto);
		
		monitoringType = monitoringManager.check(MonitoringSyncProcessor.MONITORING_TYPE_SYNC);
		Assert.assertNotNull(monitoringType);
		results = monitoringType.getResults();
		long numberOfSync = results.stream()
				.filter(result -> "acc".equals(result.getModule()))
				.count();

		Assert.assertEquals(numberOfSyncBeforeTest + 1, numberOfSync);
		IdmMonitoringResultDto resultWithSync = results.stream()
				.filter(result -> "acc".equals(result.getModule()))
				.filter(result -> syncConfig.getId().equals(result.getDto().getId()))
				.findFirst().get();
		
		Assert.assertEquals(MonitoringLevel.ERROR, resultWithSync.getLevel());
		
	}

	private AbstractSysSyncConfigDto createSyncConfig(SysSystemDto system) {

		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setEntityType(SystemEntityType.IDENTITY);
		mappingFilter.setSystemId(system.getId());
		mappingFilter.setOperationType(SystemOperationType.PROVISIONING);
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
		AbstractSysSyncConfigDto syncConfigCustom = new SysSyncContractConfigDto();
		syncConfigCustom.setReconciliation(true);
		syncConfigCustom.setCustomFilter(false);
		syncConfigCustom.setSystemMapping(mapping.getId());
		syncConfigCustom.setCorrelationAttribute(uidAttribute.getId());
		syncConfigCustom.setName(helper.createName());
		syncConfigCustom.setLinkedAction(SynchronizationLinkedActionType.UPDATE_ENTITY);
		syncConfigCustom.setUnlinkedAction(SynchronizationUnlinkedActionType.IGNORE);
		syncConfigCustom.setMissingEntityAction(SynchronizationMissingEntityActionType.CREATE_ENTITY);
		syncConfigCustom.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);

		syncConfigCustom = syncConfigService.save(syncConfigCustom);

		SysSyncConfigFilter configFilter = new SysSyncConfigFilter();
		configFilter.setSystemId(system.getId());
		Assert.assertEquals(1, syncConfigService.find(configFilter, null).getTotalElements());
		return syncConfigCustom;
	}

}
