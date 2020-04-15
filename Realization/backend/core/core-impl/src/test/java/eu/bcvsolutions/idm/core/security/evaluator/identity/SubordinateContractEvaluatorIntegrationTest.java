package eu.bcvsolutions.idm.core.security.evaluator.identity;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Authorization policy evaluator test.
 * 
 * @author Radek Tomiška
 */
@Transactional
public class SubordinateContractEvaluatorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityContractService contractService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	
	@Test
	public void testSubordinatesRead() {
		// create subordinates and managers - 
		IdmIdentityDto subordinateOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto subordinateTwo = getHelper().createIdentity((GuardedString) null); // other
		IdmIdentityDto managerOne = getHelper().createIdentity();
		IdmIdentityDto managerTwo = getHelper().createIdentity();
		IdmIdentityContractDto primeContact = getHelper().getPrimeContract(subordinateOne);
		IdmIdentityContractDto otherContact = getHelper().createIdentityContact(subordinateOne);// other contract
		getHelper().createContractGuarantee(primeContact, managerOne);
		getHelper().createContractGuarantee(getHelper().getPrimeContract(subordinateTwo), managerTwo);
		IdmIdentityRoleDto assignedRolePrime = getHelper().createIdentityRole(primeContact, getHelper().createRole());
		getHelper().createIdentityRole(otherContact, getHelper().createRole()); // other
		//
		List<IdmIdentityDto> subordinates = null;
		List<IdmIdentityContractDto> subordinateContracts = null;
		List<IdmIdentityRoleDto> subordinateAssignedRoles = null;
		IdmRoleDto roleOne = getHelper().createRole();
		//
		getHelper().createIdentityRole(managerOne, roleOne);
		getHelper().createIdentityRole(managerTwo, roleOne);
		//
		// check - read without policy
		try {			
			getHelper().login(managerOne.getUsername(), managerOne.getPassword());
			//
			subordinates = identityService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(subordinates.isEmpty());	
			subordinateContracts = contractService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(subordinateContracts.isEmpty());	
			subordinateAssignedRoles = identityRoleService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(subordinateAssignedRoles.isEmpty());
			//
			Set<String> permissions = identityService.getPermissions(subordinateOne);
			Assert.assertTrue(permissions.isEmpty());
			permissions = contractService.getPermissions(otherContact);
			Assert.assertTrue(permissions.isEmpty());
			permissions = contractService.getPermissions(primeContact);
			Assert.assertTrue(permissions.isEmpty());
		} finally {
			logout();
		}
		//
		// without login
		subordinateContracts = contractService.find(null, IdmBasePermission.READ).getContent();
		Assert.assertTrue(subordinateContracts.isEmpty());
		//
		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				roleOne.getId(),
				CoreGroupPermission.IDENTITYCONTRACT,
				IdmIdentityContract.class,
				SubordinateContractEvaluator.class,
				IdmBasePermission.READ);
		// identity transitively
		getHelper().createAuthorizationPolicy(
				roleOne.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				IdentityByContractEvaluator.class);
		// role transitively
		getHelper().createAuthorizationPolicy(
				roleOne.getId(),
				CoreGroupPermission.IDENTITYROLE,
				IdmIdentityRole.class,
				IdentityRoleByContractEvaluator.class);
		//
		try {
			getHelper().login(managerOne.getUsername(), managerOne.getPassword());
			//
			// without update permission
			subordinates = identityService.find(null, IdmBasePermission.UPDATE).getContent();
			Assert.assertTrue(subordinates.isEmpty());
			subordinateContracts = contractService.find(null, IdmBasePermission.UPDATE).getContent();
			Assert.assertTrue(subordinateContracts.isEmpty());	
			subordinateAssignedRoles = identityRoleService.find(null, IdmBasePermission.UPDATE).getContent();
			Assert.assertTrue(subordinateAssignedRoles.isEmpty());
			//
			// evaluate	access
			subordinates = identityService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, subordinates.size());	
			Assert.assertEquals(subordinateOne.getId(), subordinates.get(0).getId());
			subordinateContracts = contractService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, subordinateContracts.size());	
			Assert.assertEquals(primeContact.getId(), subordinateContracts.get(0).getId());
			subordinateAssignedRoles = identityRoleService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, subordinateAssignedRoles.size());	
			Assert.assertEquals(assignedRolePrime.getId(), subordinateAssignedRoles.get(0).getId());
			//
			Set<String> permissions = identityService.getPermissions(subordinateOne);
			Assert.assertEquals(1, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
			permissions = contractService.getPermissions(primeContact);
			Assert.assertEquals(1, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
			permissions = identityRoleService.getPermissions(assignedRolePrime);
			Assert.assertEquals(1, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
		} finally {
			logout();
		}
		//
		getHelper().createAuthorizationPolicy(
				roleOne.getId(),
				CoreGroupPermission.IDENTITYCONTRACT,
				IdmIdentityContract.class,
				SubordinateContractEvaluator.class,
				IdmBasePermission.UPDATE);
		//
		try {
			getHelper().login(managerOne.getUsername(), managerOne.getPassword());
			//
			Set<String> permissions = identityService.getPermissions(subordinateOne);
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.UPDATE.name())));
			permissions = contractService.getPermissions(primeContact);
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.UPDATE.name())));
			permissions = identityRoleService.getPermissions(assignedRolePrime);
			Assert.assertEquals(4, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.UPDATE.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.CREATE.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.DELETE.name())));
		} finally {
			logout();
		}
		//
		// check - by other manager
		try {			
			getHelper().login(managerTwo.getUsername(), managerTwo.getPassword());
			//
			subordinates = identityService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, subordinates.size());	
			Assert.assertEquals(subordinateTwo.getId(), subordinates.get(0).getId());
			subordinateContracts = contractService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, subordinateContracts.size());	
			Assert.assertNotEquals(primeContact.getId(), subordinateContracts.get(0).getId());
			Assert.assertNotEquals(otherContact.getId(), subordinateContracts.get(0).getId());
			subordinateAssignedRoles = identityRoleService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(subordinateAssignedRoles.isEmpty());
			//
			Set<String> permissions = contractService.getPermissions(primeContact);
			Assert.assertTrue(permissions.isEmpty());
			permissions = identityRoleService.getPermissions(assignedRolePrime);
			Assert.assertTrue(permissions.isEmpty());
			permissions = identityService.getPermissions(subordinateOne);
			Assert.assertTrue(permissions.isEmpty());
		} finally {
			logout();
		}
	}
}
