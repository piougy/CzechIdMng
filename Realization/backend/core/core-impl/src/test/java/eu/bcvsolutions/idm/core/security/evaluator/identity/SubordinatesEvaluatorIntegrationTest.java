package eu.bcvsolutions.idm.core.security.evaluator.identity;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractEvaluatorIntegrationTest;

/**
 * Authorization policy evaluator test.
 * 
 * @author Radek Tomiška
 */
@Transactional
public class SubordinatesEvaluatorIntegrationTest extends AbstractEvaluatorIntegrationTest {

	@Autowired private IdmIdentityService identityService;
	
	@Test
	public void testSubordinatesRead() {
		// create subordinates and managers - 
		IdmIdentityDto subordinateOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto subordinateTwo = getHelper().createIdentity((GuardedString) null); // other
		IdmIdentityDto managerOne = getHelper().createIdentity();
		IdmIdentityDto managerTwo = getHelper().createIdentity();
		getHelper().createContractGuarantee(getHelper().getPrimeContract(subordinateOne), managerOne);
		getHelper().createContractGuarantee(getHelper().getPrimeContract(subordinateTwo), managerTwo);
		//
		List<IdmIdentityDto> subordinates = null;
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
			//
			Set<String> permissions = identityService.getPermissions(subordinateOne);
			Assert.assertTrue(permissions.isEmpty());
		} finally {
			logout();
		}
		//
		// without login
		subordinates = identityService.find(null, IdmBasePermission.READ).getContent();
		Assert.assertTrue(subordinates.isEmpty());
		//
		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				roleOne.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				SubordinatesEvaluator.class,
				IdmBasePermission.READ);
		//
		try {
			getHelper().login(managerOne.getUsername(), managerOne.getPassword());
			//
			// without update permission
			subordinates = identityService.find(null, IdmBasePermission.UPDATE).getContent();
			Assert.assertTrue(subordinates.isEmpty());
			//
			// evaluate	access
			subordinates = identityService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, subordinates.size());	
			Assert.assertEquals(subordinateOne.getId(), subordinates.get(0).getId());
			//
			Set<String> permissions = identityService.getPermissions(subordinateOne);
			Assert.assertEquals(1, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
		} finally {
			logout();
		}
		//
		getHelper().createAuthorizationPolicy(
				roleOne.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				SubordinatesEvaluator.class,
				IdmBasePermission.UPDATE);
		//
		try {
			getHelper().login(managerOne.getUsername(), managerOne.getPassword());
			//
			Set<String> permissions = identityService.getPermissions(subordinateOne);
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.UPDATE.name())));
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
			//
			Set<String> permissions = identityService.getPermissions(subordinateOne);
			Assert.assertTrue(permissions.isEmpty());
		} finally {
			logout();
		}
	}
}
