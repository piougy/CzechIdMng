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
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic role catalogue service operations
 * 
 * @author Ondřej Kopr
 * @author Radek Tomiška
 *
 */
public class DefaultIdmRoleCatalogueServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdmRoleService roleService;
	@Autowired private IdmRoleCatalogueRepository roleCatalogueRepository;
	@Autowired private IdmRoleCatalogueService roleCatalogueService;
	
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
		IdmRoleCatalogueDto roleCatalogue = new IdmRoleCatalogueDto();
		String catalogueName = "cat_one_" + System.currentTimeMillis();
		roleCatalogue.setCode(catalogueName);
		roleCatalogue.setName(catalogueName);
		roleCatalogue = roleCatalogueService.save(roleCatalogue);
		// role
		IdmRole role = new IdmRole();
		String roleName = "test_r_" + System.currentTimeMillis();
		role.setName(roleName);		
		//
		IdmRoleCatalogueRole roleCatalogueRole = new IdmRoleCatalogueRole();
		roleCatalogueRole.setRole(role);
		roleCatalogueRole.setRoleCatalogue(roleCatalogueRepository.findOne(roleCatalogue.getId()));
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
		List<IdmRoleCatalogueDto> roleCatalogues = roleCatalogueService.findAllByRole(role.getId());
		assertEquals(0, roleCatalogues.size());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testDuplicitNamesRoots() {
		IdmRoleCatalogueDto roleCatalogue = new IdmRoleCatalogueDto();
		String name = "cat_one_" + System.currentTimeMillis();
		roleCatalogue.setCode(name);
		roleCatalogue.setName("test");
		//
		this.roleCatalogueService.save(roleCatalogue);
		//
		// create second
		IdmRoleCatalogueDto roleCatalogue2 = new IdmRoleCatalogueDto();
		name = "cat_one_" + System.currentTimeMillis();
		roleCatalogue2.setCode(name);
		roleCatalogue2.setName("test");
		// throws error
		this.roleCatalogueService.save(roleCatalogue2);
	}
	
	@Test(expected = ResultCodeException.class)
	public void testDuplicitNamesChilds() {
		IdmRoleCatalogueDto root = new IdmRoleCatalogueDto();
		String code = "cat_one_" + System.currentTimeMillis();
		root.setCode(code);
		root.setName("test" + System.currentTimeMillis());
		root = this.roleCatalogueService.save(root);
		//
		IdmRoleCatalogueDto roleCatalogue = new IdmRoleCatalogueDto();
		code = "cat_one_" + System.currentTimeMillis();
		roleCatalogue.setCode(code);
		roleCatalogue.setParent(root.getId());
		roleCatalogue.setName("test");
		//
		roleCatalogue = this.roleCatalogueService.save(roleCatalogue);
		//
		// create second
		IdmRoleCatalogueDto roleCatalogue2 = new IdmRoleCatalogueDto();
		code = "cat_one_" + System.currentTimeMillis();
		roleCatalogue2.setCode(code);
		roleCatalogue2.setParent(root.getId());
		roleCatalogue2.setName("test");
		// throws error
		this.roleCatalogueService.save(roleCatalogue2);
	}
	
	@Test
	public void testNameDiffLevel() {
		// code must be unique for all nodes,
		// name must be unique for all children of parent.
		IdmRoleCatalogueDto root = new IdmRoleCatalogueDto();
		String code = "cat_one_" + System.currentTimeMillis();
		root.setName("test_01");
		root.setCode(code);
		root = this.roleCatalogueService.save(root);
		//
		IdmRoleCatalogueDto child1 = new IdmRoleCatalogueDto();
		code = "cat_one_" + System.currentTimeMillis();
		child1.setName("test_02");
		child1.setParent(root.getId());
		child1.setCode(code);
		child1 = this.roleCatalogueService.save(child1);
		//
		IdmRoleCatalogueDto child2 = new IdmRoleCatalogueDto();
		code = "cat_one_" + System.currentTimeMillis();
		child2.setName("test_02");
		child2.setParent(child1.getId());
		child2.setCode(code);
		child2 = this.roleCatalogueService.save(child2);
		/* excepted
		 * - root
		 * 		- child1
		 * 				- child2
		 */
		assertEquals(1, this.roleCatalogueService.findChildrenByParent(root.getId(), null).getTotalElements());
		assertEquals(1, this.roleCatalogueService.findChildrenByParent(child1.getId(), null).getTotalElements());
		assertEquals(0, this.roleCatalogueService.findChildrenByParent(child2.getId(), null).getTotalElements());
	}
}
