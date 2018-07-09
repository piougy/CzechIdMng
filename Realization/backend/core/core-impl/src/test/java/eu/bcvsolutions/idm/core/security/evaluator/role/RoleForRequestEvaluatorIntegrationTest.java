package eu.bcvsolutions.idm.core.security.evaluator.role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Role - can be requested flag tests (security)
 * 
 * @author Peter Sourek
 * @author Radek Tomiška
 */
public class RoleForRequestEvaluatorIntegrationTest extends AbstractIntegrationTest {

	private final String TEST_PWD = "aaaAAAa12345789000*bcv";
	//
	@Autowired private SecurityService securityService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmIdentityContractService contractService;
	@Autowired private LoginService loginService;
	//
	private RoleCanBeRequestedEvaluator evaluator;
	private IdmRoleDto canBeRequested, cannotBeRequested;
	private IdmIdentityDto hasNoRole, hasNoRights, hasRoleEvaluator, hasEvaluatorUpdate, hasEvaluatorAllRights;

	@Before
	public void prepareTestData() {
		loginAsAdmin();
		evaluator = new RoleCanBeRequestedEvaluator(securityService);

		hasNoRole = createUser(uniqueString(), TEST_PWD);
		//
		IdmRoleDto noRightsRole = createRole(uniqueString(), false);
		hasNoRights = createUser(uniqueString(), TEST_PWD, noRightsRole);
		//
		IdmRoleDto readRightsRole = createRole(uniqueString(), false, IdmBasePermission.READ);
		hasRoleEvaluator = createUser(uniqueString(), TEST_PWD, readRightsRole);
		//
		IdmRoleDto writeRightsRole = createRole(uniqueString(), false, IdmBasePermission.UPDATE, IdmBasePermission.READ);
		hasEvaluatorUpdate = createUser(uniqueString(), TEST_PWD, writeRightsRole);
		//
		IdmRoleDto allRightsRole = createRole(uniqueString(), false, IdmBasePermission.ADMIN);
		hasEvaluatorAllRights = createUser(uniqueString(), TEST_PWD, allRightsRole);
		//
		canBeRequested = createRole(uniqueString(), true);
		cannotBeRequested = createRole(uniqueString(), false);
		logout();
	}

	/**
	 * Just few of these just to be sure even though it is probably unnecessary
	 */
	@Test
	public void testSupports() {
		assertTrue(evaluator.supports(IdmRole.class));
		assertFalse(evaluator.supports(BaseEntity.class));
		assertFalse(evaluator.supports(IdmIdentityDto.class));
	}
	
	/**
	 * User without roles should not be able to see any role
	 */
	@Test
	public void testCannotReadNoRoleAssigned() {
		Page<IdmRoleDto> res1 = getRoleAsUser(hasNoRole, canBeRequested);
		assertEquals(0, res1.getTotalElements());
		//
		Page<IdmRoleDto> res2 = getRoleAsUser(hasNoRole, cannotBeRequested);
		assertEquals(0, res2.getTotalElements());
	}

	/**
	 * User who has no rights (has role which has no permission) should not be able to see role no matter if it can be requested or not
	 */
	@Test
	public void testCannotReadNoPermission() {
		Page<IdmRoleDto> res1 = getRoleAsUser(hasNoRights, canBeRequested);
		assertEquals(0, res1.getTotalElements());
		//
		Page<IdmRoleDto> res2 = getRoleAsUser(hasNoRights, cannotBeRequested);
		assertEquals(0, res2.getTotalElements());
	}

	/**
	 * User with READ permission and tested evaluator should be able to see role that can be requested
	 */
	@Test
	public void testCanReadRoleCanBeRequested() {
		Page<IdmRoleDto> res1 = getRoleAsUser(hasRoleEvaluator, canBeRequested);
		assertEquals(1, res1.getTotalElements());
		assertEquals(res1.getContent().get(0).getId(), canBeRequested.getId());
		//
		Page<IdmRoleDto> res2 = getRoleAsUser(hasRoleEvaluator, cannotBeRequested);
		assertEquals(0, res2.getTotalElements());
	}

	/**
	 * User with UPDATE permission and tested evaluator should be able to see role that can be requested and also should be able to update it
	 */
	@Test
	public void testCanUpdate() {
		Page<IdmRoleDto> res1 = getRoleAsUser(hasEvaluatorUpdate, canBeRequested);
		assertEquals(1, res1.getTotalElements());
		IdmRoleDto found1 = res1.getContent().get(0);
		assertEquals(found1.getId(), canBeRequested.getId());
		//
		final String testDescription = uniqueString();
		saveRoleAsUser(hasEvaluatorUpdate, found1, testDescription);
		Page<IdmRoleDto> res2 = getRoleAsUser(hasEvaluatorUpdate, canBeRequested);
		IdmRoleDto found2 = res2.getContent().get(0);
		assertEquals(found2.getDescription(), testDescription);
		//
		Page<IdmRoleDto> res3 = getRoleAsUser(hasEvaluatorUpdate, cannotBeRequested);
		assertEquals(0, res3.getTotalElements());
	}

