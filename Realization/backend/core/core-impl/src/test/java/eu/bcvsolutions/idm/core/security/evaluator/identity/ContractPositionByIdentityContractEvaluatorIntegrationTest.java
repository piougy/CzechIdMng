package eu.bcvsolutions.idm.core.security.evaluator.identity;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractEvaluatorIntegrationTest;

/**
 * Permission to contract position by contract
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class ContractPositionByIdentityContractEvaluatorIntegrationTest extends AbstractEvaluatorIntegrationTest {

	@Autowired private IdmContractPositionService service;
	@Autowired private IdmIdentityContractService identityContractService;
	
	@Test
	public void testCrud() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity);
		IdmIdentityContractDto otherContract = getHelper().createContract(identity);
		List<IdmContractPositionDto> positions = null;
		IdmRoleDto role = getHelper().createRole();
		
		IdmContractPositionDto positionOne = getHelper().createContractPosition(primeContract.getId());
		getHelper().createContractPosition(otherContract.getId()); // other
		
		getHelper().createIdentityRole(identity, role);
		getHelper().createUuidPolicy(role.getId(), primeContract.getId(), IdmBasePermission.READ);
		//
		// check - read without transient policy
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			Assert.assertEquals(primeContract.getId(), identityContractService.get(primeContract.getId(), IdmBasePermission.READ).getId());
			positions = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(positions.isEmpty());	
		} finally {
			logout();
		}
		//
		// without login
		positions = service.find(null, IdmBasePermission.READ).getContent();
		Assert.assertTrue(positions.isEmpty());
		//
		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.CONTRACTPOSITION,
				IdmContractPosition.class,
				ContractPositionByIdentityContractEvaluator.class);
		//
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			// evaluate	access
			getHelper().login(identity.getUsername(), identity.getPassword());
			positions = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, positions.size());	
			Assert.assertEquals(positionOne.getId(), positions.get(0).getId());
			//
			Set<String> permissions = service.getPermissions(positionOne);
			Assert.assertEquals(1, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
		} finally {
			logout();
		}
		//
		getHelper().createUuidPolicy(role.getId(), primeContract.getId(), IdmBasePermission.UPDATE);
		//
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			Set<String> permissions = service.getPermissions(positionOne);
			Assert.assertEquals(4, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.UPDATE.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.CREATE.name())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.DELETE.name())));
		} finally {
			logout();
		}
	}	
}
