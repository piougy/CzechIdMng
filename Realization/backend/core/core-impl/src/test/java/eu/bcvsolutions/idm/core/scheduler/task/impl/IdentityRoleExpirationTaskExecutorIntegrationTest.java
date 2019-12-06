package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.HashMap;
import java.util.List;

import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Integration test for IdentityRoleExpirationTaskExecutor
 * 
 * @author Filip Mestanek
 * @author Radek Tomiška
 */
public class IdentityRoleExpirationTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdentityRoleExpirationTaskExecutor expirationExecutor;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private LongRunningTaskManager lrtManager;
	
	/**
	 * This test assigns expired role and checks, whether
	 * the role is removed by the executor.
	 */
	@Test
	public void testExpiredRole() {
		
		// If there were already some expired roles, remove them
		expirationExecutor.init(new HashMap<>());
		expirationExecutor.process();
				
		// Role
		IdmRoleDto role = getHelper().createRole();
		
		// Identity
		IdmIdentityDto identity = getHelper().createIdentity();
		
		// Identity contract
		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity);
		
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
		
		expirationExecutor.init(new HashMap<>());
		expirationExecutor.process();
		
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
}