	/**
	 * User with ADMIN permission and tested evaluator should be able to see role that can be requested
	 * and also should be able to update it and delete it
	 */
	@Test
	public void testCanAdmin() {
		Page<IdmRoleDto> res1 = getRoleAsUser(hasEvaluatorAllRights, canBeRequested);
		assertEquals(1, res1.getTotalElements());
		IdmRoleDto found1 = res1.getContent().get(0);
		assertEquals(found1.getId(), canBeRequested.getId());
		//
		final String testDescription = uniqueString();
		Exception e = saveRoleAsUser(hasEvaluatorAllRights, found1, testDescription);
		assertNull(e);
		Page<IdmRoleDto> res2 = getRoleAsUser(hasEvaluatorAllRights, canBeRequested);
		IdmRoleDto found2 = res2.getContent().get(0);
		assertEquals(found2.getDescription(), testDescription);
		//
		Exception e2 = deleteRoleAsUser(hasEvaluatorAllRights, found2);
		assertNull(e2);
		Page<IdmRoleDto> res3 = getRoleAsUser(hasEvaluatorAllRights, canBeRequested);
		assertEquals(0, res3.getTotalElements());
		//
		Page<IdmRoleDto> res4 = getRoleAsUser(hasEvaluatorAllRights, cannotBeRequested);
		assertEquals(0, res4.getTotalElements());
		//
		cannotBeRequested.setDescription(testDescription);
		Exception e3 = saveRoleAsUser(hasEvaluatorAllRights, cannotBeRequested, testDescription);
		assertNotNull(e3);
		IdmRoleDto found3 = roleService.get(cannotBeRequested.getId());
		assertFalse(testDescription.equals(found3.getDescription()));
		//
		Exception e4 = deleteRoleAsUser(hasEvaluatorAllRights, cannotBeRequested);
		assertNotNull(e4);
		IdmRoleDto res6 = roleService.get(cannotBeRequested.getId());
		assertNotNull(res6);
	}

	private Exception saveRoleAsUser(IdmIdentityDto user, IdmRoleDto found1, String testDescription) {
		try {
			loginService.login(new LoginDto(user.getUsername(), new GuardedString(TEST_PWD)));
			IdmRoleDto fnd = roleService.get(found1.getId());
			fnd.setDescription(testDescription);
			roleService.save(fnd, IdmBasePermission.UPDATE);
		} catch (Exception ex) {
			return ex;
		} finally {
			logout();
		}
		return null;
	}

	private Exception deleteRoleAsUser(IdmIdentityDto user, IdmRoleDto role) {
		try {
			loginService.login(new LoginDto(user.getUsername(), new GuardedString(TEST_PWD)));
			roleService.delete(role, IdmBasePermission.DELETE);
		} catch (Exception ex) {
			return ex;
		} finally {
			logout();
		}
		return null;
	}


	private Page<IdmRoleDto> getRoleAsUser(IdmIdentityDto user, IdmRoleDto role) {
		try {
			loginService.login(new LoginDto(user.getUsername(), new GuardedString(TEST_PWD)));
			//
			IdmRoleFilter rf = getRoleFilter("name", role.getName());
			Page<IdmRoleDto> readRole = roleService.find(rf, null, IdmBasePermission.READ);
			return readRole;
		} finally {
			logout();
		}
	}

	private IdmRoleFilter getRoleFilter(String prop, String val) {
		IdmRoleFilter rf = new IdmRoleFilter();
		rf.setProperty(prop);
		rf.setValue(val);
		return rf;
	}

	private IdmRoleDto createRole(String name, boolean canBeRequested, IdmBasePermission... permissions) {
		IdmRoleDto roleWithNoRights = new IdmRoleDto();
		roleWithNoRights.setCanBeRequested(canBeRequested);
		roleWithNoRights.setName(name);
		final IdmRoleDto result = this.roleService.save(roleWithNoRights);
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

	private IdmIdentityDto createUser(String name, String password, IdmRoleDto... roles) {
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

	private void assignRoles(IdmIdentityContractDto contract, IdmRoleDto... roles) {
		for (IdmRoleDto role : roles) {
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
