package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueFilter;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Basic role catalogue service operations
 * 
 * @author Ondřej Kopr
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultIdmRoleCatalogueServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdmRoleCatalogueService roleCatalogueService;
	@Autowired private IdmRoleCatalogueRoleService roleCatalogueRoleService;
	@Autowired private TestHelper helper;
	
	@Before
	public void init() {
		loginAsAdmin();
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
		IdmRoleDto role = getHelper().createRole();
		//
		IdmRoleCatalogueRoleDto roleCatalogueRole = new IdmRoleCatalogueRoleDto();
		roleCatalogueRole.setRole(role.getId());
		roleCatalogueRole.setRoleCatalogue(roleCatalogue.getId());
		roleCatalogueRoleService.save(roleCatalogueRole);
		//
		List<IdmRoleCatalogueRoleDto> list = roleCatalogueRoleService.findAllByRoleCatalogue(roleCatalogue.getId());
		assertEquals(1, list.size());
		UUID catalogId = list.get(0).getRoleCatalogue();
		UUID roleId = list.get(0).getRole();
		//
		assertNotNull(catalogId);
		assertNotNull(roleId);
		assertEquals(roleCatalogue.getId(), catalogId);
		assertEquals(role.getId(), roleId);
		//
		roleCatalogueService.delete(roleCatalogue);
		//
		Assert.assertEquals(0, roleCatalogueRoleService.findAllByRoleCatalogue(roleCatalogue.getId()).size());
		List<IdmRoleCatalogueDto> roleCatalogues = roleCatalogueService.findAllByRole(role.getId());
		assertEquals(0, roleCatalogues.size());
	}

