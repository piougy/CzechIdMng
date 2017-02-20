package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic role service operations
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmRoleCatalogueServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmRoleCatalogueService roleCatalogueService;
	@Autowired
	private IdmRoleCatalogueRoleService roleCatalogueRoleService;
	
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
		// catalogue
		IdmRoleCatalogue roleCatalogue = new IdmRoleCatalogue();
		String catalogueName = "cat_one_" + System.currentTimeMillis();
		roleCatalogue.setName(catalogueName);
		roleCatalogue = roleCatalogueService.save(roleCatalogue);
		// role
		IdmRole role = new IdmRole();
		String roleName = "test_r_" + System.currentTimeMillis();
		role.setName(roleName);		

		IdmRoleCatalogueRole roleCatalogueRole = new IdmRoleCatalogueRole();
		roleCatalogueRole.setRole(role);
		roleCatalogueRole.setRoleCatalogue(roleCatalogue);
		//
		role.setRoleCatalogues(Lists.newArrayList(roleCatalogueRole));
		role = roleService.save(role);
		//
		List<IdmRoleCatalogueRole> list = role.getRoleCatalogues();
		assertEquals(1, list.size());
		IdmRoleCatalogue catalog = list.get(0).getRoleCatalogue();
		IdmRole roleFromCatalogue = list.get(0).getRole();
		//
		assertNotNull(catalog);
		assertNotNull(roleFromCatalogue);
		assertEquals(catalogueName, catalog.getName());
		assertEquals(roleName, roleFromCatalogue.getName());
		//
		roleCatalogueService.delete(roleCatalogue);
		//
		List<IdmRoleCatalogue> roleCatalogues = roleCatalogueRoleService.getRoleCatalogueByRole(role);
		assertEquals(0, roleCatalogues.size());
	}
}
