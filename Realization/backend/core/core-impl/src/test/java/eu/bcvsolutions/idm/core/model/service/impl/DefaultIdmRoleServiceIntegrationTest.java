package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.TestHelper;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.AuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleGuaranteeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic role service operations
 * 
 * @author Radek Tomiška
 * @author Marek Klement
 *
 */
public class DefaultIdmRoleServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmRoleCatalogueRoleService idmRoleCatalogueRoleService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmRoleGuaranteeRepository roleGuaranteeRepository;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_USER_1);
	}
	
	@After 
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testReferentialIntegrity() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "delete_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity.setPassword(new GuardedString("heslo")); // confidential storage
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity = identityService.save(identity);
		// role
		IdmRoleDto role = new IdmRoleDto();
		String roleName = "test_r_" + System.currentTimeMillis();
		role.setName(roleName);
		IdmRoleGuaranteeDto roleGuarantee = new IdmRoleGuaranteeDto();
		roleGuarantee.setRole(role.getId());
		roleGuarantee.setGuarantee(identity.getId());
		role.setGuarantees(Lists.newArrayList(roleGuarantee));
		role = roleService.save(role);
		
		assertNotNull(roleService.getByCode(roleName));
		assertEquals(1, roleGuaranteeRepository.findAllByRole_Id(role.getId()).size());
		
		roleService.delete(role);
		
		assertNull(roleService.getByCode(roleName));
		assertEquals(0, roleGuaranteeRepository.findAllByRole_Id(role.getId()).size());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testReferentialIntegrityAssignedRoles() {
		// prepare data
		IdmIdentityDto identity = helper.createIdentity("delete-test");
		IdmRoleDto role = helper.createRole("test-delete");
		// assigned role
		helper.createIdentityRole(identity, role);
		//
		roleService.delete(role);
	}
	
	@Test
	public void testReferentialIntegrityAuthorizationPolicies() {
		// prepare data
		IdmRoleDto role = helper.createRole();
		// policy
		helper.createBasePolicy(role.getId(), IdmBasePermission.ADMIN);
		//
		roleService.delete(role);
		//
		AuthorizationPolicyFilter policyFilter = new AuthorizationPolicyFilter();
		policyFilter.setRoleId(role.getId());
		assertEquals(0, authorizationPolicyService.find(policyFilter, null).getTotalElements());
	}

	@Test
	public void textFilterTest(){
		helper.createRole("SomeName001");
		helper.createRole("SomeName002");
		helper.createRole("SomeName003");
		helper.createRole("SomeName104");

		IdmRoleDto role5 = new IdmRoleDto();
		role5.setDescription("SomeName005");
		role5.setName("SomeName105");
		role5 = roleService.save(role5);

		RoleFilter filter = new RoleFilter();
		filter.setText("SomeName00");
		Page<IdmRoleDto> result = roleService.find(filter,null);
		assertEquals("Wrong text filter",4,result.getTotalElements());
		assertEquals("Wrong text filter description",true,result.getContent().contains(role5));
	}

	@Test
	public void typeFilterTest(){
		IdmRoleDto role = helper.createRole();
		IdmRoleDto role2 = helper.createRole();
		IdmRoleDto role3 = helper.createRole();

		RoleType type = RoleType.LOGIN;
		RoleType type2 = RoleType.BUSINESS;

		role = roleService.get(role.getId());
		role.setRoleType(type);
		role = roleService.save(role);

		role2 = roleService.get(role2.getId());
		role2.setRoleType(type);
		role2 = roleService.save(role2);

		role3 = roleService.get(role3.getId());
		role3.setRoleType(type2);
		role3 = roleService.save(role3);

		RoleFilter filter = new RoleFilter();
		filter.setRoleType(type);
		Page<IdmRoleDto> result = roleService.find(filter,null);
		assertEquals("Wrong type #1", 2, result.getTotalElements());
		assertTrue("Wrong type #1 contains", result.getContent().contains(role));
		filter.setRoleType(type2);
		result = roleService.find(filter,null);
		assertEquals("Wrong type #2", 1, result.getTotalElements());
		assertTrue("Wrong type #2 contains", result.getContent().contains(role3));
	}

	@Test
	public void guaranteeFilterTest(){
		IdmIdentityDto identity = helper.createIdentity();

		IdmRoleDto role = new IdmRoleDto();
		role.setName("IgnacMikinaRole");
		helper.createRole();

		IdmRoleGuaranteeDto roleGuarantee = new IdmRoleGuaranteeDto();
		roleGuarantee.setRole(role.getId());
		roleGuarantee.setGuarantee(identity.getId());
		role.setGuarantees(Lists.newArrayList(roleGuarantee));
		role = roleService.save(role);

		RoleFilter filter = new RoleFilter();
		filter.setGuaranteeId(identity.getId());
		Page<IdmRoleDto> result = roleService.find(filter, null);
		assertEquals("Wrong guarantee", 1, result.getTotalElements());
		assertEquals("Wrong guarantee id", role.getId(), result.getContent().get(0).getId());
	}

	@Test
	public void catalogueFilterTest(){
		IdmRoleDto role = new IdmRoleDto();
		role.setName("PetrSadloRole");
		role = roleService.save(role);

		IdmRoleCatalogueDto catalogue = helper.createRoleCatalogue();
		IdmRoleCatalogueRoleDto catalogueRole = new IdmRoleCatalogueRoleDto();
		catalogueRole.setRole(role.getId());
		catalogueRole.setRoleCatalogue(catalogue.getId());
		catalogueRole = idmRoleCatalogueRoleService.save(catalogueRole);

		RoleFilter filter = new RoleFilter();
		filter.setRoleCatalogueId(catalogue.getId());
		Page<IdmRoleDto> result = roleService.find(filter,null);
		assertEquals("Wrong catalogue", 1, result.getTotalElements());
		assertTrue("Wrong catalogue id #1", result.getContent().contains(role));
	}
}
