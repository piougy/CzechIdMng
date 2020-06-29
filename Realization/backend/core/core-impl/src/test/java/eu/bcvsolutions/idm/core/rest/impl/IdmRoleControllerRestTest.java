package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.RoleBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.role.RoleCanBeRequestedEvaluator;
import eu.bcvsolutions.idm.test.api.TestHelper;

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
	public void testFindWithoutCatalogue() {
		// prepare role catalogue
		IdmRoleCatalogueDto roleCatalogue = getHelper().createRoleCatalogue();
		// create roles
		String environment = getHelper().createName();
		IdmRoleDto roleOne = getHelper().createRole(null, null, environment);
		IdmRoleDto roleTwo = getHelper().createRole(null, null, environment);
		// assign role into catalogue
		getHelper().createRoleCatalogueRole(roleOne, roleCatalogue);
		//
		// test
		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setEnvironment(environment);
		filter.setWithoutCatalogue(Boolean.TRUE);
		List<IdmRoleDto> roles = find(filter);
		Assert.assertEquals(1, roles.size());
		Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleTwo.getId())));
		//
		filter.setWithoutCatalogue(Boolean.FALSE);
		roles = find(filter);
		Assert.assertEquals(1, roles.size());
		Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleOne.getId())));
		//
		filter.setWithoutCatalogue(null);
		roles = find(filter);
		Assert.assertEquals(2, roles.size());
		Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleOne.getId())));
		Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleTwo.getId())));
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
		Assert.assertEquals(2, getDto(roleRoot.getId()).getChildrenCount());
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
	
	@Test
	public void testGetIncompatibleRoles() throws Exception {
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		IdmRoleDto roleThree = getHelper().createRole();
		IdmRoleDto roleFour = getHelper().createRole();
		IdmRoleDto roleFive = getHelper().createRole();
		IdmRoleDto roleSix = getHelper().createRole();
		// create incompatible roles definition
		getHelper().createIncompatibleRole(roleTwo, roleFive);
		getHelper().createIncompatibleRole(roleFive, roleSix);
		//
		// create role composition
		getHelper().createRoleComposition(roleOne, roleTwo);
		getHelper().createRoleComposition(roleOne, roleThree);
		getHelper().createRoleComposition(roleTwo, roleFour);
		getHelper().createRoleComposition(roleThree, roleFive);
		//
		String response = getMockMvc().perform(get(String.format("%s/incompatible-roles", getDetailUrl(roleOne.getId())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andReturn()
                .getResponse()
                .getContentAsString();
		//
		Set<IdmIncompatibleRoleDto> incompatibleRoles = toDtos(response, ResolvedIncompatibleRoleDto.class)
				.stream()
				.map(ResolvedIncompatibleRoleDto::getIncompatibleRole)
				.collect(Collectors.toSet());
		Assert.assertEquals(1, incompatibleRoles.size());
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(roleTwo.getId()) && ir.getSub().equals(roleFive.getId());
				}));
	}
	
	@Test
	public void testFindCanBeRequestedRoles() throws Exception {
		String description = getHelper().createName();
		IdmRoleDto role = prepareDto();
		role.setDescription(description);
		role.setCanBeRequested(true);
		IdmRoleDto roleOne = createDto(role); 
		role = prepareDto();
		role.setDescription(description);
		role.setCanBeRequested(false);
		IdmRoleDto roleTwo = createDto(role); // other
		//
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto assignedRole = getHelper().createRole();
		//
		getHelper().createIdentityRole(identity, assignedRole);
		//
		// create authorization policy - assign to role
		getHelper().createAuthorizationPolicy(
				assignedRole.getId(),
				CoreGroupPermission.ROLE,
				IdmRole.class,
				RoleCanBeRequestedEvaluator.class,
				RoleBasePermission.CANBEREQUESTED);
		//
		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setText(description);
		List<IdmRoleDto> roles = find("can-be-requested", filter, getAuthentication(identity.getUsername()));
		//
		Assert.assertFalse(roles.isEmpty());
		Assert.assertTrue(roles.stream().allMatch(r -> r.isCanBeRequested()));
		Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleOne.getId())));
		Assert.assertFalse(roles.stream().anyMatch(r -> r.getId().equals(roleTwo.getId())));
	}
}
