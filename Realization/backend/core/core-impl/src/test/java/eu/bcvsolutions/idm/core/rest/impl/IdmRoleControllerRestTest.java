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
	}
	
	@Test
	public void testFindByEnvironment() {
		IdmRoleDto roleOne = prepareDto();
		roleOne.setCode(null);
		roleOne.setBaseCode(getHelper().createName());
		roleOne.setEnvironment(getHelper().createName());
		IdmRoleDto roleOneCreated = createDto(roleOne);
		IdmRoleDto roleTwo = prepareDto();
		roleTwo.setCode(null);
		roleTwo.setBaseCode(getHelper().createName());
		roleTwo.setEnvironment(getHelper().createName());
		roleTwo = createDto(roleTwo);
		//
		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setEnvironment(roleOne.getEnvironment());
		List<IdmRoleDto> roles = find(filter);
		Assert.assertEquals(1, roles.size());
		Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleOneCreated.getId())));
	}
	
	@Test
	public void testFindByBaseCode() {
		IdmRoleDto roleOne = prepareDto();
		roleOne.setCode(null);
		roleOne.setBaseCode(getHelper().createName());
		IdmRoleDto roleOneCreated = createDto(roleOne);
		IdmRoleDto roleTwo = prepareDto();
		roleTwo.setCode(null);
		roleTwo.setBaseCode(getHelper().createName());
		roleTwo = createDto(roleTwo);
		//
		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setBaseCode(roleOne.getBaseCode());
		List<IdmRoleDto> roles = find(filter);
		Assert.assertEquals(1, roles.size());
		Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleOneCreated.getId())));
	}
	
	@Test
	public void testFindByRoleComposition() {
		IdmRoleDto roleRoot = createDto();
		IdmRoleDto roleOne = createDto();
		IdmRoleDto roleOneSub = createDto();
		IdmRoleDto roleTwo = createDto();
		getHelper().createRoleComposition(roleRoot, roleOne);
		getHelper().createRoleComposition(roleRoot, roleTwo);
		getHelper().createRoleComposition(roleOne, roleOneSub);
		
		//
		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setParent(roleRoot.getId());
		List<IdmRoleDto> roles = find(filter);
		Assert.assertEquals(2, roles.size());
		Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleOne.getId())));
		Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleTwo.getId())));
		//
		filter.setParent(roleOne.getId());
		roles = find(filter);
		Assert.assertEquals(1, roles.size());
		Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleOneSub.getId())));
		//
		filter.setParent(roleTwo.getId());
		roles = find(filter);
		Assert.assertTrue(roles.isEmpty());
	}
}
