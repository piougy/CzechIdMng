package eu.bcvsolutions.idm.acc.event.processor.identity;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableList;

import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Generate single password to all identiy systems
 * 
 * @author Radek Tomiška
 *
 */
public class IdentitySetPasswordProcessorIntegrationTest extends AbstractIntegrationTest {

	private static final String IDENTITY_PASSWORD_ONE = "password_one";
	//	
	@Autowired private TestHelper helper;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private AccIdentityAccountService identityAccountService;
	@Autowired private AccAccountService accountService;
	
	@Before
	public void init() {
		loginAsAdmin(InitApplicationData.ADMIN_USERNAME);
	}

	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testGeneratePassword() {
		SysSystemDto system = helper.createTestResourceSystem(true);
		//
		IdmRoleDto role = helper.createRole();
		helper.createRoleSystem(role, system);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto contract = helper.getPrimeContract(identity.getId());
		contract.setValidFrom(new LocalDate().plusDays(1));
		identityContractService.save(contract);
		identity = identityService.get(identity.getId());
		Assert.assertEquals(IdentityState.FUTURE_CONTRACT, identity.getState());
		helper.createIdentityRole(identity, role);
		//
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		AccIdentityAccountDto accountIdentityOne = identityAccountService.find(filter, null).getContent().get(0);
		AccAccountDto account = accountService.get(accountIdentityOne.getAccount());
		// Create new password one
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setAccounts(ImmutableList.of(account.getId().toString()));
		passwordChange.setNewPassword(new GuardedString(IDENTITY_PASSWORD_ONE));
		passwordChange.setIdm(true);
		//
		// Do change of password for selected accounts
		identityService.passwordChange(identity, passwordChange);
		//
		// Check correct password One
		TestResource resource = helper.findResource(account.getRealUid());
		Assert.assertNotNull(resource);
		Assert.assertEquals(IDENTITY_PASSWORD_ONE, resource.getPassword());
		//
		// set contract to valid
		contract.setValidFrom(new LocalDate());
		identityContractService.save(contract);
		identity = identityService.get(identity.getId());
		Assert.assertEquals(IdentityState.VALID, identity.getState());
		//
		// check password on target system was changed
		resource = helper.findResource(account.getRealUid());
		Assert.assertNotNull(resource);
		Assert.assertNotEquals(IDENTITY_PASSWORD_ONE, resource.getPassword());
	}
}
