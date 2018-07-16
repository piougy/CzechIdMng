package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * SysRoleSystemAttributeService tests
 * 
 * @author svandav
 *
 */
public class DefaultSysRoleSystemAttributeServiceTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired
	private SysRoleSystemAttributeService roleSystemAttributeService;

	@Before
	public void login() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testAddRoleMappingAttribute() {
		IdmRoleDto role = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);

		SysRoleSystemAttributeDto attribute = roleSystemAttributeService.addRoleMappingAttribute(system.getId(),
				role.getId(), "FIRSTNAME", null, IcObjectClassInfo.ACCOUNT);
		assertNotNull(attribute);

		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setRoleId(role.getId());
		roleSystemFilter.setSystemId(system.getId());

		List<SysRoleSystemDto> roleSystems = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(1, roleSystems.size());
		SysRoleSystemDto roleSystem = roleSystems.get(0);
		SysSystemMappingDto systemMapping = roleSystemAttributeService.getSystemMapping(system.getId(),
				IcObjectClassInfo.ACCOUNT, SystemOperationType.PROVISIONING);
		assertNotNull(systemMapping);
		assertEquals(systemMapping.getId(), roleSystem.getSystemMapping());
	}

}
