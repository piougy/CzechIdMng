package eu.bcvsolutions.idm.core.model.event.processor;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTokenFilter;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;

/**
 * Tests IdmRole's authority modifications, which should disable all tokens with this role involved.
 * 
 * To compute the difference in role authorities, one must checkout the
 * original entity from storage, {@see IdmRoleRepository#getPersistedRoleAuthorities(IdmRole)}.
 * But since tests are usually run in single transaction, the role
 * is not persisted into the storage and we can not find the original
 * entity. Therefore there are multiple transactions in each test,
 * one to create new role, another to update its authorities.
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 *
 */
public class PermissionsAuthorityChangeProcessorTest extends AbstractIdentityAuthoritiesProcessorTest {
	
	@Autowired private IdmAuthorizationPolicyRepository policyRepository;
	
	@Test
	public void testRemoveAuthorityUpdateUsers() throws Exception {
		IdmRoleDto role = getTestRole();
		IdmIdentityDto i = getHelper().createIdentity();
		IdmIdentityContractDto c = getTestContract(i);
		getTestIdentityRole(role, c);
		//
		List<IdmTokenDto> tokens = tokenManager.getTokens(i);
		//
		Assert.assertTrue(tokens.isEmpty());
		//
		// login - one token
		getHelper().login(i.getUsername(), i.getPassword());
		try {
			tokens = tokenManager.getTokens(i);
			Assert.assertEquals(1, tokens.size());
			Assert.assertFalse(tokens.get(0).isDisabled());
			
			clearAuthPolicies(role);
			
			tokens = tokenManager.getTokens(i);
			Assert.assertEquals(1, tokens.size());
			Assert.assertTrue(tokens.get(0).isDisabled());
		} finally {
			getHelper().logout();
		}
	}

	@Test
	public void testAddAuthorityUpdateUsers() throws Exception {
		IdmRoleDto role = getTestRole();
		IdmIdentityDto i = getHelper().createIdentity();
		IdmIdentityContractDto c = getTestContract(i);
		getTestIdentityRole(role, c);
		//
		IdmTokenFilter filter = new IdmTokenFilter();
		filter.setOwnerType(tokenManager.getOwnerType(i.getClass()));
		filter.setOwnerId(i.getId());
		List<IdmTokenDto> tokens = tokenManager.getTokens(i);
		//
		Assert.assertTrue(tokens.isEmpty());
		//
		// login - one token
		getHelper().login(i.getUsername(), i.getPassword());
		try {
			tokens = tokenManager.getTokens(i);
			Assert.assertEquals(1, tokens.size());
			Assert.assertFalse(tokens.get(0).isDisabled());
		
			getTransactionTemplate().execute(new TransactionCallback<Object>() {
				public Object doInTransaction(TransactionStatus transactionStatus) {
					createTestPolicy(role, IdmBasePermission.EXECUTE, IdmGroupPermission.APP);
					return null;
				}
			});
			// add role - token should not be removed
			tokens = tokenManager.getTokens(i);
			Assert.assertEquals(1, tokens.size());
			Assert.assertFalse(tokens.get(0).isDisabled());
		} finally {
			getHelper().logout();
		}
	}
	
	/**
	 * Change permissions type for given policy.
	 * @throws Exception
	 */
	@Test
	public void testChangePersmissions() throws Exception {
		IdmRoleDto role = getTestRole();
		IdmIdentityDto i = getHelper().createIdentity();
		IdmIdentityContractDto c = getTestContract(i);
		getTestIdentityRole(role, c);
		//
		IdmTokenFilter filter = new IdmTokenFilter();
		filter.setOwnerType(tokenManager.getOwnerType(i.getClass()));
		filter.setOwnerId(i.getId());
		List<IdmTokenDto> tokens = tokenManager.getTokens(i);
		//
		Assert.assertTrue(tokens.isEmpty());
		//
		// login - one token
		getHelper().login(i.getUsername(), i.getPassword());
		try {	
			tokens = tokenManager.getTokens(i);
			Assert.assertEquals(1, tokens.size());
			Assert.assertFalse(tokens.get(0).isDisabled());
			//
			changeAuthorizationPolicyPermissions(role);
			//
			tokens = tokenManager.getTokens(i);
			Assert.assertEquals(1, tokens.size());
			Assert.assertTrue(tokens.get(0).isDisabled());
		} finally {
			getHelper().logout();
		}
	}

	private void changeAuthorizationPolicyPermissions(IdmRoleDto role) {
		getTransactionTemplate().execute(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus status) {
				authorizationPolicyService.getRolePolicies(role.getId(), false)
				.forEach(policy -> {
					policy.setGroupPermission(CoreGroupPermission.AUDIT_READ);
					authorizationPolicyService.save(policy);
				});
				return null;
			}
		});
	}

	private void clearAuthPolicies(IdmRoleDto role) {
		getTransactionTemplate().execute(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus transactionStatus) {
				List<IdmAuthorizationPolicy> policies = policyRepository.getPolicies(role.getId(), false);
				policies.forEach(policy -> authorizationPolicyService.deleteById(policy.getId()));
				return null;
			}
		});
	}

}
