package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.test.TestHelper;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for Role filter
 * 
 * @author Patrik Stloukal
 *
 */
public class DefaultSysRoleSystemServiceFilterTest extends AbstractIntegrationTest {

	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired
	private SysSystemMappingService mappingService;
	@Autowired
	private TestHelper helper;

	@Before
	public void login() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testRoleIdFilter() {
		IdmRoleDto role = helper.createRole();
		IdmRoleDto role2 = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);
		//
		SysSystemMappingDto sysSystemMappingDto = mappingService
				.findBySystemId(system.getId(), SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY).get(0);
		SysRoleSystemDto roleSystem = createRoleSystem(role.getId(), system.getId(), sysSystemMappingDto.getId());
		createRoleSystem(role2.getId(), system.getId(), sysSystemMappingDto.getId());
		//
		SysRoleSystemFilter roleFilter = new SysRoleSystemFilter();
		roleFilter.setRoleId(role.getId());
		Page<SysRoleSystemDto> pages = roleSystemService.find(roleFilter, null);
		//
		assertEquals(1, pages.getTotalElements());
		assertEquals(roleSystem.getId(), pages.getContent().get(0).getId());
	}

	@Test
	public void testSystemIdFilter() {
		IdmRoleDto role1 = helper.createRole();
		IdmRoleDto role2 = helper.createRole();
		SysSystemDto system1 = helper.createTestResourceSystem(true);
		SysSystemDto system2 = helper.createTestResourceSystem(true);
		//
		SysSystemMappingDto sysSystemMappingDto = mappingService
				.findBySystemId(system1.getId(), SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY).get(0);
		createRoleSystem(role1.getId(), system1.getId(), sysSystemMappingDto.getId());
		SysRoleSystemDto roleSystem2 = createRoleSystem(role2.getId(), system2.getId(), sysSystemMappingDto.getId());
		//
		SysRoleSystemFilter roleFilter = new SysRoleSystemFilter();
		roleFilter.setSystemId(system2.getId());
		Page<SysRoleSystemDto> pages = roleSystemService.find(roleFilter, null);
		//
		assertEquals(1, pages.getTotalElements());
		assertEquals(roleSystem2.getId(), pages.getContent().get(0).getId());
	}

	@Test
	public void testSystemMappingIdFilter() {
		IdmRoleDto role1 = helper.createRole();
		IdmRoleDto role2 = helper.createRole();
		SysSystemDto system1 = helper.createTestResourceSystem(true);
		SysSystemDto system2 = helper.createTestResourceSystem(true);
		//
		SysSystemMappingDto sysSystemMappingDto1 = mappingService
				.findBySystemId(system1.getId(), SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY).get(0);
		SysSystemMappingDto sysSystemMappingDto2 = mappingService
				.findBySystemId(system2.getId(), SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY).get(0);
		SysRoleSystemDto roleSystem1 = createRoleSystem(role1.getId(), system1.getId(), sysSystemMappingDto1.getId());
		createRoleSystem(role2.getId(), system1.getId(), sysSystemMappingDto2.getId());
		//
		SysRoleSystemFilter roleFilter = new SysRoleSystemFilter();
		roleFilter.setSystemMappingId(sysSystemMappingDto1.getId());
		Page<SysRoleSystemDto> pages = roleSystemService.find(roleFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(roleSystem1.getId(), pages.getContent().get(0).getId());
	}

	/**
	 * Create {@link SysRoleSystemDto}
	 * 
	 * @param roleId
	 * @param systemId
	 * @param systemMappingId
	 * @return
	 */
	private SysRoleSystemDto createRoleSystem(UUID roleId, UUID systemId, UUID systemMappingId) {
		helper.createRole();
		SysRoleSystemDto role = new SysRoleSystemDto();
		role.setRole(roleId);
		role.setSystem(systemId);
		role.setSystemMapping(systemMappingId);
		return roleSystemService.save(role);
	}
}
