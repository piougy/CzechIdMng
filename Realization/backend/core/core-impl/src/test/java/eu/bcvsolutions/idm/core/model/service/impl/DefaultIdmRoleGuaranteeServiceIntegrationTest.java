package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic role quarantee service test
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class DefaultIdmRoleGuaranteeServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmRoleGuaranteeService roleGuaranteeService;
	@Autowired private IdmRoleGuaranteeRoleService roleGuaranteeRoleService;
	
	@Test
	public void testFindRoleGuaranteeByRole() {
		IdmIdentityDto guarantee1 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto guarantee2 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto guarantee3 = getHelper().createIdentity((GuardedString) null);
		//
		IdmRoleDto role = getHelper().createRole();
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
		Assert.assertEquals(3, list.size());
		//
		List<UUID> guarantees = list.stream().map(IdmRoleGuaranteeDto::getGuarantee).collect(Collectors.toList());
		roleGuarantee = list.get(0);
		Assert.assertEquals(role.getId(), roleGuarantee.getRole());
		Assert.assertTrue(guarantees.contains(guarantee3.getId()));
		Assert.assertTrue(guarantees.contains(guarantee2.getId()));
		Assert.assertTrue(guarantees.contains(guarantee1.getId()));
	}
	
	@Test
	public void testFindRoleGuaranteeByGuarantee() {
		IdmIdentityDto guarantee = getHelper().createIdentity((GuardedString) null);
		//
		IdmRoleDto role1 = getHelper().createRole();
		IdmRoleDto role2 = getHelper().createRole();
		IdmRoleDto role3 = getHelper().createRole();
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
		Assert.assertEquals(3, list.size());
		//
		List<UUID> roles = list.stream().map(IdmRoleGuaranteeDto::getRole).collect(Collectors.toList());
		roleGuarantee = list.get(0);
		Assert.assertEquals(guarantee.getId(), roleGuarantee.getGuarantee());
		Assert.assertTrue(roles.contains(role1.getId()));
		Assert.assertTrue(roles.contains(role2.getId()));
		Assert.assertTrue(roles.contains(role3.getId()));
	}
	
	@Test
	public void testFindRoleGuaranteeByRoleGuarantee() {
		IdmRoleDto guaranteeRole = getHelper().createRole();
		IdmRoleDto guaranteeRoleTwo = getHelper().createRole();
		//
		IdmRoleDto role1 = getHelper().createRole();
		IdmRoleDto role2 = getHelper().createRole();
		IdmRoleDto role3 = getHelper().createRole();
		//
		IdmRoleGuaranteeRoleDto roleGuaranteeOne = new IdmRoleGuaranteeRoleDto();
		roleGuaranteeOne.setRole(role1.getId());
		roleGuaranteeOne.setGuaranteeRole(guaranteeRole.getId());
		roleGuaranteeOne = roleGuaranteeRoleService.save(roleGuaranteeOne);
		//
		IdmRoleGuaranteeRoleDto roleGuaranteeTwo = new IdmRoleGuaranteeRoleDto();
		roleGuaranteeTwo.setRole(role2.getId());
		roleGuaranteeTwo.setGuaranteeRole(guaranteeRoleTwo.getId());
		roleGuaranteeTwo = roleGuaranteeRoleService.save(roleGuaranteeTwo);
		//
		IdmRoleGuaranteeRoleDto roleGuaranteeThree = new IdmRoleGuaranteeRoleDto();
		roleGuaranteeThree.setRole(role3.getId());
		roleGuaranteeThree.setGuaranteeRole(guaranteeRole.getId());
		roleGuaranteeThree = roleGuaranteeRoleService.save(roleGuaranteeThree);
		//
		IdmRoleGuaranteeRoleFilter filter = new IdmRoleGuaranteeRoleFilter();
		filter.setGuaranteeRole(guaranteeRole.getId());
		List<IdmRoleGuaranteeRoleDto> list = roleGuaranteeRoleService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());
		//
		List<UUID> roles = list.stream().map(IdmRoleGuaranteeRoleDto::getRole).collect(Collectors.toList());
		IdmRoleGuaranteeRoleDto roleGuaranteeFirst = list.get(0);
		Assert.assertEquals(guaranteeRole.getId(), roleGuaranteeFirst.getGuaranteeRole());
		Assert.assertTrue(roles.contains(role1.getId()));
		Assert.assertFalse(roles.contains(role2.getId()));
		Assert.assertTrue(roles.contains(role3.getId()));
	}
}
