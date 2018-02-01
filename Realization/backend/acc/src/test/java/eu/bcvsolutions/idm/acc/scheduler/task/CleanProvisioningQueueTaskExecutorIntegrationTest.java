package eu.bcvsolutions.idm.acc.scheduler.task;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.CancelProvisioningQueueTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for LRT CleanProvisioningQueueTaskExecutor
 * 
 * @author Patrik Stloukal
 *
 */
public class CleanProvisioningQueueTaskExecutorIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private IdmIdentityService idmIdentityService;
	@Autowired
	private SysProvisioningOperationService sysProvisioningOperationService;
	@Autowired
	private SysProvisioningBatchService sysProvisioningBatchService;
	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private LongRunningTaskManager longRunningTaskManager;
	@Autowired
	private IdmIdentityRoleService identityRoleService;

	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testLRT() {
		// create identity
		IdmIdentityDto person = createIdentity("firstName" + System.currentTimeMillis(),
				"Surname" + System.currentTimeMillis(), "email" + System.currentTimeMillis() + "@gemail.eu",
				"000000009", false);

		// create system read only
		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setReadonly(true);
		systemService.save(system);

		// create role, "assign" role to system, "assign" role to identity
		IdmRoleDto role = helper.createRole();
		SysRoleSystemDto roleSystemDefault = helper.createRoleSystem(role, system);
		roleSystemDefault.setSystemMapping(helper.getDefaultMapping(system).getId());
		roleSystemService.save(roleSystemDefault);
		helper.createIdentityRole(person, role);

		// find items in provisioning queue
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		Page<SysProvisioningOperationDto> page = sysProvisioningOperationService.find(filter, null);
		Assert.assertEquals(1, page.getContent().size());

		UUID batchId = page.getContent().get(0).getBatch();
		SysProvisioningBatchDto batch = sysProvisioningBatchService.get(batchId);
		assertNotNull(batch);
		// create and start LRT to clean
		CancelProvisioningQueueTaskExecutor lrt = new CancelProvisioningQueueTaskExecutor();
		//
		longRunningTaskManager.executeSync(lrt);

		// items in queue are cleaned
		filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		page = sysProvisioningOperationService.find(filter, null);
		Assert.assertEquals(0, page.getContent().size());

		batch = sysProvisioningBatchService.get(batchId);
		assertNull(batch);
	}

	@Test
	public void testLrtWithoutFilter() {
		// create identity
		IdmIdentityDto person = createIdentity("firstName" + System.currentTimeMillis(),
				"Surname" + System.currentTimeMillis(), "email" + System.currentTimeMillis() + "@gemail.eu",
				"000000009", false);
		IdmIdentityDto personSecond = createIdentity("firstName" + System.currentTimeMillis(),
				"Surname" + System.currentTimeMillis(), "email" + System.currentTimeMillis() + "@gemail.eu",
				"000000009", false);

		// create system read only
		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setReadonly(true);
		systemService.save(system);

		// create role, "assign" role to system, "assign" role to identity
		IdmRoleDto role = helper.createRole();
		SysRoleSystemDto roleSystemDefault = helper.createRoleSystem(role, system);
		roleSystemDefault.setSystemMapping(helper.getDefaultMapping(system).getId());
		roleSystemService.save(roleSystemDefault);
		helper.createIdentityRole(person, role);

		// create system read only
		SysSystemDto systemSecond = helper.createTestResourceSystem(true);
		systemSecond.setReadonly(true);
		systemService.save(systemSecond);

		// create role, "assign" role to system, "assign" role to identity
		IdmRoleDto roleSecond = helper.createRole();
		SysRoleSystemDto roleSystemDefaultSecond = helper.createRoleSystem(roleSecond, systemSecond);
		roleSystemDefaultSecond.setSystemMapping(helper.getDefaultMapping(systemSecond).getId());
		roleSystemService.save(roleSystemDefaultSecond);
		helper.createIdentityRole(personSecond, roleSecond);

		// find items in provisioning queue
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		Page<SysProvisioningOperationDto> page = sysProvisioningOperationService.find(filter, null);
		Assert.assertEquals(1, page.getContent().size());

		// find items in provisioning queue// both systems
		SysProvisioningOperationFilter filterSecond = new SysProvisioningOperationFilter();
		filterSecond.setSystemId(systemSecond.getId());
		Page<SysProvisioningOperationDto> pageSecond = sysProvisioningOperationService.find(filterSecond, null);
		Assert.assertEquals(1, pageSecond.getContent().size());

		UUID batchId = page.getContent().get(0).getBatch();
		SysProvisioningBatchDto batch = sysProvisioningBatchService.get(batchId);
		assertNotNull(batch);
		// create and start LRT to clean
		CancelProvisioningQueueTaskExecutor lrt = new CancelProvisioningQueueTaskExecutor();
		//
		longRunningTaskManager.executeSync(lrt);

		// items in queue are cleaned
		filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		page = sysProvisioningOperationService.find(filter, null);
		Assert.assertEquals(0, page.getContent().size());

		batch = sysProvisioningBatchService.get(batchId);
		assertNull(batch);
	}

	@Test
	public void testLrtWithFilter() {
		// create identity
		IdmIdentityDto person = createIdentity("firstName" + System.currentTimeMillis(),
				"Surname" + System.currentTimeMillis(), "email" + System.currentTimeMillis() + "@gemail.eu",
				"000000009", false);
		IdmIdentityDto personSecond = createIdentity("firstName" + System.currentTimeMillis(),
				"Surname" + System.currentTimeMillis(), "email" + System.currentTimeMillis() + "@gemail.eu",
				"000000009", false);

		// create system read only
		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setReadonly(true);
		systemService.save(system);

		// create role, "assign" role to system, "assign" role to identity
		IdmRoleDto role = helper.createRole();
		SysRoleSystemDto roleSystemDefault = helper.createRoleSystem(role, system);
		roleSystemDefault.setSystemMapping(helper.getDefaultMapping(system).getId());
		roleSystemService.save(roleSystemDefault);
		IdmIdentityRoleDto identityRole = helper.createIdentityRole(person, role);
		identityRole.setValidFrom(LocalDate.now().plusDays(1));
		identityRoleService.save(identityRole);

		// create system read only
		SysSystemDto systemSecond = helper.createTestResourceSystem(true);
		systemSecond.setReadonly(true);
		systemService.save(systemSecond);

		// create role, "assign" role to system, "assign" role to identity
		IdmRoleDto roleSecond = helper.createRole();
		SysRoleSystemDto roleSystemDefaultSecond = helper.createRoleSystem(roleSecond, systemSecond);
		roleSystemDefaultSecond.setSystemMapping(helper.getDefaultMapping(systemSecond).getId());
		roleSystemService.save(roleSystemDefaultSecond);
		helper.createIdentityRole(personSecond, roleSecond);

		// find items in provisioning queue
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		Page<SysProvisioningOperationDto> page = sysProvisioningOperationService.find(filter, null);
		Assert.assertEquals(2, page.getContent().size());

		// find items in provisioning queue// both systems
		SysProvisioningOperationFilter filterSecond = new SysProvisioningOperationFilter();
		filterSecond.setSystemId(systemSecond.getId());
		Page<SysProvisioningOperationDto> pageSecond = sysProvisioningOperationService.find(filterSecond, null);
		Assert.assertEquals(1, pageSecond.getContent().size());

		UUID batchId = page.getContent().get(0).getBatch();
		SysProvisioningBatchDto batch = sysProvisioningBatchService.get(batchId);
		assertNotNull(batch);
		// create and start LRT to clean
		CancelProvisioningQueueTaskExecutor lrt = new CancelProvisioningQueueTaskExecutor();
		//
		SysProvisioningOperationFilter filterLrt = new SysProvisioningOperationFilter();
		filterLrt.setSystemId(system.getId());
		filterLrt.setOperationType(ProvisioningEventType.CREATE);

		page = sysProvisioningOperationService.find(filterLrt, null);
		Assert.assertEquals(1, page.getContent().size());

		lrt.setFilter(filterLrt);
		//
		longRunningTaskManager.executeSync(lrt);

		// items in queue are cleaned
		filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		page = sysProvisioningOperationService.find(filter, null);
		Assert.assertEquals(0, page.getContent().size());

		batch = sysProvisioningBatchService.get(batchId);
		assertNull(batch);

		// find items in provisioning queue// both systems
		pageSecond = sysProvisioningOperationService.find(filterSecond, null);
		Assert.assertEquals(1, pageSecond.getContent().size());
	}

	private IdmIdentityDto createIdentity(String firstName, String lastName, String email, String phone,
			boolean disabled) {
		IdmIdentityDto identity2 = helper.createIdentity();
		identity2.setFirstName(firstName);
		identity2.setLastName(lastName);
		identity2.setEmail(email);
		identity2.setPhone(phone);
		return idmIdentityService.save(identity2);
	}

}
