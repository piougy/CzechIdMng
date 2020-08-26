package eu.bcvsolutions.idm.vs.service.impl;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.vs.TestHelper;
import eu.bcvsolutions.idm.vs.config.domain.VsConfiguration;
import eu.bcvsolutions.idm.vs.dto.VsSystemDto;
import eu.bcvsolutions.idm.vs.service.api.VsSystemImplementerService;

/**
 * Virtual system test
 *
 * @author Svanda
 */
@Transactional
public class VsSystemServiceTest extends AbstractIntegrationTest {

	private static String VS_SYSTEM = "VS_SYSTEM_ONE";

	@Autowired
	private TestHelper helper;
	@Autowired
	private VsSystemImplementerService systemImplementersService;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired
	private SysSyncConfigService configService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private ConfidentialStorage confidentialStorage;

	@After
	public void logout() {
		super.logout();
		// @Transactional -> db is rolled back, but cache has to be cleared too
		this.configurationService.deleteValue(VsConfiguration.PROPERTY_DEFAULT_ROLE);
	}

	@Test
	public void createVirtualSystemTest() {
		VsSystemDto config = new VsSystemDto();
		config.setName(VS_SYSTEM);
		SysSystemDto system = helper.createVirtualSystem(config);
		Assert.assertNotNull(system);
		Assert.assertEquals(system.getName(), VS_SYSTEM);
		Assert.assertTrue(system.isVirtual());
	}

	@Test
	public void checkImplementersTest() {
		String userOneName = "vsUserOne";
		String roleOneName = "vsRoleOne";
		IdmIdentityDto userOne = getHelper().createIdentity(userOneName);
		IdmRoleDto roleOne = getHelper().createRole(roleOneName);

		VsSystemDto config = new VsSystemDto();
		config.setName(VS_SYSTEM);
		config.setImplementers(ImmutableList.of(userOne.getId()));
		config.setImplementerRoles(ImmutableList.of(roleOne.getId()));
		SysSystemDto system = helper.createVirtualSystem(config);
		Assert.assertNotNull(system);

		List<IdmIdentityDto> implementes = systemImplementersService.findRequestImplementers(system.getId());
		Assert.assertEquals(1, implementes.size());
		Assert.assertEquals(userOneName, implementes.get(0).getUsername());
	}

	@Test
	/**
	 * If none implementers role is set, then we use as implementers all users with
	 * 'superAdminRole'
	 */
	public void checkDefaultImplementersTest() {
		VsSystemDto config = new VsSystemDto();
		config.setName(VS_SYSTEM);
		SysSystemDto system = helper.createVirtualSystem(config);
		Assert.assertNotNull(system);

		List<IdmIdentityDto> implementes = systemImplementersService.findRequestImplementers(system.getId());
		Assert.assertEquals(1, implementes.size());
		Assert.assertEquals("admin", implementes.get(0).getUsername());
	}

	@Test
	/**
	 * If none implementers role is set, then we use as implementers all users with
	 * 'superAdminRole'
	 */
	public void checkSpecificImplementerRoleTest() {

		String userOneName = "vsUserOne";
		String roleOneName = "vsRoleOne";
		IdmIdentityDto userTwo = getHelper().createIdentity(userOneName);
		IdmRoleDto roleOne = getHelper().createRole(roleOneName);
		getHelper().assignRoles(getHelper().getPrimeContract(userTwo.getId()), false, roleOne);

		this.configurationService.setValue(VsConfiguration.PROPERTY_DEFAULT_ROLE, roleOneName);
		VsSystemDto config = new VsSystemDto();
		config.setName(VS_SYSTEM);
		SysSystemDto system = helper.createVirtualSystem(config);
		Assert.assertNotNull(system);

		List<IdmIdentityDto> implementes = systemImplementersService.findRequestImplementers(system.getId());
		Assert.assertEquals(1, implementes.size());
		Assert.assertEquals(userOneName, implementes.get(0).getUsername());
	}

