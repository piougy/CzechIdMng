package eu.bcvsolutions.idm.core.security.evaluator.role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.service.LoginService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * @author Peter Sourek
 */
public class RoleForRequestEvaluatorIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private IdmRoleService roleService;

	@Autowired
	private IdmAuthorizationPolicyService authorizationPolicyService;

	@Autowired
	private IdmIdentityService identityService;

	@Autowired
	private IdmIdentityRoleService identityRoleService;

	@Autowired
	IdmIdentityContractService contractService;

	@Autowired
	LoginService loginService;

	private RoleCanBeRequestedEvaluator evaluator;
	private IdmRole canBeRequested, cannotBeRequested;
	private IdmIdentityDto hasNoRole, hasNoRights, hasRoleEvaluator, hasEvaluatorUpdate, hasEvaluatorAllRights;

	private final String TEST_PWD = "aaaAAAa12345789000*bcv";

	@Before
	public void prepareTestData() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		evaluator = new RoleCanBeRequestedEvaluator(securityService);

		hasNoRole = createUser(uniqueString(), TEST_PWD);
		//
		IdmRole noRightsRole = createRole(uniqueString(), false);
		hasNoRights = createUser(uniqueString(), TEST_PWD, noRightsRole);
		//
		IdmRole readRightsRole = createRole(uniqueString(), false, IdmBasePermission.READ);
		hasRoleEvaluator = createUser(uniqueString(), TEST_PWD, readRightsRole);
		//
		IdmRole writeRightsRole = createRole(uniqueString(), false, IdmBasePermission.UPDATE, IdmBasePermission.READ);
		hasEvaluatorUpdate = createUser(uniqueString(), TEST_PWD, writeRightsRole);
		//
		IdmRole allRightsRole = createRole(uniqueString(), false, IdmBasePermission.ADMIN);
		hasEvaluatorAllRights = createUser(uniqueString(), TEST_PWD, allRightsRole);
		//
		canBeRequested = createRole(uniqueString(), true);
		cannotBeRequested = createRole(uniqueString(), false);
		logout();
	}

	@Test
	public void testSupports() {
		// Just few of these just to be sure even though it is probably unnecessary
		assertTrue(evaluator.supports(IdmRole.class));
		assertFalse(evaluator.supports(BaseEntity.class));
		assertFalse(evaluator.supports(IdmIdentityDto.class));
	}

	@Test
	public void testCase1() {
		// User who has no rights (has role which has no permission) should not be able to see role no matter if it can be requested or not
		Page<IdmRole> res1 = getRoleAsUser(hasNoRights, canBeRequested);
		assertEquals(0, res1.getTotalElements());
		//
		Page<IdmRole> res2 = getRoleAsUser(hasNoRights, cannotBeRequested);
		assertEquals(0, res2.getTotalElements());
	}

	@Test
	public void testCase2() {
		// User with READ permission and tested evaluator should be able to see role that can be requested
		Page<IdmRole> res1 = getRoleAsUser(hasRoleEvaluator, canBeRequested);
		assertEquals(1, res1.getTotalElements());
		assertEquals(res1.getContent().get(0).getId(), canBeRequested.getId());
		//
		Page<IdmRole> res2 = getRoleAsUser(hasRoleEvaluator, cannotBeRequested);
		assertEquals(0, res2.getTotalElements());
	}

	@Test
	public void testCase3() {
		// User without roles should not be able to see any role
		Page<IdmRole> res1 = getRoleAsUser(hasNoRole, canBeRequested);
		assertEquals(0, res1.getTotalElements());
		//
		Page<IdmRole> res2 = getRoleAsUser(hasNoRole, cannotBeRequested);
		assertEquals(0, res2.getTotalElements());
	}

	@Test
	public void testCase4() {
		// User with UPDATE permission and tested evaluator should be able to see role that can be requested and also should be able to update it
		Page<IdmRole> res1 = getRoleAsUser(hasEvaluatorUpdate, canBeRequested);
		assertEquals(1, res1.getTotalElements());
		IdmRole found1 = res1.getContent().get(0);
		assertEquals(found1.getId(), canBeRequested.getId());
		// TODO: uncomment when role service is refactored to DTOs
		/*final String testDescription = uniqueString();
		saveRoleAsUser(hasEvaluatorUpdate, found1, testDescription);
		Page<IdmRole> res2 = getRoleAsUser(hasEvaluatorUpdate, canBeRequested);
		IdmRole found2 = res2.getContent().get(0);
		assertEquals(found2.getDescription(), testDescription);*/
		//
		Page<IdmRole> res3 = getRoleAsUser(hasEvaluatorUpdate, cannotBeRequested);
		assertEquals(0, res3.getTotalElements());
	}

	@Test
	public void testCase5() {
		// User with ADMIN permission and tested evaluator should be able to see role that can be requested and also should be able to update it and delete it
		Page<IdmRole> res1 = getRoleAsUser(hasEvaluatorAllRights, canBeRequested);
		assertEquals(1, res1.getTotalElements());
		IdmRole found1 = res1.getContent().get(0);
		assertEquals(found1.getId(), canBeRequested.getId());
		//TODO: uncomment when role service is refactored to DTOs
		/*final String testDescription = uniqueString();
		Exception e = saveRoleAsUser(hasEvaluatorAllRights, found1, testDescription);
		assertNull(e);
		Page<IdmRole> res2 = getRoleAsUser(hasEvaluatorAllRights, canBeRequested);
		IdmRole found2 = res2.getContent().get(0);
		assertEquals(found2.getDescription(), testDescription);
		//
		Exception e2 = deleteRoleAsUser(hasEvaluatorAllRights, found2);
		assertNull(e2);
		Page<IdmRole> res3 = getRoleAsUser(hasEvaluatorAllRights, canBeRequested);
		assertEquals(0, res3.getTotalElements());*/
		//
		//
		//
		Page<IdmRole> res4 = getRoleAsUser(hasEvaluatorAllRights, cannotBeRequested);
		assertEquals(0, res4.getTotalElements());
		//TODO: uncomment when role service is refactored to DTOs
		/*cannotBeRequested.setDescription(testDescription);
		Exception e3 = saveRoleAsUser(hasEvaluatorAllRights, cannotBeRequested, testDescription);
		assertNotNull(e3);
		IdmRole found3 = roleService.get(cannotBeRequested.getId());
		assertNotEquals(found3.getDescription(), testDescription);
		//
		Exception e4 = deleteRoleAsUser(hasEvaluatorAllRights, cannotBeRequested);
		assertNotNull(e4);
		IdmRole res6 = roleService.get(cannotBeRequested.getId());
		assertNotNull(res6);*/
	}


	// ************************ HELPER METHODS ***************************

	private Exception saveRoleAsUser(IdmIdentityDto user, IdmRole found1, String testDescription) {
		try {
			loginService.login(new LoginDto(user.getUsername(), new GuardedString(TEST_PWD)));
			IdmRole fnd = roleService.get(found1.getId());
			fnd.setDescription(testDescription);
			// TODO: Use save(dto, UPDATE) when service is refactored to use DTOs
			roleService.save(fnd);
		} catch (Exception o_O) {
			return o_O;
		} finally {
			logout();
		}
		return null;
	}

	private Exception deleteRoleAsUser(IdmIdentityDto user, IdmRole role) {
		try {
			loginService.login(new LoginDto(user.getUsername(), new GuardedString(TEST_PWD)));
			// TODO: Use delete(dto, DELETE) when service is refactored to use DTOs
			roleService.delete(role);
		} catch (Exception o_O) {
			return o_O;
		} finally {
			logout();
		}
		return null;
	}


	private Page<IdmRole> getRoleAsUser(IdmIdentityDto user, IdmRole role) {
		try {
			loginService.login(new LoginDto(user.getUsername(), new GuardedString(TEST_PWD)));
			//
			RoleFilter rf = getRoleFilter("name", role.getName());
			Page<IdmRole> readRole = roleService.findSecured(rf, null, IdmBasePermission.READ);
			return readRole;
		} finally {
			logout();
		}
	}

	private RoleFilter getRoleFilter(String prop, String val) {
		RoleFilter rf = new RoleFilter();
		rf.setProperty(prop);
		rf.setValue(val);
		return rf;
	}

	private IdmRole createRole(String name, boolean canBeRequested, IdmBasePermission... permissions) {
		IdmRole roleWithNoRights = new IdmRole();
		roleWithNoRights.setCanBeRequested(canBeRequested);
		roleWithNoRights.setName(name);
		final IdmRole result = this.roleService.save(roleWithNoRights);
		//
		if (permissions != null && permissions.length > 0) {
			createPolicy(result.getId(), permissions);
		}
		//
		return result;
	}

	private IdmAuthorizationPolicyDto createPolicy(UUID roleId, IdmBasePermission... permissions) {
		IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
		// add autocomplete data access
		policy.setPermissions(permissions);
		policy.setGroupPermission(CoreGroupPermission.ROLE.getName());
		policy.setAuthorizableType(IdmRole.class.getCanonicalName());
		policy.setRole(roleId);
		policy.setEvaluator(RoleCanBeRequestedEvaluator.class);
		return authorizationPolicyService.save(policy);
	}

	private IdmIdentityDto createUser(String name, String password, IdmRole... roles) {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setEmail(RandomStringUtils.randomAlphabetic(10) + "@email.com");
		identity.setLastName(name);
		identity.setFirstName(name);
		identity.setUsername(name);
		identity.setPassword(new GuardedString(password));
		final IdmIdentityDto result = identityService.save(identity);
		//
		IdmIdentityContractDto contract = createContract(result);
		assignRoles(contract, roles);
		//
		return result;
	}

	private void assignRoles(IdmIdentityContractDto contract, IdmRole... roles) {
		for (IdmRole role : roles) {
			IdmIdentityRoleDto idr = new IdmIdentityRoleDto();
			idr.setRole(role.getId());
			idr.setIdentityContract(contract.getId());
			identityRoleService.save(idr);
		}
	}

	private IdmIdentityContractDto createContract(IdmIdentityDto result) {
		IdmIdentityContractDto dto = new IdmIdentityContractDto();
		dto.setIdentity(result.getId());
		dto.setPosition("MY_DEFAULT");
		return contractService.save(dto);
	}

	private String uniqueString() {
		String rnd = RandomStringUtils.randomAlphabetic(10);
		final long time = System.nanoTime();
		return rnd + "_" + time;
	}

}
