package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleGuaranteeRepository;
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
	@Transactional
	public void testReferentialIntegrity() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString("heslo"));
		// role
		IdmRoleDto role = getHelper().createRole();
		IdmRoleGuaranteeDto roleGuarantee = new IdmRoleGuaranteeDto();
		roleGuarantee.setRole(role.getId());
		roleGuarantee.setGuarantee(identity.getId());
		role.setGuarantees(Lists.newArrayList(roleGuarantee));
		role = roleService.save(role);
		
		assertNotNull(roleService.getByCode(role.getCode()));
		assertEquals(1, roleGuaranteeRepository.findAllByRole_Id(role.getId()).size());
		
		roleService.delete(role);
		
		assertNull(roleService.getByCode(role.getCode()));
		assertEquals(0, roleGuaranteeRepository.findAllByRole_Id(role.getId()).size());
	}
	
	@Transactional
	@Test(expected = ResultCodeException.class)
	public void testReferentialIntegrityAssignedRoles() {
		// prepare data
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		// assigned role
		getHelper().createIdentityRole(identity, role);
		//
		roleService.delete(role);
	}
	
	@Test
	@Transactional
	public void testReferentialIntegrityAuthorizationPolicies() {
		// prepare data
		IdmRoleDto role = getHelper().createRole();
		// policy
		getHelper().createBasePolicy(role.getId(), IdmBasePermission.ADMIN);
		//
		roleService.delete(role);
		//
		IdmAuthorizationPolicyFilter policyFilter = new IdmAuthorizationPolicyFilter();
		policyFilter.setRoleId(role.getId());
		assertEquals(0, authorizationPolicyService.find(policyFilter, null).getTotalElements());
	}

	@Test
	@Transactional
	public void textFilterTest(){
		getHelper().createRole("SomeName001");
		getHelper().createRole("SomeName002");
		getHelper().createRole("SomeName003");
		getHelper().createRole("SomeName104");

		IdmRoleDto role5 = new IdmRoleDto();
		role5.setDescription("SomeName005");
		role5.setName("SomeName105");
		role5 = roleService.save(role5);

		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setText("SomeName00");
		Page<IdmRoleDto> result = roleService.find(filter,null);
		assertEquals("Wrong text filter", 4, result.getTotalElements());
		assertEquals("Wrong text filter description", true, result.getContent().contains(role5));
	}

	@Test
	@Transactional
	public void typeFilterTest(){
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto role2 = getHelper().createRole();
		IdmRoleDto role3 = getHelper().createRole();

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

		IdmRoleFilter filter = new IdmRoleFilter();
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
	@Transactional
	public void guaranteeFilterTest(){
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		IdmRoleGuaranteeDto roleGuarantee = new IdmRoleGuaranteeDto();
		roleGuarantee.setRole(role.getId());
		roleGuarantee.setGuarantee(identity.getId());
		role.setGuarantees(Lists.newArrayList(roleGuarantee));
		role = roleService.save(role);

		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setGuaranteeId(identity.getId());
		Page<IdmRoleDto> result = roleService.find(filter, null);
		assertEquals("Wrong guarantee", 1, result.getTotalElements());
		assertEquals("Wrong guarantee id", role.getId(), result.getContent().get(0).getId());
	}

	@Test
	@Transactional
	public void catalogueFilterTest(){
		IdmRoleDto role = getHelper().createRole();

		IdmRoleCatalogueDto catalogue = getHelper().createRoleCatalogue();
		IdmRoleCatalogueRoleDto catalogueRole = new IdmRoleCatalogueRoleDto();
		catalogueRole.setRole(role.getId());
		catalogueRole.setRoleCatalogue(catalogue.getId());
		catalogueRole = idmRoleCatalogueRoleService.save(catalogueRole);

		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setRoleCatalogueId(catalogue.getId());
		Page<IdmRoleDto> result = roleService.find(filter,null);
		assertEquals("Wrong catalogue", 1, result.getTotalElements());
		assertTrue("Wrong catalogue id #1", result.getContent().contains(role));
	}
	
	/**
	 * Test find role by all string fields
	 */
	@Test
	@Transactional
	public void testCorrelableFilter() {
		IdmRoleDto role = getHelper().createRole();
		role.setExternalId(getHelper().createName());
		role.setName(getHelper().createName());
		role.setDescription(getHelper().createName());
		IdmRoleDto roleFull = roleService.save(role);

		ArrayList<Field> fields = Lists.newArrayList(IdmRole_.class.getFields());
		IdmRoleFilter filter = new IdmRoleFilter();

		fields.forEach(field -> {
			filter.setProperty(field.getName());

			try {
				Object value = EntityUtils.getEntityValue(roleFull, field.getName());
				if (value == null || !(value instanceof String)) {
					return;
				}
				filter.setValue(value.toString());
				List<IdmRoleDto> identities = roleService.find(filter, null).getContent();
				assertTrue(identities.contains(roleFull));

			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| IntrospectionException e) {
				e.printStackTrace();
			}

		});

	}
}
