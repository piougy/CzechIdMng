package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmRoleControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmRoleDto> {

	@Autowired private IdmRoleController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmRoleDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmRoleDto prepareDto() {
		IdmRoleDto dto = new IdmRoleDto();
		dto.setCode(getHelper().createName());
		dto.setName(dto.getCode());
		return dto;
	}
	
	@Test
	public void testFindByRoleCatalogueRecursivelly() {
		// prepare role catalogue
		try {
			getHelper().loginAdmin();
			//
			IdmRoleCatalogueDto roleCatalogue = getHelper().createRoleCatalogue();
			IdmRoleCatalogueDto roleCatalogueOne = getHelper().createRoleCatalogue(null, roleCatalogue.getId());
			IdmRoleCatalogueDto roleCatalogueTwo = getHelper().createRoleCatalogue(null, roleCatalogue.getId());
			IdmRoleCatalogueDto roleCatalogueOneSub = getHelper().createRoleCatalogue(null, roleCatalogueOne.getId());
			IdmRoleCatalogueDto roleCatalogueOneSubSub = getHelper().createRoleCatalogue(null, roleCatalogueOneSub.getId());
			IdmRoleCatalogueDto roleCatalogueOther = getHelper().createRoleCatalogue();
			// create roles
			IdmRoleDto roleOne = getHelper().createRole();
			IdmRoleDto roleTwo = getHelper().createRole();
			IdmRoleDto roleThree = getHelper().createRole();
			// assign role into catalogue
			getHelper().createRoleCatalogueRole(roleOne, roleCatalogueOne);
			getHelper().createRoleCatalogueRole(roleTwo, roleCatalogueTwo);
			getHelper().createRoleCatalogueRole(roleThree, roleCatalogueOneSubSub);
			//
			// test
			IdmRoleFilter filter = new IdmRoleFilter();
			filter.setRoleCatalogueId(roleCatalogueOne.getId());
			List<IdmRoleDto> roles = find(filter);
			Assert.assertEquals(2, roles.size());
			Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleOne.getId())));
			Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleThree.getId())));
			//
			filter.setRoleCatalogueId(roleCatalogueOther.getId());
			roles = find(filter);
			Assert.assertTrue(roles.isEmpty());
			//
			filter.setRoleCatalogueId(roleCatalogueTwo.getId());
			roles = find(filter);
			Assert.assertEquals(1, roles.size());
			Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleTwo.getId())));
			//
			filter.setRoleCatalogueId(roleCatalogue.getId());
			roles = find(filter);
			Assert.assertEquals(3, roles.size());
			Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleOne.getId())));
			Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleTwo.getId())));
			Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleThree.getId())));
		} finally {
			logout();
		}
	}
}
