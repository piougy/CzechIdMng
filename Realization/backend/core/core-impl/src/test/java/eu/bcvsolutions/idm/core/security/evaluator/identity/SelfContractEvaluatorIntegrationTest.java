package eu.bcvsolutions.idm.core.security.evaluator.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractEvaluatorIntegrationTest;

/**
 * Tests for {@link SelfContractEvaluator}.
 *
 * @author Radek Tomiška
 */
@Transactional
public class SelfContractEvaluatorIntegrationTest extends AbstractEvaluatorIntegrationTest {

	@Autowired
	private IdmIdentityContractService contractService;
	
	@Test
	public void testGreenLine() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity);
		getHelper().createIdentity(); // other
		getHelper().createIdentityRole(identity, role);

		// try get identity role
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			List<IdmIdentityContractDto> contracts = contractService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(contracts.isEmpty());	
		} finally {
			logout();
		}

		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				role.getId(),
				CoreGroupPermission.IDENTITYCONTRACT,
				IdmIdentityContract.class,
				SelfContractEvaluator.class,
				IdmBasePermission.READ);
		
		// get identity role after add authorization policy
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			List<IdmIdentityContractDto> contracts = contractService.find(null, IdmBasePermission.READ).getContent();
			assertFalse(contracts.isEmpty());
			assertEquals(1, contracts.size());
			IdmIdentityContractDto contract = contracts.get(0);
			Assert.assertEquals(primeContract.getId(), contract.getId());
			//
			Set<String> contractPermissions = contractService.getPermissions(contract);
			Assert.assertEquals(1, contractPermissions.size());
			Assert.assertTrue(contractPermissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
		} finally {
			logout();
		}
	}
}
