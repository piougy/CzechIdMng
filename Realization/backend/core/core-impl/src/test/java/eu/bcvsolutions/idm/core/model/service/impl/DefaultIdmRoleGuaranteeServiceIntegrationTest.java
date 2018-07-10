package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Basic role quarantee service test
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class DefaultIdmRoleGuaranteeServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper testHelper;
	@Autowired private IdmRoleGuaranteeService roleGuaranteeService;
	
	@Before
	public void init() {
		loginAsAdmin();
	}
	
	@After 
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testFindRoleGuaranteeByRole() {
		IdmIdentityDto guarantee1 = testHelper.createIdentity();
		IdmIdentityDto guarantee2 = testHelper.createIdentity();
		IdmIdentityDto guarantee3 = testHelper.createIdentity();
		//
		IdmRoleDto role = testHelper.createRole();
		//
		IdmRoleGuaranteeDto roleGuarantee = new IdmRoleGuaranteeDto();
		roleGuarantee.setRole(role.getId());
		roleGuarantee.setGuarantee(guarantee1.getId());
		roleGuaranteeService.save(roleGuarantee);
		//
		roleGuarantee = new IdmRoleGuaranteeDto();
		roleGuarantee.setRole(role.getId());
		roleGuarantee.setGuarantee(guarantee2.getId());
		roleGuaranteeService.save(roleGuarantee);
		//
		roleGuarantee = new IdmRoleGuaranteeDto();
		roleGuarantee.setRole(role.getId());
		roleGuarantee.setGuarantee(guarantee3.getId());
		roleGuaranteeService.save(roleGuarantee);
		//
		IdmRoleGuaranteeFilter filter = new IdmRoleGuaranteeFilter();
		filter.setRole(role.getId());
		List<IdmRoleGuaranteeDto> list = roleGuaranteeService.find(filter, null).getContent();
		assertEquals(3, list.size());
		//
		List<UUID> guarantees = list.stream().map(IdmRoleGuaranteeDto::getGuarantee).collect(Collectors.toList());
		roleGuarantee = list.get(0);
		assertEquals(role.getId(), roleGuarantee.getRole());
		assertTrue(guarantees.contains(guarantee3.getId()));
		assertTrue(guarantees.contains(guarantee2.getId()));
		assertTrue(guarantees.contains(guarantee1.getId()));
	}
	
	@Test
	public void testFindRoleGuaranteeByGuarantee() {
		IdmIdentityDto guarantee = testHelper.createIdentity();
		//
		IdmRoleDto role1 = testHelper.createRole();
		IdmRoleDto role2 = testHelper.createRole();
		IdmRoleDto role3 = testHelper.createRole();
		//
		IdmRoleGuaranteeDto roleGuarantee = new IdmRoleGuaranteeDto();
		roleGuarantee.setRole(role1.getId());
		roleGuarantee.setGuarantee(guarantee.getId());
		roleGuaranteeService.save(roleGuarantee);
		//
		roleGuarantee = new IdmRoleGuaranteeDto();
		roleGuarantee.setRole(role2.getId());
		roleGuarantee.setGuarantee(guarantee.getId());
		roleGuaranteeService.save(roleGuarantee);
		//
		roleGuarantee = new IdmRoleGuaranteeDto();
		roleGuarantee.setRole(role3.getId());
		roleGuarantee.setGuarantee(guarantee.getId());
		roleGuaranteeService.save(roleGuarantee);
		//
		IdmRoleGuaranteeFilter filter = new IdmRoleGuaranteeFilter();
		filter.setGuarantee(guarantee.getId());
		List<IdmRoleGuaranteeDto> list = roleGuaranteeService.find(filter, null).getContent();
		assertEquals(3, list.size());
		//
		List<UUID> roles = list.stream().map(IdmRoleGuaranteeDto::getRole).collect(Collectors.toList());
		roleGuarantee = list.get(0);
		assertEquals(guarantee.getId(), roleGuarantee.getGuarantee());
		assertTrue(roles.contains(role1.getId()));
		assertTrue(roles.contains(role2.getId()));
		assertTrue(roles.contains(role3.getId()));
	}
	
}
