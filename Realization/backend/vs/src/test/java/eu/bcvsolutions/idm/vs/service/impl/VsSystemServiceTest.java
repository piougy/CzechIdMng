package eu.bcvsolutions.idm.vs.service.impl;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.vs.TestHelper;
import eu.bcvsolutions.idm.vs.config.domain.VsConfiguration;
import eu.bcvsolutions.idm.vs.service.api.VsSystemImplementerService;
import eu.bcvsolutions.idm.vs.service.api.dto.VsSystemDto;

/**
 * Virtual system test
 * 
 * @author Svanda
 *
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
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void createVirtualSystemTest() {
		VsSystemDto config = new VsSystemDto();
		config.setName(VS_SYSTEM);
		SysSystemDto system = helper.createVirtualSystem(config);
		Assert.assertNotNull(system);
		Assert.assertEquals(system.getName(), VS_SYSTEM);
		Assert.assertEquals(true, system.isVirtual());
	}

	@Test
	public void checkImplementersTest() {
		String userOneName = "vsUserOne";
		String roleOneName = "vsRoleOne";
		IdmIdentityDto userOne = helper.createIdentity(userOneName);
		IdmRoleDto roleOne = helper.createRole(roleOneName);

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
	 * If none implementers role is set, then we use as implementers all users
	 * with 'superAdminRole'
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
	 * If none implementers role is set, then we use as implementers all users
	 * with 'superAdminRole'
	 */
	public void checkSpecificImplementerRoleTest() {

		String userOneName = "vsUserOne";
		String roleOneName = "vsRoleOne";
		IdmIdentityDto userTwo = helper.createIdentity(userOneName);
		IdmRoleDto roleOne = helper.createRole(roleOneName);
		helper.assignRoles(helper.getPrimeContract(userTwo.getId()), false, roleOne);
		
		this.configurationService.setValue(VsConfiguration.PROPERTY_DEFAULT_ROLE, roleOneName);
		VsSystemDto config = new VsSystemDto();
		config.setName(VS_SYSTEM);
		SysSystemDto system = helper.createVirtualSystem(config);
		Assert.assertNotNull(system);

		List<IdmIdentityDto> implementes = systemImplementersService.findRequestImplementers(system.getId());
		Assert.assertEquals(1, implementes.size());
		Assert.assertEquals(userOneName, implementes.get(0).getUsername());
	}

}
