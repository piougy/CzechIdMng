package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Test for Identity Roles filters
 *
 * @author Marek Klement
 *
 */
public class DefaultIdentityRoleServiceTest extends AbstractIntegrationTest{
	@Autowired
	IdmIdentityRoleService idmIdentityRoleService;
	@Autowired
	TestHelper testHelper;
	@Autowired
	IdmRoleCatalogueRoleService idmRoleCatalogueRoleService;

	@Before
	public void logIn(){
		loginAsAdmin();
	}

	@After
	public void logOut(){
		super.logout();
	}

	@Test
	public void identityIdTest(){
		IdmRoleDto role = testHelper.createRole();
		IdmIdentityDto person = testHelper.createIdentity();
		IdmIdentityRoleDto addRole = testHelper.createIdentityRole(person,role);
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(person.getId());
		Page<IdmIdentityRoleDto> result = idmIdentityRoleService.find(filter, null);
		assertEquals("Wrong RoleId",addRole.getId(), result.getContent().get(0).getId());
	}

	@Test
	public void roleCatalogueTest(){
		IdmIdentityDto person = testHelper.createIdentity();
		IdmIdentityDto person2 = testHelper.createIdentity();
		IdmRoleCatalogueDto catalogue = testHelper.createRoleCatalogue();
		IdmRoleCatalogueRoleDto catalogueRole = new IdmRoleCatalogueRoleDto();
		IdmRoleCatalogueRoleDto catalogueRole2 = new IdmRoleCatalogueRoleDto();
		IdmIdentityContractDto contract = testHelper.createIdentityContact(person);
		IdmIdentityContractDto contract2 = testHelper.createIdentityContact(person2);
		IdmRoleDto role = testHelper.createRole();
		IdmRoleDto role2 = testHelper.createRole();
		catalogueRole.setRoleCatalogue(catalogue.getId());
		catalogueRole.setRole(role.getId());
		catalogueRole2.setRoleCatalogue(catalogue.getId());
		catalogueRole2.setRole(role2.getId());
		IdmIdentityRoleDto roleCover = testHelper.createIdentityRole(contract,role);
		IdmIdentityRoleDto roleCover2 = testHelper.createIdentityRole(contract2,role2);
		idmRoleCatalogueRoleService.save(catalogueRole);
		idmRoleCatalogueRoleService.save(catalogueRole2);
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setRoleCatalogueId(catalogue.getId());
		Page<IdmIdentityRoleDto> result = idmIdentityRoleService.find(filter, null);
		// behavior with result.getContent().get(0) is not good for use, order comes from DB!
		result.getContent().stream().forEach(cat -> {
			if (cat.getId().equals(roleCover.getId()) || cat.getId().equals(roleCover2.getId())) {
				// success
			} else {
				fail("Wrong id founded: " + cat.getId());
			}
		});
	}
}
