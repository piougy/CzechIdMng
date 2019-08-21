package eu.bcvsolutions.idm.core.security.evaluator.identity;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Permission to contract guarantee by contract
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class ContractGuaranteeByIdentityContractEvaluatorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdmContractGuaranteeService service;
	@Autowired private IdmIdentityContractService identityContractService;
	
	@Test
	public void testCrud() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity);
		IdmIdentityContractDto otherContract = getHelper().createIdentityContact(identity);
		List<IdmContractGuaranteeDto> guarantees = null;
		IdmRoleDto role = getHelper().createRole();
		
		IdmContractGuaranteeDto guaranteeOne = getHelper().createContractGuarantee(primeContract, getHelper().createIdentity());
		getHelper().createContractGuarantee(otherContract.getId(), getHelper().createIdentity().getId()); // other
		
		getHelper().createIdentityRole(identity, role);
		getHelper().createUuidPolicy(role.getId(), primeContract.getId(), IdmBasePermission.READ);
		//
		// check - read without transient policy
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			Assert.assertEquals(primeContract.getId(), identityContractService.get(primeContract.getId(), IdmBasePermission.READ).getId());
			guarantees = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(guarantees.isEmpty());	
		} finally {
			logout();
		}
		//
		// without login
		guarantees = service.find(null, IdmBasePermission.READ).getContent();
		Assert.assertTrue(guarantees.isEmpty());
		//
		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.CONTRACTGUARANTEE,
				IdmContractGuarantee.class,
				ContractGuaranteeByIdentityContractEvaluator.class);
		//
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			// evaluate	access
			getHelper().login(identity.getUsername(), identity.getPassword());
			guarantees = service.find(null, IdmBasePermission.READ).getContent();
			Assert.assertEquals(1, guarantees.size());	
			Assert.assertEquals(guaranteeOne.getId(), guarantees.get(0).getId());
			//
			Set<String> permissions = service.getPermissions(guaranteeOne);
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
			Set<String> permissions = service.getPermissions(guaranteeOne);
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
