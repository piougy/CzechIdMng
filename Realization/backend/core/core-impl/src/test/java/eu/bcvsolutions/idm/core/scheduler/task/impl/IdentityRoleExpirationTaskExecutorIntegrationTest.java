package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.time.LocalDate;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Integration test for IdentityRoleExpirationTaskExecutor
 * 
 * @author Filip Mestanek
 * @author Radek Tomiška
 */
public class IdentityRoleExpirationTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private LongRunningTaskManager lrtManager;
	
	/**
	 * This test assigns expired role and checks, whether
	 * the role is removed by the executor.
	 */
	@Test
	public void testExpiredRole() {
		IdentityRoleExpirationTaskExecutor lrt = new IdentityRoleExpirationTaskExecutor();
		lrt.init(null);
		lrtManager.executeSync(lrt);
				
		// Role
		IdmRoleDto role = getHelper().createRole();
		
		// Identity
		IdmIdentityDto identity = getHelper().createIdentity();
		
		// Identity contract
		IdmIdentityContractDto contract = getHelper().createContract(identity);
		
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
		
		lrt = new IdentityRoleExpirationTaskExecutor();
		lrt.init(null);
		lrtManager.executeSync(lrt);
		
		roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(0, roles.size());
	}
	
	@Test
	public void testExpiredBusinessRole() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		//
		// normal and business role
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleRoot = getHelper().createRole();
		IdmRoleDto roleSub = getHelper().createRole();
		getHelper().createRoleComposition(roleRoot, roleSub);
		//
		// assign roles
		IdmIdentityRoleDto assignedRoleOne = getHelper().createIdentityRole(contract, roleRoot);
		IdmIdentityRoleDto assignedRoleTwo = getHelper().createIdentityRole(contract, roleOne);
		//
		// expire contract
		assignedRoleOne.setValidTill(LocalDate.now().minusDays(2));
		assignedRoleOne = identityRoleService.save(assignedRoleOne);
		assignedRoleTwo.setValidTill(LocalDate.now().minusDays(2));
		assignedRoleTwo = identityRoleService.save(assignedRoleTwo);
		//
		// test after create before lrt is executed
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityContractId(contract.getId());
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.find(filter, null).getContent();
		//
		Assert.assertEquals(3, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(roleOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(roleRoot.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(roleSub.getId())));
		Assert.assertTrue(assignedRoles.stream().allMatch(ir -> !ir.isValid()));
		//
		IdentityRoleExpirationTaskExecutor lrt = new IdentityRoleExpirationTaskExecutor();
		lrt.init(null);
		lrtManager.executeSync(lrt);
		//
		assignedRoles = identityRoleService.find(filter, null).getContent();
		//
		Assert.assertTrue(assignedRoles.isEmpty());
	}
	
	@Test
	public void testExpiredRoleAsync() {
		try {
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);	
			IdmRoleDto role = getHelper().createRole();
			IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
			IdmIdentityContractDto contract = getHelper().createContract(identity);
			
			// Role on contract
			IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
			identityRole.setIdentityContract(contract.getId());
			identityRole.setRole(role.getId());
			identityRole.setValidTill(LocalDate.now().minusDays(1));
			identityRole = identityRoleService.save(identityRole);
			
			// Quick check
			List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
			Assert.assertEquals(1, assignedRoles.size());
			Assert.assertTrue(LocalDate.now().isAfter(assignedRoles.get(0).getValidTill()));
			
			IdentityRoleExpirationTaskExecutor lrt = new IdentityRoleExpirationTaskExecutor();
			lrt.init(null);
			lrtManager.execute(lrt);
			
			getHelper().waitForResult(res -> {
				return !identityRoleService.findAllByIdentity(identity.getId()).isEmpty();
			}, 1000, 30);
			
			assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
			Assert.assertTrue(assignedRoles.isEmpty());
		} finally {
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, false);
		}
	}
}