	@Test
	public void checkCreatedMappingAndSync() {
		VsSystemDto config = new VsSystemDto();
		config.setName(VS_SYSTEM);
		SysSystemDto system = helper.createVirtualSystem(config);
		//
		Assert.assertNotNull(system);
		//
		Assert.assertEquals(system.getName(), VS_SYSTEM);
		Assert.assertTrue(system.isVirtual());
		//
		List<SysSystemMappingDto> mappings = systemMappingService.findBySystemId(system.getId(),
				SystemOperationType.SYNCHRONIZATION, SystemEntityType.IDENTITY);
		Assert.assertEquals("Wrong size of found mappings!", 1, mappings.size());
		SysSystemMappingDto mapping = mappings.get(0);
		Assert.assertNotNull("Mapping is null!", mapping);
		//
		List<SysSystemAttributeMappingDto> attributeMappings = systemAttributeMappingService
				.findBySystemMapping(mapping);
		Assert.assertEquals("Wrong size of found attributes for mapping!", 1, attributeMappings.size());
		SysSystemAttributeMappingDto attributeMapping = attributeMappings.get(0);
		Assert.assertNotNull("Mapping is null!", attributeMapping);
		Assert.assertEquals("Wrong IdmPropertyName - username!", "username", attributeMapping.getIdmPropertyName());
		Assert.assertEquals("Wrong Name of attribute mapping - username!", "username", attributeMapping.getName());
		//
		SysSyncConfigFilter filter = new SysSyncConfigFilter();
		filter.setSystemId(system.getId());
		filter.setName("Link virtual accounts to identities");
		List<AbstractSysSyncConfigDto> syncConfigs = configService.find(filter, null).getContent();
		Assert.assertEquals("Wrong size of synchronizations!", 1, syncConfigs.size());
		Assert.assertNotNull("Sync config is null!", syncConfigs.get(0));
	}
	
	@Test
	/**
	 * Test proves bug fix of password overriding trouble of the remote ConnectorServer.
	 * This occurred when switching from VS system to remote ConnectorServer.   
	 */
	public void testPasswordNotOverriden() {
		final String testPassword = "myPassword123456";
		VsSystemDto config = new VsSystemDto();
		config.setName(helper.createName());
		SysSystemDto system = helper.createVirtualSystem(config);
		system.setRemote(true);
		system.setVirtual(false);
		system.getConnectorServer().setPassword(new GuardedString(testPassword));
		system = systemService.save(system);
				
		String storedPassword = confidentialStorage.getGuardedString(system.getId(),
				SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD).asString();
		
		Assert.assertEquals(testPassword, storedPassword);
	}

	@Test
	public void testCreateDefaultRole() {
		VsSystemDto config = new VsSystemDto();
		config.setName(getHelper().createName());
		config.setCreateDefaultRole(true);
		config.setRoleName(getHelper().createName());

		SysSystemDto system = helper.createVirtualSystem(config);
		IdmRoleFilter roleFilter = new IdmRoleFilter();
		roleFilter.setCodeableIdentifier(config.getRoleName());
		List<IdmRoleDto> roles = roleService.find(roleFilter, null).getContent();

		Assert.assertEquals(1, roles.size());
		IdmRoleDto role = roles.get(0);
		Assert.assertEquals(config.getRoleName(), role.getCode());

		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setRoleId(role.getId());
		roleSystemFilter.setSystemId(system.getId());
		List<SysRoleSystemDto> roleSystems = roleSystemService.find(roleSystemFilter, null).getContent();
		Assert.assertEquals(1, roleSystems.size());

	}

	@Test
	public void testNoCreateDefaultRole() {
		VsSystemDto config = new VsSystemDto();
		config.setName(getHelper().createName());
		config.setCreateDefaultRole(false);
		config.setRoleName(getHelper().createName());

		helper.createVirtualSystem(config);
		IdmRoleFilter roleFilter = new IdmRoleFilter();
		roleFilter.setCodeableIdentifier(config.getRoleName());
		List<IdmRoleDto> roles = roleService.find(roleFilter, null).getContent();

		Assert.assertEquals(0, roles.size());

	}

}
