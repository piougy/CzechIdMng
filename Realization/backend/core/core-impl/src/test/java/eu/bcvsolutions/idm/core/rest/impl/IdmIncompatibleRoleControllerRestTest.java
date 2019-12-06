package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIncompatibleRoleFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * 
 * @author Radek Tomiška
 *
 */
public class IdmIncompatibleRoleControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmIncompatibleRoleDto> {

	@Autowired private IdmIncompatibleRoleController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmIncompatibleRoleDto, ?> getController() {
		return controller;
	}
	
	@Override
	protected boolean supportsPatch() {
		return false;
	}
	
	@Override
	protected boolean supportsPut() {
		return false;
	}

	@Override
	protected IdmIncompatibleRoleDto prepareDto() {
		IdmIncompatibleRoleDto dto = new IdmIncompatibleRoleDto();
		dto.setSuperior(getHelper().createRole().getId());
		dto.setSub(getHelper().createRole().getId());
		//
		return dto;
	}
	
	@Test
	public void testFindBySuperior() {
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		IdmRoleDto roleThree = getHelper().createRole();
		//
		IdmIncompatibleRoleDto incompatibleRoleOne = getHelper().createIncompatibleRole(roleOne, roleTwo);
		IdmIncompatibleRoleDto incompatibleRoleTwo = getHelper().createIncompatibleRole(roleThree, roleOne);
		IdmIncompatibleRoleDto incompatibleRoleThree = getHelper().createIncompatibleRole(roleThree, roleTwo);
		//
		IdmIncompatibleRoleFilter filter = new IdmIncompatibleRoleFilter();
		filter.setSuperiorId(roleOne.getId());
		List<IdmIncompatibleRoleDto> incompatibleRoles = find(filter);
		Assert.assertEquals(1, incompatibleRoles.size());
		Assert.assertEquals(incompatibleRoleOne.getId(), incompatibleRoles.get(0).getId());
		//
		filter.setSuperiorId(roleThree.getId());
		incompatibleRoles = find(filter);
		Assert.assertEquals(2, incompatibleRoles.size());
		Assert.assertTrue(incompatibleRoles.stream().anyMatch(ir -> ir.getId().equals(incompatibleRoleTwo.getId())));
		Assert.assertTrue(incompatibleRoles.stream().anyMatch(ir -> ir.getId().equals(incompatibleRoleThree.getId())));
		//
		filter.setSuperiorId(roleTwo.getId());
		incompatibleRoles = find(filter);
		Assert.assertTrue(incompatibleRoles.isEmpty());
	}
	
	@Test
	public void testFindBySub() {
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		IdmRoleDto roleThree = getHelper().createRole();
		//
		IdmIncompatibleRoleDto incompatibleRoleOne = getHelper().createIncompatibleRole(roleOne, roleTwo);
		IdmIncompatibleRoleDto incompatibleRoleTwo = getHelper().createIncompatibleRole(roleThree, roleOne);
		IdmIncompatibleRoleDto incompatibleRoleThree = getHelper().createIncompatibleRole(roleThree, roleTwo);
		//
		IdmIncompatibleRoleFilter filter = new IdmIncompatibleRoleFilter();
		filter.setSubId(roleOne.getId());
		List<IdmIncompatibleRoleDto> incompatibleRoles = find(filter);
		Assert.assertEquals(1, incompatibleRoles.size());
		Assert.assertEquals(incompatibleRoleTwo.getId(), incompatibleRoles.get(0).getId());
		//
		filter.setSubId(roleTwo.getId());
		incompatibleRoles = find(filter);
		Assert.assertEquals(2, incompatibleRoles.size());
		Assert.assertTrue(incompatibleRoles.stream().anyMatch(ir -> ir.getId().equals(incompatibleRoleOne.getId())));
		Assert.assertTrue(incompatibleRoles.stream().anyMatch(ir -> ir.getId().equals(incompatibleRoleThree.getId())));
		//
		filter.setSubId(roleThree.getId());
		incompatibleRoles = find(filter);
		Assert.assertTrue(incompatibleRoles.isEmpty());
	}
}
