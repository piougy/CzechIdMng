package eu.bcvsolutions.idm.core.model.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.event.processor.ObserveRequestProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Role composition test
 * - assign subroles (sync / async)
 * - remove subroles (sync / async)
 * - role composition validity
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmRoleCompositionServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private ApplicationContext context;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmCacheManager cacheManager;
	//
	private DefaultIdmRoleCompositionService service;

	@Before
	public void init() {
		service = context.getAutowireCapableBeanFactory().createBean(DefaultIdmRoleCompositionService.class);
	}
	
	@Transactional
	@Test(expected = ResultCodeException.class)
	public void testReferentialIntegrityHasRoleComposition() {
		// prepare data
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto subrole = getHelper().createRole();
		// assigned role
		getHelper().createRoleComposition(role, subrole);
		//
		getHelper().getService(IdmRoleService.class).delete(role);
	}
	
	@Test
	@Transactional
	public void testFindDirectSubRoles() {
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subTwo = getHelper().createRole();
		IdmRoleDto subOneSub = getHelper().createRole();
		getHelper().createRoleComposition(superior, subOne);
		getHelper().createRoleComposition(superior, subTwo);
		getHelper().createRoleComposition(subOne, subOneSub);
		//
		List<IdmRoleCompositionDto> directSubRoles = service.findDirectSubRoles(superior.getId());
		Assert.assertEquals(2, directSubRoles.size());
		Assert.assertTrue(directSubRoles.stream().anyMatch(s -> s.getSub().equals(subOne.getId())));
		Assert.assertTrue(directSubRoles.stream().anyMatch(s -> s.getSub().equals(subTwo.getId())));
		//
		directSubRoles = service.findDirectSubRoles(subOne.getId());
		Assert.assertEquals(1, directSubRoles.size());
		Assert.assertTrue(directSubRoles.stream().anyMatch(s -> s.getSub().equals(subOneSub.getId())));
	}
	
	@Test
	@Transactional
	public void testFindAllSuperiorRoles() {
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subTwo = getHelper().createRole();
		IdmRoleDto subOneSub = getHelper().createRole();
		IdmRoleDto subOneSubSub = getHelper().createRole();
		getHelper().createRoleComposition(superior, subOne);
		getHelper().createRoleComposition(superior, subTwo);
		getHelper().createRoleComposition(subOne, subOneSub);
		getHelper().createRoleComposition(subOneSub, subOneSubSub);
		//
		List<IdmRoleCompositionDto> allSuperiorRoles = service.findAllSuperiorRoles(superior.getId());
		Assert.assertTrue(allSuperiorRoles.isEmpty());
		//
		allSuperiorRoles = service.findAllSuperiorRoles(subOne.getId());
		Assert.assertEquals(1, allSuperiorRoles.size());
		Assert.assertTrue(allSuperiorRoles.stream().anyMatch(s -> s.getSuperior().equals(superior.getId())));
		//
		allSuperiorRoles = service.findAllSuperiorRoles(subOneSubSub.getId());
		Assert.assertEquals(3, allSuperiorRoles.size());
		// ordered
		Assert.assertEquals(subOneSub.getId(), allSuperiorRoles.get(0).getSuperior());
		Assert.assertEquals(subOne.getId(), allSuperiorRoles.get(1).getSuperior());
		Assert.assertEquals(superior.getId(), allSuperiorRoles.get(2).getSuperior());
	}
	
	@Test
	@Transactional
	public void testFindAllSuperiorWithCycles() {
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subTwo = getHelper().createRole();
		getHelper().createRoleComposition(superior, subOne);
		getHelper().createRoleComposition(subOne, subTwo);
		getHelper().createRoleComposition(subTwo, superior);
		//
		List<IdmRoleCompositionDto> allSuperiorRoles = service.findAllSuperiorRoles(superior.getId());
		Assert.assertEquals(2, allSuperiorRoles.size());
		// ordered
		Assert.assertEquals(subTwo.getId(), allSuperiorRoles.get(0).getSuperior());
		Assert.assertEquals(subOne.getId(), allSuperiorRoles.get(1).getSuperior());
	}
	
	@Test
	@Transactional
	public void testFindAllSuperiorWithSelf() {
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		getHelper().createRoleComposition(superior, superior);
		//
		List<IdmRoleCompositionDto> allSuperiorRoles = service.findAllSuperiorRoles(superior.getId());
		Assert.assertTrue(allSuperiorRoles.isEmpty());
	}
	
	@Test
	@Transactional
	public void testAssignUpdateRemoveSubRoles() {
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subTwo = getHelper().createRole();
		IdmRoleDto subOneSub = getHelper().createRole();
		IdmRoleDto subOneSubSub = getHelper().createRole();
		getHelper().createRoleComposition(superior, subOne);
		getHelper().createRoleComposition(superior, subTwo);
		getHelper().createRoleComposition(subOne, subOneSub);
		getHelper().createRoleComposition(subOneSub, subOneSubSub);
		//
		// assign subOne role
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityRoleDto identityRole = getHelper().createIdentityRole(identity, subOne, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(3, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOneSub.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOneSubSub.getId())));
		Assert.assertTrue(assignedRoles.stream().allMatch(ir -> ir.getValidFrom().equals(identityRole.getValidFrom())));
		Assert.assertTrue(assignedRoles.stream().allMatch(ir -> ir.getValidTill().equals(identityRole.getValidTill())));
		//
		// update
		identityRole.setValidFrom(null);
		identityRoleService.save(identityRole);
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(3, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOneSub.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOneSubSub.getId())));
		Assert.assertTrue(assignedRoles.stream().allMatch(ir -> ir.getValidFrom() == null));
		Assert.assertTrue(assignedRoles.stream().allMatch(ir -> ir.getValidTill().equals(identityRole.getValidTill())));
		//
		// delete
		identityRoleService.delete(identityRole);
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
	}
	
	@Test
	@Transactional
	public void testAssignRolesPreventCycles() {		
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subOneSub = getHelper().createRole();
		getHelper().createRoleComposition(superior, subOne);
		getHelper().createRoleComposition(subOne, subOneSub);
		getHelper().createRoleComposition(subOneSub, superior);
		//
		// assign superior role
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, superior);
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(3, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(superior.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOneSub.getId())));
		//
		// assign sub role with cycle
		IdmIdentityDto identityTwo = getHelper().createIdentity();
		getHelper().createIdentityRole(identityTwo, subOneSub);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identityTwo.getId());
		Assert.assertEquals(3, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(superior.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOneSub.getId())));
	}
	
	@Test
	@Transactional
	public void testAssignRolesPreventCyclesSameSuperiorAsSub() {
		IdmRoleDto superior = getHelper().createRole();
		getHelper().createRoleComposition(superior, superior);
		//
		// assign superior role
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, superior);
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(superior.getId())));
	}
	
	@Test
	@Transactional
	public void testAssignRolesPreventCyclesSameSuperiorAsSubByRequest() {
		IdmRoleDto superior = getHelper().createRole();
		getHelper().createRoleComposition(superior, superior);
		//
		List<IdmRoleCompositionDto> allSubRoles = service.findAllSubRoles(superior.getId());
		Assert.assertTrue(allSubRoles.isEmpty());
		//
		// assign superior role
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleRequestDto roleRequest = getHelper().createRoleRequest(identity, superior);
		getHelper().executeRequest(roleRequest, false);
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(superior.getId())));
	}
	
	@Test
	public void testAssignRolesPreventCyclesByRequest() {
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subTwo = getHelper().createRole();
		IdmRoleCompositionDto subOneComposition = getHelper().createRoleComposition(superior, subOne);
		IdmRoleCompositionDto subTwoComposition = getHelper().createRoleComposition(subOne, subTwo);
		IdmRoleCompositionDto cyclicComposition = getHelper().createRoleComposition(subTwo, superior);
		//
		// find all sub roles
		List<IdmRoleCompositionDto> allSubRoles = service.findAllSubRoles(superior.getId());
		Assert.assertEquals(2, allSubRoles.size());
		Assert.assertTrue(allSubRoles.stream().anyMatch(c -> c.getId().equals(subOneComposition.getId())));
		Assert.assertTrue(allSubRoles.stream().anyMatch(c -> c.getId().equals(subTwoComposition.getId())));
		//
		// assign superior role
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleRequestDto roleRequest = getHelper().createRoleRequest(identity, superior);
		//
		getHelper().executeRequest(roleRequest, false);
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(3, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(superior.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subTwo.getId())));
		//
		// remove role composition
		service.delete(cyclicComposition);
		//
		allSubRoles = service.findAllSubRoles(superior.getId());
		Assert.assertEquals(2, allSubRoles.size());
		Assert.assertTrue(allSubRoles.stream().anyMatch(c -> c.getId().equals(subOneComposition.getId())));
		Assert.assertTrue(allSubRoles.stream().anyMatch(c -> c.getId().equals(subTwoComposition.getId())));
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(3, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(superior.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subTwo.getId())));
		//
		cyclicComposition = getHelper().createRoleComposition(subTwo, superior);
		//
		allSubRoles = service.findAllSubRoles(superior.getId());
		Assert.assertEquals(2, allSubRoles.size());
		Assert.assertTrue(allSubRoles.stream().anyMatch(c -> c.getId().equals(subOneComposition.getId())));
		Assert.assertTrue(allSubRoles.stream().anyMatch(c -> c.getId().equals(subTwoComposition.getId())));
		// role is asssigned in the middle => cycle is detected one step after
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(6, assignedRoles.size());
		Assert.assertEquals(2, assignedRoles.stream().filter(ir -> ir.getRole().equals(superior.getId())).count());
		Assert.assertEquals(2, assignedRoles.stream().filter(ir -> ir.getRole().equals(subOne.getId())).count());
		Assert.assertEquals(2, assignedRoles.stream().filter(ir -> ir.getRole().equals(subTwo.getId())).count());
		//
		// remove role composition again
		service.delete(cyclicComposition);
		//
		allSubRoles = service.findAllSubRoles(superior.getId());
		Assert.assertEquals(2, allSubRoles.size());
		Assert.assertTrue(allSubRoles.stream().anyMatch(c -> c.getId().equals(subOneComposition.getId())));
		Assert.assertTrue(allSubRoles.stream().anyMatch(c -> c.getId().equals(subTwoComposition.getId())));
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(3, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(superior.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subTwo.getId())));
		
	}
	
	@Test
	public void testAssignRolesDuplicates() {
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subTwo = getHelper().createRole();
		IdmRoleDto subOneOne = getHelper().createRole();
		IdmRoleDto subSubOneOne = getHelper().createRole();
		getHelper().createRoleComposition(superior, subOne);
		getHelper().createRoleComposition(superior, subTwo);
		getHelper().createRoleComposition(subOne, subOneOne);
		IdmRoleCompositionDto cyclicComposition = getHelper().createRoleComposition(subTwo, subOneOne);
		getHelper().createRoleComposition(subOneOne, subSubOneOne);
		//
		// find all sub roles
		List<IdmRoleCompositionDto> allSubRoles = service.findAllSubRoles(superior.getId());
		Assert.assertEquals(6, allSubRoles.size());
		//
		// assign superior role
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleRequestDto roleRequest = getHelper().createRoleRequest(identity, superior);
		//
		getHelper().executeRequest(roleRequest, false);
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(7, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(superior.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subTwo.getId())));
		Assert.assertEquals(2, assignedRoles.stream().filter(ir -> ir.getRole().equals(subOneOne.getId())).count());
		Assert.assertEquals(2, assignedRoles.stream().filter(ir -> ir.getRole().equals(subSubOneOne.getId())).count());
		//
		// remove role composition
		service.delete(cyclicComposition);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(5, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(superior.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subTwo.getId())));
		Assert.assertEquals(1, assignedRoles.stream().filter(ir -> ir.getRole().equals(subOneOne.getId())).count());
		Assert.assertEquals(1, assignedRoles.stream().filter(ir -> ir.getRole().equals(subSubOneOne.getId())).count());
		//
		// create again
		cyclicComposition = getHelper().createRoleComposition(subTwo, subOneOne);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(7, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(superior.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subTwo.getId())));
		Assert.assertEquals(2, assignedRoles.stream().filter(ir -> ir.getRole().equals(subOneOne.getId())).count());
		Assert.assertEquals(2, assignedRoles.stream().filter(ir -> ir.getRole().equals(subSubOneOne.getId())).count());
		//
		// remove again
		service.delete(cyclicComposition);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(5, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(superior.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subTwo.getId())));
		Assert.assertEquals(1, assignedRoles.stream().filter(ir -> ir.getRole().equals(subOneOne.getId())).count());
		Assert.assertEquals(1, assignedRoles.stream().filter(ir -> ir.getRole().equals(subSubOneOne.getId())).count());
	}
	
	@Test
	public void testAssignSubRolesByRequest() {
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subTwo = getHelper().createRole();
		IdmRoleDto subOneSub = getHelper().createRole();
		IdmRoleDto subOneSubSub = getHelper().createRole();
		getHelper().createRoleComposition(superior, subOne);
		getHelper().createRoleComposition(superior, subTwo);
		getHelper().createRoleComposition(subOne, subOneSub);
		getHelper().createRoleComposition(subOneSub, subOneSubSub);
		//
		// assign superior role
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		IdmRoleRequestDto roleRequest = getHelper().createRoleRequest(identity, subOne);
		//
		getHelper().executeRequest(roleRequest, false);
		//
		// check after create
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(3, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOneSub.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOneSubSub.getId())));
	}
	
	@Test
	@Ignore
	public void testAssignSubRolesByRequestAsync() throws InterruptedException {
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subTwo = getHelper().createRole();
		IdmRoleDto subOneSub = getHelper().createRole();
		IdmRoleDto subOneSubSub = getHelper().createRole();
		getHelper().createRoleComposition(superior, subOne);
		getHelper().createRoleComposition(superior, subTwo);
		getHelper().createRoleComposition(subOne, subOneSub);
		getHelper().createRoleComposition(subOneSub, subOneSubSub);
		//
		try {
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
			//
			// assign superior role
			IdmIdentityDto identity = getHelper().createIdentity();
			//
			IdmRoleRequestDto roleRequest = getHelper().createRoleRequest(identity, subOne);
			//
			ObserveRequestProcessor.listenContent(roleRequest.getId());
			//
			getHelper().executeRequest(roleRequest, false);
			//
			// wait for notify
			ObserveRequestProcessor.waitForEnd(roleRequest.getId()); 	
			//
			// check after create
			List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
			Assert.assertEquals(3, assignedRoles.size());
			Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
			Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOneSub.getId())));
			Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOneSubSub.getId())));
		} finally {
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, false);
		}
	}
	
	@Test
	public void testRemoveAssignedRolesAfterRemoveRoleComposition() {
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subOneSub = getHelper().createRole();
		getHelper().createRoleComposition(superior, subOne);
		IdmRoleCompositionDto subOneSubRoleComposition = getHelper().createRoleComposition(subOne, subOneSub);
		//
		// assign superior role
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, superior);
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(3, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(superior.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOneSub.getId())));
		//
		// remove role composition
		service.delete(subOneSubRoleComposition);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(2, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(superior.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
	}
	
	@Test
	public void testAssignRolesAfterCreateRoleComposition() {
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subOneSub = getHelper().createRole();
		getHelper().createRoleComposition(superior, subOne);
		//
		// assign superior role
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, superior);
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(2, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(superior.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
		//
		// create composition
		getHelper().createRoleComposition(subOne, subOneSub);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(3, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(superior.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subOneSub.getId())));
	}
	
	@Test
	@Transactional
	public void testGetDistinctRoles() {
		// null => empty
		Assert.assertTrue(service.getDistinctRoles(null).isEmpty());
		Assert.assertTrue(service.getDistinctRoles(new ArrayList<>()).isEmpty());
		//
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subTwo = getHelper().createRole();
		IdmRoleDto subOneSub = getHelper().createRole();
		IdmRoleDto subOneSubSub = getHelper().createRole();
		List<IdmRoleCompositionDto> compositions = new ArrayList<>();
		compositions.add(getHelper().createRoleComposition(superior, subOne));
		compositions.add(getHelper().createRoleComposition(superior, subTwo));
		compositions.add(getHelper().createRoleComposition(subOne, subOneSub));
		compositions.add(getHelper().createRoleComposition(subOneSub, subOneSubSub));
		//
		Set<UUID> distinctRoles = service.getDistinctRoles(compositions);
		//
		Assert.assertEquals(5, distinctRoles.size());
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(superior.getId())));
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(subOne.getId())));
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(subTwo.getId())));
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(subOneSub.getId())));
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(subOneSubSub.getId())));
	}
	
	@Test
	@Transactional
	public void testFindAllSubRoles() {
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subTwo = getHelper().createRole();
		IdmRoleDto subOneSub = getHelper().createRole();
		IdmRoleDto subOneSubSub = getHelper().createRole();
		getHelper().createRoleComposition(superior, subOne);
		getHelper().createRoleComposition(superior, subTwo);
		getHelper().createRoleComposition(subOne, subOneSub);
		getHelper().createRoleComposition(subOneSub, subOneSubSub);
		//
		List<IdmRoleCompositionDto> allSubRoles = service.findAllSubRoles(superior.getId());
		Set<UUID> distinctRoles = service.getDistinctRoles(allSubRoles);
		Assert.assertEquals(5, distinctRoles.size());
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(superior.getId())));
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(subOne.getId())));
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(subTwo.getId())));
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(subOneSub.getId())));
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(subOneSubSub.getId())));
		//
		allSubRoles = service.findAllSubRoles(subOneSubSub.getId());
		Assert.assertTrue(allSubRoles.isEmpty());
		Assert.assertNotNull(cacheManager.getValue(IdmRoleCompositionService.ALL_SUB_ROLES_CACHE_NAME, subOneSubSub.getId()));
		//
		allSubRoles = service.findAllSubRoles(subOne.getId());
		distinctRoles = service.getDistinctRoles(allSubRoles);
		Assert.assertNotNull(cacheManager.getValue(IdmRoleCompositionService.ALL_SUB_ROLES_CACHE_NAME, subOne.getId()));
		//
		Assert.assertEquals(3, distinctRoles.size());
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(subOne.getId())));
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(subOneSub.getId())));
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(subOneSubSub.getId())));
		//
		// add role composition
		IdmRoleDto subOneSubTwo = getHelper().createRole();
		getHelper().createRoleComposition(subOneSub, subOneSubTwo);
		Assert.assertNull(cacheManager.getValue(IdmRoleCompositionService.ALL_SUB_ROLES_CACHE_NAME, subOneSubSub.getId()));
		Assert.assertNull(cacheManager.getValue(IdmRoleCompositionService.ALL_SUB_ROLES_CACHE_NAME, subOne.getId()));
		//
		allSubRoles = service.findAllSubRoles(subOne.getId());
		distinctRoles = service.getDistinctRoles(allSubRoles);
		Assert.assertNotNull(cacheManager.getValue(IdmRoleCompositionService.ALL_SUB_ROLES_CACHE_NAME, subOne.getId()));
		//
		Assert.assertEquals(4, distinctRoles.size());
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(subOne.getId())));
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(subOneSub.getId())));
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(subOneSubSub.getId())));
		Assert.assertTrue(distinctRoles.stream().anyMatch(r -> r.equals(subOneSubTwo.getId())));
	}
	
}
