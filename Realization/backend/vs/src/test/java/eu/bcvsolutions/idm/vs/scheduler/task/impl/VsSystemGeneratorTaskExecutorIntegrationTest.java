package eu.bcvsolutions.idm.vs.scheduler.task.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test of the VsSystemGeneratorTaskExecutor 
 * 
 * @author Ondrej Husnik
 *
 */
public class VsSystemGeneratorTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired 
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired 
	private LongRunningTaskManager longRunningTaskManager;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmRoleService roleService;
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void generate10Sys1Role1User() {
		final String TEST_PREFIX = "testPrefix1";
		VsSystemGeneratorTaskExecutor taskExecutor = new VsSystemGeneratorTaskExecutor();
		AutowireHelper.autowire(taskExecutor);
		Map<String, Object> properties = new HashMap<>();
		properties.put(VsSystemGeneratorTaskExecutor.IDNENTITY_COUNT, 1);
		properties.put(VsSystemGeneratorTaskExecutor.ROLE_COUNT, 1);
		properties.put(VsSystemGeneratorTaskExecutor.SYSTEM_COUNT, 10);
		properties.put(VsSystemGeneratorTaskExecutor.ITEM_PREFIX_KEY, TEST_PREFIX);
		taskExecutor.init(properties);
		longRunningTaskManager.execute(taskExecutor);

		// expected number of identities was created
		IdmIdentityFilter identityFilt = new IdmIdentityFilter();
		identityFilt.setText(TEST_PREFIX);
		List<IdmIdentityDto> identities = identityService.find(identityFilt, null).getContent();
		Assert.assertEquals(1, identities.size());
		
		// expected number of roles was created
		IdmRoleFilter roleFilt = new IdmRoleFilter();
		roleFilt.setText(TEST_PREFIX);
		List<IdmRoleDto> roles = roleService.find(roleFilt, null).getContent();
		Set<UUID> roleIds = roles.stream()
				.map(IdmRoleDto::getId)
				.collect(Collectors.toSet());
		Assert.assertEquals(1, roles.size());
		
		// expected number of systems was created
		SysSystemFilter systemFilt = new SysSystemFilter();
		systemFilt.setText(TEST_PREFIX);
		List<SysSystemDto> systems = systemService.find(systemFilt, null).getContent();
		Set<UUID> sysIds = systems.stream()
				.map(SysSystemDto::getId)
				.collect(Collectors.toSet());
		Assert.assertEquals(10, systems.size());
		
		// systems are connected with roles
		SysRoleSystemFilter roleSystemFilt = new SysRoleSystemFilter();
		roleSystemFilt.setRoleId(roles.get(0).getId());
		List<SysRoleSystemDto> roleSystems = roleSystemService.find(roleSystemFilt, null).getContent();
		Set<UUID> sysIdForRoles = roleSystems.stream()
				.map(SysRoleSystemDto::getSystem)
				.collect(Collectors.toSet());
		Assert.assertTrue(Objects.equals(sysIds, sysIdForRoles));
		
		// roles are connected with identities
		Set<UUID> roleIdForIdentity = identityRoleService.findAllByIdentity(identities.get(0).getId()).stream()
				.map(IdmIdentityRoleDto::getRole)
				.collect(Collectors.toSet());
		Assert.assertTrue(Objects.equals(roleIdForIdentity, roleIds));
	}
	
	@Test
	public void generate10Sys10Role1User() {
		final String TEST_PREFIX = "testPrefix2";
		VsSystemGeneratorTaskExecutor taskExecutor = new VsSystemGeneratorTaskExecutor();
		AutowireHelper.autowire(taskExecutor);
		Map<String, Object> properties = new HashMap<>();
		properties.put(VsSystemGeneratorTaskExecutor.IDNENTITY_COUNT, 1);
		properties.put(VsSystemGeneratorTaskExecutor.ROLE_COUNT, 10);
		properties.put(VsSystemGeneratorTaskExecutor.SYSTEM_COUNT, 10);
		properties.put(VsSystemGeneratorTaskExecutor.ITEM_PREFIX_KEY, TEST_PREFIX);
		taskExecutor.init(properties);
		longRunningTaskManager.execute(taskExecutor);

		// expected number of identities was created
		IdmIdentityFilter identityFilt = new IdmIdentityFilter();
		identityFilt.setText(TEST_PREFIX);
		List<IdmIdentityDto> identities = identityService.find(identityFilt, null).getContent();
		Assert.assertEquals(1, identities.size());
		
		// expected number of roles was created
		IdmRoleFilter roleFilt = new IdmRoleFilter();
		roleFilt.setText(TEST_PREFIX);
		List<IdmRoleDto> roles = roleService.find(roleFilt, null).getContent();
		Set<UUID> roleIds = roles.stream()
				.map(IdmRoleDto::getId)
				.collect(Collectors.toSet());
		Assert.assertEquals(10, roles.size());
		
		// expected number of systems was created
		SysSystemFilter systemFilt = new SysSystemFilter();
		systemFilt.setText(TEST_PREFIX);
		List<SysSystemDto> systems = systemService.find(systemFilt, null).getContent();
		Set<UUID> sysIds = systems.stream()
				.map(SysSystemDto::getId)
				.collect(Collectors.toSet());
		Assert.assertEquals(10, systems.size());
		
		// systems are connected with roles
		Set<UUID> sysIdForRoles = new HashSet<UUID>();
		SysRoleSystemFilter roleSystemFilt = new SysRoleSystemFilter();
		for (IdmRoleDto role : roles) {
			roleSystemFilt.setRoleId(role.getId());
			List<SysRoleSystemDto> roleSystems = roleSystemService.find(roleSystemFilt, null).getContent();
			Assert.assertEquals(1, roleSystems.size());
			sysIdForRoles.add(roleSystems.get(0).getSystem());
		}
		Assert.assertTrue(Objects.equals(sysIds, sysIdForRoles));
		
		// roles are connected with identities
		Set<UUID> roleIdForIdentity = identityRoleService.findAllByIdentity(identities.get(0).getId()).stream()
				.map(IdmIdentityRoleDto::getRole)
				.collect(Collectors.toSet());
		Assert.assertTrue(Objects.equals(roleIdForIdentity, roleIds));
	}
}
