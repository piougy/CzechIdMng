package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Incompatible role tests
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultIdmIncompatibleRoleServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private ApplicationContext context;
	//
	private DefaultIdmIncompatibleRoleService service;

	@Before
	public void init() {
		service = context.getAutowireCapableBeanFactory().createBean(DefaultIdmIncompatibleRoleService.class);
	}
	
	@Test
	public void testReferentialIntegrityRoleIsDeleted() {
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		IdmRoleDto roleThree = getHelper().createRole();
		//
		IdmIncompatibleRoleDto incompatibleRoleOne = getHelper().createIncompatibleRole(roleOne, roleTwo);
		IdmIncompatibleRoleDto incompatibleRoleTwo = getHelper().createIncompatibleRole(roleThree, roleOne);
		IdmIncompatibleRoleDto incompatibleRoleThree = getHelper().createIncompatibleRole(roleThree, roleTwo);
		//
		getHelper().getService(IdmRoleService.class).delete(roleOne);
		//
		Assert.assertNull(service.get(incompatibleRoleOne));
		Assert.assertNull(service.get(incompatibleRoleTwo));
		Assert.assertNotNull(service.get(incompatibleRoleThree));
	}
	
	@Test(expected = EntityNotFoundException.class)
	public void testResolveIncompatibleRolesRoleNotExists() {
		service.resolveIncompatibleRoles(Lists.newArrayList(getHelper().createName()));
	}
	
	@Test
	public void testResolveIncompatibleRoles() {
		Assert.assertTrue(service.resolveIncompatibleRoles(null).isEmpty());
		Assert.assertTrue(service.resolveIncompatibleRoles(Lists.newArrayList()).isEmpty());
		//
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto superiorTwo = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subTwo = getHelper().createRole();
		IdmRoleDto subOneSub = getHelper().createRole();
		IdmRoleDto subOneSubSub = getHelper().createRole();
		IdmRoleDto three = getHelper().createRole();
		IdmRoleDto threeSub = getHelper().createRole();
		IdmRoleDto threeSubSub = getHelper().createRole();
		getHelper().createRoleComposition(superior, subOne);
		getHelper().createRoleComposition(superior, subTwo);
		getHelper().createRoleComposition(subOne, subOneSub);
		getHelper().createRoleComposition(subOneSub, subOneSubSub);
		getHelper().createRoleComposition(three, threeSub);
		getHelper().createRoleComposition(threeSub, threeSubSub);
		// prepare incompatible roles
		getHelper().createIncompatibleRole(subOne, subTwo);
		getHelper().createIncompatibleRole(subOneSubSub, threeSubSub);
		getHelper().createIncompatibleRole(subTwo, threeSub);
		getHelper().createIncompatibleRole(subOne, subOne);
		//
		Set<ResolvedIncompatibleRoleDto> resolvedIncompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(subTwo));
		Assert.assertTrue(resolvedIncompatibleRoles.isEmpty());
		//
		resolvedIncompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(subTwo, superiorTwo));
		Assert.assertTrue(resolvedIncompatibleRoles.isEmpty());
		//
		resolvedIncompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(subOne)); // wrong definition. TODO: add validation
		Assert.assertTrue(resolvedIncompatibleRoles.isEmpty());
		//
		resolvedIncompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(superior)); // incompatible roles inside business role definition
		Assert.assertEquals(1, resolvedIncompatibleRoles.size());
		Assert.assertTrue(resolvedIncompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getIncompatibleRole().getSuperior().equals(subOne.getId()) && ir.getIncompatibleRole().getSub().equals(subTwo.getId());
				}));
		//
		resolvedIncompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(subOne, subTwo));
		Assert.assertEquals(2, resolvedIncompatibleRoles.size());
		Assert.assertTrue(resolvedIncompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getIncompatibleRole().getSuperior().equals(subOne.getId()) 
							&& ir.getIncompatibleRole().getSub().equals(subTwo.getId())
							&& ir.getDirectRole().equals(subOne);
				}));
		Assert.assertTrue(resolvedIncompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getIncompatibleRole().getSuperior().equals(subOne.getId()) 
							&& ir.getIncompatibleRole().getSub().equals(subTwo.getId())
							&& ir.getDirectRole().equals(subTwo);
				}));
		//
		// 
		resolvedIncompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(subOne, three));
		Assert.assertEquals(2, resolvedIncompatibleRoles.size());
		Assert.assertTrue(resolvedIncompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getIncompatibleRole().getSuperior().equals(subOneSubSub.getId()) 
							&& ir.getIncompatibleRole().getSub().equals(threeSubSub.getId())
							&& ir.getDirectRole().equals(subOne);
				}));
		Assert.assertTrue(resolvedIncompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getIncompatibleRole().getSuperior().equals(subOneSubSub.getId()) 
							&& ir.getIncompatibleRole().getSub().equals(threeSubSub.getId())
							&& ir.getDirectRole().equals(three);
				}));
		//
		Set<IdmIncompatibleRoleDto> incompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(subOneSub, subTwo, three))
				.stream()
				.map(ResolvedIncompatibleRoleDto::getIncompatibleRole)
				.collect(Collectors.toSet());
		Assert.assertEquals(2, incompatibleRoles.size());
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(subOneSubSub.getId()) && ir.getSub().equals(threeSubSub.getId());
				}));
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(subTwo.getId()) && ir.getSub().equals(threeSub.getId());
				}));
		//
		incompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(subTwo, three, subOne))
				.stream()
				.map(ResolvedIncompatibleRoleDto::getIncompatibleRole)
				.collect(Collectors.toSet());
		Assert.assertEquals(3, incompatibleRoles.size());
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(subOneSubSub.getId()) && ir.getSub().equals(threeSubSub.getId());
				}));
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(subOne.getId()) && ir.getSub().equals(subTwo.getId());
				}));
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(subTwo.getId()) && ir.getSub().equals(threeSub.getId());
				}));
		//
		incompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(three, subTwo))
				.stream()
				.map(ResolvedIncompatibleRoleDto::getIncompatibleRole)
				.collect(Collectors.toSet());
		Assert.assertEquals(1, incompatibleRoles.size());
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(subTwo.getId()) && ir.getSub().equals(threeSub.getId());
				}));
		//
		incompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(three, superior, superiorTwo))
				.stream()
				.map(ResolvedIncompatibleRoleDto::getIncompatibleRole)
				.collect(Collectors.toSet());
		Assert.assertEquals(3, incompatibleRoles.size());
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(subOneSubSub.getId()) && ir.getSub().equals(threeSubSub.getId());
				}));
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(subOne.getId()) && ir.getSub().equals(subTwo.getId());
				}));
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(subTwo.getId()) && ir.getSub().equals(threeSub.getId());
				}));
	}
	
}
