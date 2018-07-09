package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for System filter
 * 
 * @author Patrik Stloukal
 *
 */
public class DefaultSysSystemServiceFilterTest extends AbstractIntegrationTest {

	@Autowired
	private SysSystemService systemService;
	@Autowired
	private IdmPasswordPolicyService policyService;

	@Before
	public void login() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testSystemNameFilter() {
		SysSystemDto system1 = createSystem("test001" + System.currentTimeMillis(), null);
		createSystem("test002" + System.currentTimeMillis(), null);
		//
		SysSystemFilter testFilter = new SysSystemFilter();
		testFilter.setText(system1.getName());
		Page<SysSystemDto> pages = systemService.find(testFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(system1.getId(), pages.getContent().get(0).getId());
	}

	@Test
	public void testSystemVirtualFilter() {
		SysSystemDto system1 = createSystem("test001" + System.currentTimeMillis(), true);
		createSystem("test002" + System.currentTimeMillis(), false);
		//
		SysSystemFilter testFilter = new SysSystemFilter();
		testFilter.setVirtual(true);
		Page<SysSystemDto> pages = systemService.find(testFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(system1.getId(), pages.getContent().get(0).getId());
	}

	/**
	 * Id in {@link SysSystemFilter} not currently implemented. Use get by id.
	 */
	@Test
	public void testSystemIdFilter() {
		SysSystemDto system1 = createSystem("test001" + System.currentTimeMillis(), null);
		createSystem("test002" + System.currentTimeMillis(), null);
		//
		SysSystemDto foundedSystem = systemService.get(system1.getId());
		assertEquals(system1.getId(), foundedSystem.getId());
	}

	@Test
	public void testSystemPasswordPolicyValidationId() {
		SysSystemDto system1 = createSystem("test001" + System.currentTimeMillis(), null);
		createSystem("test002" + System.currentTimeMillis(), null);
		SysSystemFilter testFilter = new SysSystemFilter();
		IdmPasswordPolicyDto policy = createPasswordPolicy("first", IdmPasswordPolicyType.VALIDATE);
		system1.setPasswordPolicyValidate(policy.getId());
		system1 = systemService.save(system1);
		testFilter.setPasswordPolicyValidationId(policy.getId());
		//
		Page<SysSystemDto> pages = systemService.find(testFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(system1.getId(), pages.getContent().get(0).getId());
	}

	@Test
	public void testSystemPasswordPolicyGenerationId() {
		SysSystemDto system1 = createSystem("test01" + System.currentTimeMillis(), null);
		createSystem("test02" + System.currentTimeMillis(), null);
		SysSystemFilter testFilter = new SysSystemFilter();
		IdmPasswordPolicyDto policy = createPasswordPolicy("second", IdmPasswordPolicyType.GENERATE);
		system1.setPasswordPolicyGenerate(policy.getId());
		system1 = systemService.save(system1);
		testFilter.setPasswordPolicyGenerationId(policy.getId());
		//
		Page<SysSystemDto> pages = systemService.find(testFilter, null);
		assertEquals(1, pages.getTotalElements());
		assertEquals(system1.getId(), pages.getContent().get(0).getId());
	}

	/**
	 * Create {@link SysSystemDto} 
	 * 
	 * @param systemName
	 * @param virtual
	 * @return
	 */
	private SysSystemDto createSystem(String systemName, Boolean virtual) {
		SysSystemDto system = new SysSystemDto();
		system.setName(systemName);
		if (virtual != null) {
			system.setVirtual(virtual);
		}
		return systemService.save(system);
	}

	/**
	 * Create {@link IdmPasswordPolicyDto}
	 * 
	 * @param policyName
	 * @param type
	 * @return
	 */
	private IdmPasswordPolicyDto createPasswordPolicy(String policyName, IdmPasswordPolicyType type) {
		IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
		policy.setName(policyName);
		policy.setType(type);
		return policyService.save(policy);
	}
}
