package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.HashMap;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Integration test for IdentityRoleExpirationTaskExecutor
 * 
 * @author Filip Mestanek
 */
public class IdentityRoleExpirationTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper testHelper;
	@Autowired private IdentityRoleExpirationTaskExecutor expirationExecutor;
	@Autowired private IdmIdentityRoleService identityRoleService;
	
	private IdmIdentityDto identity;
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	/**
	 * This test assigns expired role and checks, whether
	 * the role is removed by the executor.
	 */
	@Test
	public void testExpiredRole() {
		
		// If there were already some expired roles, remove them
		expirationExecutor.init(new HashMap<>());
		expirationExecutor.process();
				
		prepareData();
		
		expirationExecutor.init(new HashMap<>());
		expirationExecutor.process();
		
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(0, roles.size());
	}
	
	private void prepareData() {
		// Role
		IdmRoleDto role = testHelper.createRole();
		
		// Identity
		identity = testHelper.createIdentity();
		
		// Identity contract
		IdmIdentityContractDto contract = testHelper.createIdentityContact(identity);
		
		// Role on contract
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(contract.getId());
		identityRole.setRole(role.getId());
		identityRole.setValidTill(LocalDate.now().minusDays(1));
		identityRole = identityRoleService.save(identityRole);
		
		// Quick check
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, roles.size());
		Assert.assertTrue(LocalDate.now().isAfter(roles.get(0).getValidTill()));
	}
}
