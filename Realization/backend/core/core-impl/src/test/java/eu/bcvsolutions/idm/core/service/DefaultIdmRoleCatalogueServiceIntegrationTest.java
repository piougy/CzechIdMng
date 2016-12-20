package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
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
		role.setRoleCatalogue(roleCatalogue);
		roleService.save(role);
		
		assertEquals(catalogueName, roleService.getByName(roleName).getRoleCatalogue().getName());
		
		roleCatalogueService.delete(roleCatalogue);
		
		assertNull(roleService.getByName(roleName).getRoleCatalogue());
	}
}
