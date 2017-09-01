package eu.bcvsolutions.idm.acc.security.evaluator;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.InitDemoData;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.service.LoginService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Permission for identity account by identity
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityAccountByRoleEvaluatorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private LoginService loginService;
	@Autowired private AccAccountService accountService;
	@Autowired private AccIdentityAccountService identityAccountService;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	@Autowired private IdmRoleService roleService;
	
	@Test
	public void testCanReadIdentityAccount() {
		IdmIdentityDto identity;
		AccIdentityAccountDto accountIdentityOne;
		try {
			loginAsAdmin(InitApplicationData.ADMIN_USERNAME);
			//
			identity = helper.createIdentity();
			SysSystem system = helper.createTestResourceSystem(true);
			
			AccAccountDto accountOne = new AccAccountDto();
			accountOne.setSystem(system.getId());
			accountOne.setUid(identity.getUsername());
			accountOne.setAccountType(AccountType.PERSONAL);
			accountOne = accountService.save(accountOne);

			accountIdentityOne = new AccIdentityAccountDto();
			accountIdentityOne.setIdentity(identity.getId());
			accountIdentityOne.setOwnership(true);
			accountIdentityOne.setAccount(accountOne.getId());
			accountIdentityOne = identityAccountService.save(accountIdentityOne);
			
			IdmRoleDto role = helper.createRole();
			IdmRoleDto defaultRole = roleService.getByCode(InitDemoData.DEFAULT_ROLE_NAME);
			
			IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
			policy.setRole(role.getId());
			policy.setGroupPermission(AccGroupPermission.IDENTITYACCOUNT.getName());
			policy.setAuthorizableType(AccIdentityAccount.class.getCanonicalName());
			policy.setEvaluator(IdentityAccountByIdentityEvaluator.class);
			authorizationPolicyService.save(policy);
			
			helper.createIdentityRole(identity, role);
			helper.createIdentityRole(identity, defaultRole);
		} finally {
			logout();
		}
		// check
		try {
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			AccIdentityAccountDto read = identityAccountService.get(accountIdentityOne.getId(), IdmBasePermission.READ);
			Assert.assertEquals(accountIdentityOne, read);
		} finally {
			logout();
		}
	}
	
	@Test(expected = ForbiddenEntityException.class)
	public void testCannotReadIdentityAccount() {
		IdmIdentityDto identity;
		AccIdentityAccountDto accountIdentityOne;
		try {
			loginAsAdmin(InitApplicationData.ADMIN_USERNAME);
			//
			identity = helper.createIdentity();
			SysSystem system = helper.createTestResourceSystem(true);
			
			AccAccountDto accountOne = new AccAccountDto();
			accountOne.setSystem(system.getId());
			accountOne.setUid(identity.getUsername());
			accountOne.setAccountType(AccountType.PERSONAL);
			accountOne = accountService.save(accountOne);

			accountIdentityOne = new AccIdentityAccountDto();
			accountIdentityOne.setIdentity(identity.getId());
			accountIdentityOne.setOwnership(true);
			accountIdentityOne.setAccount(accountOne.getId());
			accountIdentityOne = identityAccountService.save(accountIdentityOne);
		} finally {
			logout();
		}
		// check
		try {
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			identityAccountService.get(accountIdentityOne.getId(), IdmBasePermission.READ);
		} finally {
			logout();
		}
	}
}
