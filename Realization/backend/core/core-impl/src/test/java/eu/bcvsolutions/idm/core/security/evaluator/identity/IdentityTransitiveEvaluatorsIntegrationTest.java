package eu.bcvsolutions.idm.core.security.evaluator.identity;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitDemoData;
import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.rest.impl.PasswordChangeController;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Test whole self identity profile read and password change
 * - based on default role inicialization, but default role could be inited here to
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityTransitiveEvaluatorsIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private LoginService loginService;
	@Autowired private PasswordChangeController passwordChangeController;
	
	private IdmIdentityDto prepareIdentityProfile() {
		loginAsAdmin();
		GuardedString password = new GuardedString("heslo");
		// get default role
		IdmRoleDto role = roleService.getByCode(InitDemoData.DEFAULT_ROLE_NAME);
		// prepare identity
		IdmIdentityDto identity = helper.createIdentity();
		identity.setPassword(password);
		identity = identityService.save(identity);
		// assign role
		helper.createIdentityRole(identity, role);
		logout();
		//
		// password is transient, some test except password back in identity
		identity.setPassword(password);
		//
		return identity;
	}
	
	@Test
	public void testReadProfile() {
		IdmIdentityDto identity = prepareIdentityProfile();
		//
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			IdmIdentityDto read = identityService.get(identity.getId(), IdmBasePermission.READ);
			assertEquals(identity, read);
			//			
			List<IdmIdentityContractDto> contracts = identityContractService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(1, contracts.size());	
			IdmIdentityContractDto contract = contracts.get(0);
			assertEquals(identity.getId(), contract.getIdentity());
			//
			List<IdmIdentityRoleDto> roles = identityRoleService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(1, roles.size());	
			assertEquals(contract.getId(), roles.get(0).getIdentityContract());
		} finally {
			logout();
		}
	}
	
	@Test(expected = ForbiddenEntityException.class)
	public void testReadForUpdateProfile() {
		IdmIdentityDto identity = prepareIdentityProfile();
		//
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			identityService.get(identity.getId(), IdmBasePermission.UPDATE);
		} finally {
			logout();
		}
	}
	
	@Test(expected = ForbiddenEntityException.class)
	public void testUpdateProfile() {
		IdmIdentityDto identity = prepareIdentityProfile();
		//
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			identityService.save(identity, IdmBasePermission.UPDATE);
		} finally {
			logout();
		}
	}	
	
	@Test(expected = ForbiddenEntityException.class)
	public void testUpdateIdentityContract() {
		IdmIdentityDto identity = prepareIdentityProfile();
		//
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			IdmIdentityContractDto contract = identityContractService.getPrimeContract(identity.getId());
			//
			identityContractService.save(contract, IdmBasePermission.UPDATE);
		} finally {
			logout();
		}
	}
	
	@Test
	public void testChangePassword() {
		IdmIdentityDto identity = prepareIdentityProfile();
		//
		try {			
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
			passwordChangeDto.setIdm(true);
			passwordChangeDto.setAll(true);
			passwordChangeDto.setOldPassword(identity.getPassword());
			passwordChangeDto.setNewPassword(new GuardedString("heslo2"));
			passwordChangeController.passwordChange(identity.getId().toString(), passwordChangeDto);
		} finally {
			logout();
		}
	}
	
	@Test(expected = ForbiddenEntityException.class)
	public void testChangeForeignPassword() {
		IdmIdentityDto identity = prepareIdentityProfile();
		//
		try {		
			loginService.login(new LoginDto(identity.getUsername(), identity.getPassword()));
			//
			PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
			passwordChangeDto.setIdm(true);
			passwordChangeDto.setOldPassword(identity.getPassword());
			passwordChangeDto.setNewPassword(new GuardedString("heslo2"));
			passwordChangeController.passwordChange(InitTestData.TEST_ADMIN_USERNAME, passwordChangeDto);
		} finally {
			logout();
		}
	}
}

