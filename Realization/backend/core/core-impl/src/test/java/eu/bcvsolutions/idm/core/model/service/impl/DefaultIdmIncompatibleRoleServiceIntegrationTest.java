package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
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
		Assert.assertTrue(service.resolveIncompatibleRoles(Lists.newArrayList((Serializable) null, (Serializable) null)).isEmpty());
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
		Set<ResolvedIncompatibleRoleDto> resolvedIncompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(subTwo.getId()));
		Assert.assertTrue(resolvedIncompatibleRoles.isEmpty());
		//
		resolvedIncompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(subTwo.getId(), superiorTwo.getId()));
		Assert.assertTrue(resolvedIncompatibleRoles.isEmpty());
		//
		resolvedIncompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(subOne.getId())); // wrong definition. TODO: add validation
		Assert.assertTrue(resolvedIncompatibleRoles.isEmpty());
		//
		superior = getHelper().getService(IdmRoleService.class).get(superior); // preloaded role is used
		resolvedIncompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(superior)); // incompatible roles inside business role definition
		Assert.assertEquals(1, resolvedIncompatibleRoles.size());
		Assert.assertTrue(resolvedIncompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getIncompatibleRole().getSuperior().equals(subOne.getId()) && ir.getIncompatibleRole().getSub().equals(subTwo.getId());
				}));
		//
		resolvedIncompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(subOne.getId(), subTwo.getId()));
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
		resolvedIncompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(subOne.getId(), three.getId()));
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
		Set<IdmIncompatibleRoleDto> incompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(subOneSub.getId(), subTwo.getId(), three.getId()))
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
		incompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(subTwo.getId(), three.getId(), subOne.getId()))
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
		incompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(three.getId(), subTwo.getId()))
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
		incompatibleRoles = service.resolveIncompatibleRoles(Lists.newArrayList(three.getId(), superior.getId(), superiorTwo.getId()))
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
	
	@Test
	public void testResolveIncompatibleRolesInBulkRequest() {
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
		
		// create superior roles
		List<IdmRoleDto> assignRoles = Lists.newArrayList(three, superior, superiorTwo);
		//
		int count = 200; // *5 => 1000
		for(int i = 1; i <= count; i++) {
			IdmRoleDto role = getHelper().createRole();
			// create some sub roles
			IdmRoleDto subRole = getHelper().createRole();
			IdmRoleDto subSubRoleOne = getHelper().createRole();
			IdmRoleDto subSubRoleTwo = getHelper().createRole();
			IdmRoleDto subSubRoleThree = getHelper().createRole();
			getHelper().createRoleComposition(role, subRole);
			getHelper().createRoleComposition(subRole, subSubRoleOne);
			getHelper().createRoleComposition(subRole, subSubRoleTwo);
			getHelper().createRoleComposition(subRole, subSubRoleThree);
			getHelper().createIncompatibleRole(threeSubSub, subSubRoleOne);
				//
				// assign target system - should exist
				// FIXME: move to some new acc test, just backup here ...
//				SysSystemDto system = systemService.getByCode("manual-vs");
//				SysSystemMappingDto systemMapping =  AutowireHelper.getBean(SysSystemMappingService.class).findProvisioningMapping(system.getId(), SystemEntityType.IDENTITY);
//				SysRoleSystemDto roleSystem = new SysRoleSystemDto();
//				roleSystem.setSystem(system.getId());
//				roleSystem.setSystemMapping(systemMapping.getId());
//				roleSystem.setRole(role.getId());
//				//
//				// merge attribute - rights + transformation
//				AutowireHelper.getBean(SysRoleSystemAttributeService.class).addRoleMappingAttribute(system.getId(),
//						role.getId(), "rights", "return [\"value-" + i +"\"]", IcObjectClassInfo.ACCOUNT);
			assignRoles.add(role);
		}
		//
		// prepare owner
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		//
		// prepare request
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setState(RoleRequestState.CONCEPT);
		roleRequest.setExecuteImmediately(true); // without approval
		roleRequest.setApplicant(identity.getId());
		roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
		roleRequest = getHelper().getService(IdmRoleRequestService.class).save(roleRequest);
		//
		for (IdmRoleDto assignRole : assignRoles) {
			
			IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
			concept.setIdentityContract(contract.getId());
			concept.setValidFrom(contract.getValidFrom());
			concept.setValidTill(contract.getValidTill());
			concept.setRole(assignRole.getId());
			concept.setOperation(ConceptRoleRequestOperation.ADD);
			concept.setRoleRequest(roleRequest.getId());
			//
			getHelper().getService(IdmConceptRoleRequestService.class).save(concept);
		}
		long start = System.currentTimeMillis();
		//
		Set<IdmIncompatibleRoleDto> incompatibleRoles = getHelper().getService(IdmRoleRequestService.class).getIncompatibleRoles(roleRequest)
				.stream()
				.map(ResolvedIncompatibleRoleDto::getIncompatibleRole)
				.collect(Collectors.toSet());
		//
		long duration = System.currentTimeMillis() - start;
		Assert.assertTrue(duration < 5000);
		Assert.assertEquals(3 + count, incompatibleRoles.size());
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
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(threeSubSub.getId());
				}));
	}
	
	@Test
	public void testResolveIncompatibleRolesInBulkSubRoles() {
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
		
		// create superior roles
		List<IdmRoleDto> assignRoles = Lists.newArrayList(three, superior, superiorTwo);
		//
		IdmRoleDto role = getHelper().createRole();
		int count = 750; // +1 = 751
		for(int i = 1; i <= count; i++) {
			// create some sub roles
			IdmRoleDto subRole = getHelper().createRole();
			getHelper().createRoleComposition(role, subRole);
			getHelper().createIncompatibleRole(threeSubSub, subRole);
				//
				// assign target system - should exist
				// FIXME: move to some new acc test, just backup here ...
//				SysSystemDto system = systemService.getByCode("manual-vs");
//				SysSystemMappingDto systemMapping =  AutowireHelper.getBean(SysSystemMappingService.class).findProvisioningMapping(system.getId(), SystemEntityType.IDENTITY);
//				SysRoleSystemDto roleSystem = new SysRoleSystemDto();
//				roleSystem.setSystem(system.getId());
//				roleSystem.setSystemMapping(systemMapping.getId());
//				roleSystem.setRole(role.getId());
//				//
//				// merge attribute - rights + transformation
//				AutowireHelper.getBean(SysRoleSystemAttributeService.class).addRoleMappingAttribute(system.getId(),
//						role.getId(), "rights", "return [\"value-" + i +"\"]", IcObjectClassInfo.ACCOUNT);
			assignRoles.add(role);
		}
		//
		// prepare owner
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		//
		// prepare request
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setState(RoleRequestState.CONCEPT);
		roleRequest.setExecuteImmediately(true); // without approval
		roleRequest.setApplicant(identity.getId());
		roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
		roleRequest = getHelper().getService(IdmRoleRequestService.class).save(roleRequest);
		//
		for (IdmRoleDto assignRole : assignRoles) {
			
			IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
			concept.setIdentityContract(contract.getId());
			concept.setValidFrom(contract.getValidFrom());
			concept.setValidTill(contract.getValidTill());
			concept.setRole(assignRole.getId());
			concept.setOperation(ConceptRoleRequestOperation.ADD);
			concept.setRoleRequest(roleRequest.getId());
			//
			getHelper().getService(IdmConceptRoleRequestService.class).save(concept);
		}
		long start = System.currentTimeMillis();
		//
		Set<IdmIncompatibleRoleDto> incompatibleRoles = getHelper().getService(IdmRoleRequestService.class).getIncompatibleRoles(roleRequest)
				.stream()
				.map(ResolvedIncompatibleRoleDto::getIncompatibleRole)
				.collect(Collectors.toSet());
		//
		long duration = System.currentTimeMillis() - start;
		Assert.assertTrue(duration < 5000);
		Assert.assertEquals(3 + count, incompatibleRoles.size());
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
		Assert.assertTrue(incompatibleRoles
				.stream()
				.anyMatch(ir -> { 
					return ir.getSuperior().equals(threeSubSub.getId());
				}));
	}
	
}