// VS: I turned off this validation ... because in implementation is same name (different code) normal situation 
//	@Test(expected = ResultCodeException.class)
//	public void testDuplicitNamesRoots() {
//		IdmRoleCatalogueDto roleCatalogue = new IdmRoleCatalogueDto();
//		String name = "cat_one_" + System.currentTimeMillis();
//		roleCatalogue.setCode(name);
//		roleCatalogue.setName("test");
//		//
//		this.roleCatalogueService.save(roleCatalogue);
//		//
//		// create second
//		IdmRoleCatalogueDto roleCatalogue2 = new IdmRoleCatalogueDto();
//		name = "cat_one_" + System.currentTimeMillis();
//		roleCatalogue2.setCode(name);
//		roleCatalogue2.setName("test");
//		// throws error
//		this.roleCatalogueService.save(roleCatalogue2);
//	}
//	
//	@Test(expected = ResultCodeException.class)
//	public void testDuplicitNamesChilds() {
//		IdmRoleCatalogueDto root = new IdmRoleCatalogueDto();
//		String code = "cat_one_" + System.currentTimeMillis();
//		root.setCode(code);
//		root.setName("test" + System.currentTimeMillis());
//		root = this.roleCatalogueService.save(root);
//		//
//		IdmRoleCatalogueDto roleCatalogue = new IdmRoleCatalogueDto();
//		code = "cat_one_" + System.currentTimeMillis();
//		roleCatalogue.setCode(code);
//		roleCatalogue.setParent(root.getId());
//		roleCatalogue.setName("test");
//		//
//		roleCatalogue = this.roleCatalogueService.save(roleCatalogue);
//		//
//		// create second
//		IdmRoleCatalogueDto roleCatalogue2 = new IdmRoleCatalogueDto();
//		code = "cat_one_" + System.currentTimeMillis();
//		roleCatalogue2.setCode(code);
//		roleCatalogue2.setParent(root.getId());
//		roleCatalogue2.setName("test");
//		// throws error
//		this.roleCatalogueService.save(roleCatalogue2);
//	}
	
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

	@Test
	public void textFilterTest(){
		IdmRoleCatalogueDto catalogue = helper.createRoleCatalogue();
		catalogue.setCode("NameCat001");
		roleCatalogueService.save(catalogue);

		IdmRoleCatalogueDto catalogue2 = helper.createRoleCatalogue();
		catalogue2.setCode("NameCat002");
		roleCatalogueService.save(catalogue2);

		IdmRoleCatalogueDto catalogue3 = helper.createRoleCatalogue();
		catalogue3.setCode("NameCat103");
		roleCatalogueService.save(catalogue3);

		IdmRoleCatalogueDto catalogue4 = helper.createRoleCatalogue();
		catalogue4.setName("NameCat004");
		roleCatalogueService.save(catalogue4);

		IdmRoleCatalogueFilter filter = new IdmRoleCatalogueFilter();
		filter.setText("NameCat00");
		Page<IdmRoleCatalogueDto> result = roleCatalogueService.find(filter,null);
		assertEquals("Wrong text filter count", 3,result.getTotalElements());
		
		// Behavior with result.getContent().get(0) is bad
		result.getContent().forEach(cat -> {
			if (cat.getId().equals(catalogue.getId()) || cat.getId().equals(catalogue2.getId())
					|| cat.getId().equals(catalogue4.getId())) {
				// Catalog exists correct
			} else {
				fail("Wrong text by Id " + cat.getId());
			}
		});
	}

	@Test
	public void codeFilterTest(){
		IdmRoleCatalogueDto catalogue = helper.createRoleCatalogue();
		IdmRoleCatalogueDto catalogue2 = helper.createRoleCatalogue();
		IdmRoleCatalogueDto catalogue3 = helper.createRoleCatalogue();

		IdmRoleCatalogueFilter filter = new IdmRoleCatalogueFilter();
		filter.setCode(catalogue.getCode());
		Page<IdmRoleCatalogueDto> result = roleCatalogueService.find(filter,null);
		assertEquals("Wrong code count", 1,result.getTotalElements());
		assertEquals("Wrong code",catalogue.getId(),result.getContent().get(0).getId());

		filter.setCode(catalogue2.getCode());
		result = roleCatalogueService.find(filter,null);
		assertEquals("Wrong code 2 count", 1,result.getTotalElements());
		assertEquals("Wrong code 2",catalogue2.getId(),result.getContent().get(0).getId());

		filter.setCode(catalogue3.getCode());
		result = roleCatalogueService.find(filter,null);
		assertEquals("Wrong code 3 count", 1,result.getTotalElements());
		assertEquals("Wrong code 3",catalogue3.getId(),result.getContent().get(0).getId());
	}

	@Test
	public void nameFilterTest(){
		IdmRoleCatalogueDto catalogue = helper.createRoleCatalogue();
		IdmRoleCatalogueDto catalogue2 = helper.createRoleCatalogue();
		IdmRoleCatalogueDto catalogue3 = helper.createRoleCatalogue();

		IdmRoleCatalogueFilter filter = new IdmRoleCatalogueFilter();
		filter.setName(catalogue.getName());
		Page<IdmRoleCatalogueDto> result = roleCatalogueService.find(filter,null);
		assertEquals("Wrong name count", 1,result.getTotalElements());
		assertEquals("Wrong name",catalogue.getId(),result.getContent().get(0).getId());

		filter.setName(catalogue2.getName());
		result = roleCatalogueService.find(filter,null);
		assertEquals("Wrong name 2 count", 1,result.getTotalElements());
		assertEquals("Wrong name",catalogue2.getId(),result.getContent().get(0).getId());

		filter.setName(catalogue3.getName());
		result = roleCatalogueService.find(filter,null);
		assertEquals("Wrong name 3 count", 1,result.getTotalElements());
		assertEquals("Wrong name",catalogue3.getId(),result.getContent().get(0).getId());
	}

	@Test
	public void parentFilterTest(){
		IdmRoleCatalogueDto catalogue = helper.createRoleCatalogue();
		IdmRoleCatalogueDto catalogue2 = helper.createRoleCatalogue();
		IdmRoleCatalogueDto catalogue3 = helper.createRoleCatalogue();
		IdmRoleCatalogueDto catalogue4 = helper.createRoleCatalogue();
		IdmRoleCatalogueDto catalogue5 = helper.createRoleCatalogue();
		UUID catalogueId = catalogue.getId();

		catalogue2.setParent(catalogueId);
		roleCatalogueService.save(catalogue2);
		catalogue3.setParent(catalogueId);
		roleCatalogueService.save(catalogue3);
		catalogue5.setParent(catalogue4.getId());
		roleCatalogueService.save(catalogue5);

		IdmRoleCatalogueFilter filter = new IdmRoleCatalogueFilter();
		filter.setParent(catalogueId);
		Page<IdmRoleCatalogueDto> result = roleCatalogueService.find(filter,null);
		assertEquals("Wrong parent count", 2,result.getTotalElements());
		assertEquals("Wrong parent contain",true,result.getContent().contains(catalogue3));

		filter.setParent(catalogue4.getId());
		result = roleCatalogueService.find(filter,null);
		assertEquals("Wrong parent count #2", 1,result.getTotalElements());
		assertEquals("Wrong parent Id",catalogue5.getId(),result.getContent().get(0).getId());

		filter.setParent(catalogue5.getId());
		result = roleCatalogueService.find(filter,null);
		assertEquals("Wrong parent count blank", 0,result.getTotalElements());
	}
}
