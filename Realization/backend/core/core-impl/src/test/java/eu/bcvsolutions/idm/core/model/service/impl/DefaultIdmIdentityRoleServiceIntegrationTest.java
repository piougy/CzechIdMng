package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Assigned roles integration tests
 * - referential integrity
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultIdmIdentityRoleServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private ApplicationContext context;
	//
	private DefaultIdmIdentityRoleService service;

	@Before
	public void init() {
		service = context.getAutowireCapableBeanFactory().createBean(DefaultIdmIdentityRoleService.class);
	}
	
	@Test
	public void testReferentialIntegrityDirectRole() {
		IdmIdentityContractDto contract = getHelper().getPrimeContract(getHelper().createIdentity().getId());
		
		IdmRoleDto directRoleOne = getHelper().createRole(); 
		IdmRoleDto subRoleOne = getHelper().createRole();
		IdmRoleDto directRoleTwo = getHelper().createRole(); 
		//
		IdmIdentityRoleDto directIdentityRoleOne = new IdmIdentityRoleDto();
		directIdentityRoleOne.setIdentityContract(contract.getId());
		directIdentityRoleOne.setRole(directRoleOne.getId());
		directIdentityRoleOne = service.save(directIdentityRoleOne);
		//
		IdmIdentityRoleDto subIdentityRoleOne = new IdmIdentityRoleDto();
		subIdentityRoleOne.setIdentityContract(contract.getId());
		subIdentityRoleOne.setRole(subRoleOne.getId());
		subIdentityRoleOne.setDirectRole(directIdentityRoleOne.getId());
		subIdentityRoleOne = service.save(subIdentityRoleOne);
		//
		IdmIdentityRoleDto otherIdentityRoleOne = new IdmIdentityRoleDto();
		otherIdentityRoleOne.setIdentityContract(contract.getId());
		otherIdentityRoleOne.setRole(directRoleTwo.getId());
		otherIdentityRoleOne = service.save(otherIdentityRoleOne);
		//
		// check after create
		List<IdmIdentityRoleDto> assignedRoles = service.findAllByContract(contract.getId());
		Assert.assertEquals(3, assignedRoles.size());
		//
		// delete direct role
		service.delete(directIdentityRoleOne);
		assignedRoles = service.findAllByContract(contract.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(directRoleTwo.getId())));
	}
}
