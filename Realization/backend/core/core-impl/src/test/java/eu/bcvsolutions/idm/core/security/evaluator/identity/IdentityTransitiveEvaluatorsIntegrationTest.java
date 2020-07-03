package eu.bcvsolutions.idm.core.security.evaluator.identity;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.testng.collections.Lists;

import eu.bcvsolutions.idm.InitDemoData;
import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.rest.impl.PasswordChangeController;
import eu.bcvsolutions.idm.core.security.api.domain.ContractBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
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
@Transactional
public class IdentityTransitiveEvaluatorsIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private LoginService loginService;
	@Autowired private PasswordChangeController passwordChangeController;
	
	private IdmIdentityDto prepareIdentityWithAssignedRole() {
		loginAsAdmin();
		// get default role
		IdmRoleDto role = roleService.getByCode(InitDemoData.DEFAULT_ROLE_NAME);
		// prepare identity
		IdmIdentityDto identity = helper.createIdentity();
		// assign role
		helper.createIdentityRole(identity, role);
		logout();
		//
		return identity;
	}
	
	@Test
	public void testReadContractAndAssignedRole() {
		IdmIdentityDto identity = prepareIdentityWithAssignedRole();
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
			Set<String> contractPermissions = identityContractService.getPermissions(contract);
			Assert.assertTrue(contractPermissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertTrue(contractPermissions.stream().anyMatch(p -> p.equals(IdentityBasePermission.CHANGEPERMISSION.getName())));
			//
			List<IdmIdentityRoleDto> roles = identityRoleService.find(null, IdmBasePermission.READ).getContent();
			assertEquals(1, roles.size());	
			assertEquals(contract.getId(), roles.get(0).getIdentityContract());
		} finally {
			logout();
		}
	}
	
	@Test
	public void testTransitiveContractPermissions() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		getHelper().createIdentityRole(identity, role);
		getHelper().createBasePolicy(
				role.getId(), 
				CoreGroupPermission.IDENTITY, 
				IdmIdentity.class, 
				IdmBasePermission.READ, IdmBasePermission.UPDATE);
		getHelper().createAuthorizationPolicy(
				role.getId(), 
				CoreGroupPermission.IDENTITYCONTRACT, 
				IdmIdentityContract.class, 
				IdentityContractByIdentityEvaluator.class);
		//
		try {			
			getHelper().login(identity);
			//
			IdmIdentityDto read = identityService.get(identity.getId(), IdmBasePermission.READ);
			assertEquals(identity, read);
			//
			IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
			filter.setIdentity(identity.getId());
			List<IdmIdentityContractDto> contracts = identityContractService.find(filter, null, IdmBasePermission.READ).getContent();
			assertEquals(1, contracts.size());	
			IdmIdentityContractDto contract = contracts.get(0);
			Set<String> contractPermissions = identityContractService.getPermissions(contract);
			Assert.assertTrue(contractPermissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertTrue(contractPermissions.stream().anyMatch(p -> p.equals(IdmBasePermission.UPDATE.getName())));
		} finally {
			logout();
		}
	}
	
	@Test
	public void testIncludeTransitiveContractPermissions() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmRoleDto role = getHelper().createRole();
		getHelper().createIdentityRole(identity, role);
		getHelper().createBasePolicy(
				role.getId(), 
				CoreGroupPermission.IDENTITY, 
				IdmIdentity.class, 
				IdmBasePermission.READ, IdmBasePermission.UPDATE, IdmBasePermission.DELETE);
		
		ConfigurationMap properties = new ConfigurationMap();
		properties.put(
				IdentityContractByIdentityEvaluator.PARAMETER_INCLUDE_PERMISSIONS, 
				StringUtils.join(Lists.newArrayList(IdmBasePermission.READ.getName(), IdmBasePermission.UPDATE.getName()), ","));
		getHelper().createAuthorizationPolicy(
				role.getId(), 
				CoreGroupPermission.IDENTITYCONTRACT, 
				IdmIdentityContract.class, 
				IdentityContractByIdentityEvaluator.class,
				properties);
		//
		try {			
			getHelper().login(identity);
			//
			IdmIdentityDto read = identityService.get(identity.getId(), IdmBasePermission.READ);
			assertEquals(identity, read);
			//
			IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
			filter.setIdentity(identity.getId());
			List<IdmIdentityContractDto> contracts = identityContractService.find(filter, null, IdmBasePermission.READ).getContent();
			assertEquals(1, contracts.size());	
			IdmIdentityContractDto contract = contracts.get(0);
			Set<String> contractPermissions = identityContractService.getPermissions(contract);
			Assert.assertTrue(contractPermissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertTrue(contractPermissions.stream().anyMatch(p -> p.equals(IdmBasePermission.UPDATE.getName())));
			Assert.assertTrue(contractPermissions.stream().allMatch(p -> !p.equals(IdmBasePermission.DELETE.getName())));
		} finally {
			logout();
		}
	}
	
	@Test(expected = ForbiddenEntityException.class)
	public void testReadForUpdateProfile() {
		IdmIdentityDto identity = prepareIdentityWithAssignedRole();
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
		IdmIdentityDto identity = prepareIdentityWithAssignedRole();
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
		IdmIdentityDto identity = prepareIdentityWithAssignedRole();
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
		IdmIdentityDto identity = prepareIdentityWithAssignedRole();
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
		IdmIdentityDto identity = prepareIdentityWithAssignedRole();
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
	
	@Test
	public void testTransitiveContractPermissionsForSelfAndSubordinate() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityDto manager = getHelper().createIdentity();
		//
		IdmRoleDto selfRole = getHelper().createRole();
		IdmRoleDto managerRole = getHelper().createRole();
		//
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity);
		Assert.assertNotNull(primeContract);
		IdmIdentityContractDto otherContract = getHelper().createIdentityContact(identity);
		getHelper().createContractGuarantee(otherContract, manager);
		//
		getHelper().createIdentityRole(identity, selfRole);
		getHelper().createIdentityRole(manager, selfRole); // manager has both
		getHelper().createIdentityRole(manager, managerRole);
		// self
		getHelper().createAuthorizationPolicy(
				selfRole.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				SelfIdentityEvaluator.class,
				IdmBasePermission.AUTOCOMPLETE,
				IdmBasePermission.READ,
				IdentityBasePermission.CHANGEPERMISSION);
		ConfigurationMap properties = new ConfigurationMap();
		properties.put(
				IdentityContractByIdentityEvaluator.PARAMETER_INCLUDE_PERMISSIONS,
				// same configuration as doc - autocomplete is needed for FE
				StringUtils.join(Lists.newArrayList(IdmBasePermission.AUTOCOMPLETE.getName(), IdmBasePermission.READ.getName(), ContractBasePermission.CHANGEPERMISSION.getName()), ","));
		getHelper().createAuthorizationPolicy(
				selfRole.getId(), 
				CoreGroupPermission.IDENTITYCONTRACT, 
				IdmIdentityContract.class, 
				IdentityContractByIdentityEvaluator.class,
				properties);
		// manager
		getHelper().createAuthorizationPolicy(
				managerRole.getId(),
				CoreGroupPermission.IDENTITYCONTRACT,
				IdmIdentityContract.class,
				SubordinateContractEvaluator.class,
				IdmBasePermission.AUTOCOMPLETE,
				IdmBasePermission.READ,
				IdentityBasePermission.CHANGEPERMISSION);
		getHelper().createBasePolicy(
				managerRole.getId(),
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				IdmBasePermission.AUTOCOMPLETE,
				IdmBasePermission.READ);
		//
		try {			
			getHelper().login(identity);
			//
			IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
			filter.setIdentity(identity.getId());
			List<IdmIdentityContractDto> contracts = identityContractService.find(filter, null, ContractBasePermission.CHANGEPERMISSION).getContent();
			assertEquals(2, contracts.size());	
			IdmIdentityContractDto contract = contracts.get(0);
			Set<String> contractPermissions = identityContractService.getPermissions(contract);
			Assert.assertTrue(contractPermissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertTrue(contractPermissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(contractPermissions.stream().anyMatch(p -> p.equals(ContractBasePermission.CHANGEPERMISSION.getName())));
		} finally {
			logout();
		}
		// change permission for one
		try {			
			getHelper().login(manager);
			//
			IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
			filter.setIdentity(identity.getId());
			List<IdmIdentityContractDto> contracts = identityContractService.find(filter, null, ContractBasePermission.CHANGEPERMISSION).getContent();
			assertEquals(1, contracts.size());	
			IdmIdentityContractDto contract = contracts.get(0);
			Assert.assertEquals(otherContract.getId(), contract.getId());
			Set<String> contractPermissions = identityContractService.getPermissions(contract);
			Assert.assertTrue(contractPermissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertTrue(contractPermissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(contractPermissions.stream().anyMatch(p -> p.equals(ContractBasePermission.CHANGEPERMISSION.getName())));
		} finally {
			logout();
		}
		// read both
		try {			
			getHelper().login(manager);
			//
			IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
			filter.setIdentity(identity.getId());
			List<IdmIdentityContractDto> contracts = identityContractService.find(filter, null, IdmBasePermission.READ).getContent();
			assertEquals(2, contracts.size());	
		} finally {
			logout();
		}
	}
}

