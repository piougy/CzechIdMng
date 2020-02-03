package eu.bcvsolutions.idm.acc.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableSet;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link SystemMappingDeleteBulkAction}
 *
 * @author Ondrej Husnik
 *
 */

public class SystemMappingDeleteBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private SysSyncConfigService syncService;
	@Autowired
	private SysSystemMappingService mappingService;
	@Autowired
	private SysSystemAttributeMappingService attrMappingService;

	@Before
	public void login() {
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.UPDATE, IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void processBulkActionByIds() {
		SysSystemDto system = helper.createTestResourceSystem(true, getHelper().createName());

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setSystemId(system.getId());
		List<SysSystemMappingDto> mapping = mappingService.find(filter, null).getContent();
		
		// Contains only one created system mapping
		assertEquals(1, mapping.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(SysSystemMapping.class, SystemMappingDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(ImmutableSet.of(mapping.get(0).getId()));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);

		checkResultLrt(processAction, 1l, null, null);

		mapping = mappingService.find(filter, null).getContent();
		// Contains no system mapping after deletion 
		assertEquals(0, mapping.size());
	}

	@Test
	public void prevalidationBulkActionByIds() {
		SysSystemDto system = helper.createTestResourceSystem(true, getHelper().createName());
		
		// Gets existing mapping in the system
		SysSystemMappingFilter systemFilter = new SysSystemMappingFilter();
		systemFilter.setSystemId(system.getId());
		List<SysSystemMappingDto> mapping = mappingService.find(systemFilter, null).getContent();
		
		// Tests that a mapping was found
		assertNotEquals(0, mapping.size());
		
		// Finds mapped attributes in existing system
		SysSystemAttributeMappingFilter attrMapFilter = new SysSystemAttributeMappingFilter();
		attrMapFilter.setSystemId(system.getId());
		List<SysSystemAttributeMappingDto> attrMapping = attrMappingService.find(attrMapFilter, null).getContent();
		SysSystemAttributeMappingDto attrMappingDto = attrMapping //
				.stream() //
				.filter(attrMap -> {return TestHelper.ATTRIBUTE_MAPPING_NAME.equals(attrMap.getName());}) //
				.findFirst() //
				.orElse(null);
		
		// Tests presence of desired mapped attribute  
		assertNotNull(attrMappingDto);
		
		// Running prevalidation which is supposed to return no validation errors 
		// because this mapping is not used in any synchronization settings.
		SysSystemMappingDto mappingDto = mapping.get(0);
		IdmBulkActionDto bulkAction = this.findBulkAction(SysSystemMapping.class, SystemMappingDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(ImmutableSet.of(mappingDto.getId()));
		ResultModels resultModels = bulkActionManager.prevalidate(bulkAction);
		assertEquals(0, resultModels.getInfos().size());
		
		// Creates synchronization with set existing mapping
		SysSyncConfigDto syncConfig = new SysSyncConfigDto();
		syncConfig.setName(getHelper().createName());
		syncConfig.setSystemMapping(mappingDto.getId());
		syncConfig.setCorrelationAttribute(attrMappingDto.getId());		
		syncConfig = (SysSyncConfigDto) syncService.save(syncConfig);
		
		// Tests that attempt to delete a system mapping used in a synchronization setting fails
		resultModels = bulkActionManager.prevalidate(bulkAction);
		assertNotEquals(0, resultModels.getInfos().size());
	}
}
