package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.AddNewAutomaticRoleTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RemoveAutomaticRoleTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Integration tests with identity contracts:
 * - work with assigned roles, when contract is changed (disable, etc.)
 * - automatic role is defined, changed
 * - expiration
 * - evaluate state
 * - filter
 * 
 * TODO: @Transactional
 * 
 * @author Radek Tomiška
 * @author Marek Klement
 *
 */
public class DefaultIdmIdentityContractServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmIdentityContractService service;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private LongRunningTaskManager taskManager;
	@Autowired private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired private ConfigurationService configurationService;
	@Autowired private IdmTreeNodeService treeNodeService;
	@Autowired private LookupService lookupService;
	//
	private IdmTreeTypeDto treeType = null;
	private IdmTreeNodeDto nodeA = null;
	private IdmTreeNodeDto nodeB = null;
	private IdmTreeNodeDto nodeC = null;
	private IdmTreeNodeDto nodeD = null;
	private IdmTreeNodeDto nodeE = null;
	private IdmTreeNodeDto nodeF = null;
	private IdmRoleDto roleA = null;
	private IdmRoleDto roleB = null;
	private IdmRoleDto roleC = null;
	private IdmRoleDto roleD = null;
	private IdmRoleTreeNodeDto automaticRoleA = null;
	private IdmRoleTreeNodeDto automaticRoleD = null;
	private IdmRoleTreeNodeDto automaticRoleE = null;
	private IdmRoleTreeNodeDto automaticRoleF = null;
	
	@Before
	public void init() {
		loginAsAdmin();
		prepareTreeStructureAndRoles();
	}
	
	@After 
	public void logout() {
		// delete this test automatic roles only	
		if(automaticRoleA != null) try { deleteAutomaticRole(automaticRoleA); } catch (EmptyResultDataAccessException ex) {} ;
		if(automaticRoleD != null) try { deleteAutomaticRole(automaticRoleD); } catch (EmptyResultDataAccessException ex) {} ;
		if(automaticRoleE != null) try { deleteAutomaticRole(automaticRoleE); } catch (EmptyResultDataAccessException ex) {} ;
		if(automaticRoleF != null) try { deleteAutomaticRole(automaticRoleF); } catch (EmptyResultDataAccessException ex) {} ;
		//
		super.logout();
	}	
	
	private void prepareTreeStructureAndRoles() {
		// tree type
		treeType = getHelper().createTreeType();
		// four levels
		// https://proj.bcvsolutions.eu/ngidm/doku.php?id=roztridit:standardni_procesy#role_pridelovane_na_zaklade_zarazeni_v_organizacni_strukture
		nodeA = getHelper().createTreeNode(treeType, null);
		nodeB = getHelper().createTreeNode(treeType, nodeA);
		nodeC = getHelper().createTreeNode(treeType, nodeB);
		nodeD = getHelper().createTreeNode(treeType, nodeB);
		nodeE = getHelper().createTreeNode(treeType, nodeD);
		nodeF = getHelper().createTreeNode(treeType, nodeD);
		// create roles
		roleA = getHelper().createRole();
		roleB = getHelper().createRole();
		roleC = getHelper().createRole();
		roleD = getHelper().createRole();
	}
	
	/**
	 * Save automatic role with repository and manual create and wait for task
	 * @param automaticRole
	 * @return
	 */
	private IdmRoleTreeNodeDto saveAutomaticRole(IdmRoleTreeNodeDto automaticRole, boolean withLongRunningTask) {
		automaticRole.setName("default"); // default name
		IdmRoleTreeNodeDto roleTreeNode = roleTreeNodeService.saveInternal(automaticRole);
		//
		if (withLongRunningTask) {
			AddNewAutomaticRoleTaskExecutor task = new AddNewAutomaticRoleTaskExecutor();
			task.setAutomaticRoleId(roleTreeNode.getId());
			taskManager.executeSync(task);
		}
		//
		return roleTreeNodeService.get(roleTreeNode.getId());
	}
	
	private void deleteAutomaticRole(IdmRoleTreeNodeDto automaticRole) {
		RemoveAutomaticRoleTaskExecutor task = new RemoveAutomaticRoleTaskExecutor();
		task.setAutomaticRoleId(automaticRole.getId());
		taskManager.executeSync(task);
	}
	
	/**
	 * 
	 */
	private void prepareAutomaticRoles() {
		// prepare automatic roles
		automaticRoleA = new IdmRoleTreeNodeDto();
		automaticRoleA.setRecursionType(RecursionType.DOWN);
		automaticRoleA.setRole(roleA.getId());
		automaticRoleA.setTreeNode(nodeA.getId());
		automaticRoleA = saveAutomaticRole(automaticRoleA, false);	
		
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.DOWN);
		automaticRoleD.setRole(roleB.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = saveAutomaticRole(automaticRoleD, false);
		
		automaticRoleF = new IdmRoleTreeNodeDto();
		automaticRoleF.setRecursionType(RecursionType.UP);
		automaticRoleF.setRole(roleC.getId());
		automaticRoleF.setTreeNode(nodeF.getId());
		automaticRoleF = saveAutomaticRole(automaticRoleF, false);
		
		automaticRoleE = new IdmRoleTreeNodeDto();
		automaticRoleE.setRecursionType(RecursionType.NO);
		automaticRoleE.setRole(roleD.getId());
		automaticRoleE.setTreeNode(nodeE.getId());
		automaticRoleE = saveAutomaticRole(automaticRoleE, false);
	}
	
	@Test
	public void testFindAutomaticRoleWithoutRecursion() {
		// prepare
		automaticRoleA = new IdmRoleTreeNodeDto();
		automaticRoleA.setRecursionType(RecursionType.NO);
		automaticRoleA.setRole(roleA.getId());
		automaticRoleA.setTreeNode(nodeD.getId());
		automaticRoleA = saveAutomaticRole(automaticRoleA, false);
		//
		// test
		Set<IdmRoleTreeNodeDto> automaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(nodeD.getId());
		assertEquals(1, automaticRoles.size());
		assertEquals(roleA.getId(), automaticRoles.iterator().next().getRole());
		assertTrue(roleTreeNodeService.getAutomaticRolesByTreeNode(nodeB.getId()).isEmpty());
		assertTrue(roleTreeNodeService.getAutomaticRolesByTreeNode(nodeF.getId()).isEmpty());
	}
	
	@Test
	public void testFindAutomaticRoleWithRecursionDown() {
		// prepare
		automaticRoleA = new IdmRoleTreeNodeDto();
		automaticRoleA.setRecursionType(RecursionType.DOWN);
		automaticRoleA.setRole(roleA.getId());
		automaticRoleA.setTreeNode(nodeD.getId());
		automaticRoleA = saveAutomaticRole(automaticRoleA, false);
		//
		// test
		Set<IdmRoleTreeNodeDto> automaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(nodeD.getId());
		assertEquals(1, automaticRoles.size());
		assertEquals(roleA.getId(), automaticRoles.iterator().next().getRole());
		assertTrue(roleTreeNodeService.getAutomaticRolesByTreeNode(nodeB.getId()).isEmpty());
		automaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(nodeF.getId());
		assertEquals(1, automaticRoles.size());
		assertEquals(roleA.getId(), automaticRoles.iterator().next().getRole());
	}
	
	@Test
	public void testFindAutomaticRoleWithRecursionUp() {
		// prepare
		automaticRoleA = new IdmRoleTreeNodeDto();
		automaticRoleA.setRecursionType(RecursionType.UP);
		automaticRoleA.setRole(roleA.getId());
		automaticRoleA.setTreeNode(nodeD.getId());
		automaticRoleA = saveAutomaticRole(automaticRoleA, false);
		//
		// test
		Set<IdmRoleTreeNodeDto> automaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(nodeD.getId());
		assertEquals(1, automaticRoles.size());
		assertEquals(roleA.getId(), automaticRoles.iterator().next().getRole());
		assertTrue(roleTreeNodeService.getAutomaticRolesByTreeNode(nodeF.getId()).isEmpty());
		automaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(nodeB.getId());
		assertEquals(1, automaticRoles.size());
		assertEquals(roleA.getId(), automaticRoles.iterator().next().getRole());
	}	
	
	@Test
	public void testCRUDContractWithAutomaticRoles() {
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = service.getPrimeContract(identity.getId());
		contract.setIdentity(identity.getId());
		contract.setValidFrom(new LocalDate().minusDays(1));
		contract.setValidTill(new LocalDate().plusMonths(1));
		contract.setWorkPosition(nodeD.getId());
		contract.setMain(true);
		contract.setDescription("test-node-d");
		service.save(contract);
		//
		contract = service.getPrimeContract(identity.getId());
		//
		// test after create
		assertEquals(nodeD.getId(), contract.getWorkPosition());
		assertEquals("test-node-d", contract.getDescription());
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRoleDto identityRole : identityRoles) {
			assertEquals(contract.getValidFrom(), identityRole.getValidFrom());
			assertEquals(contract.getValidTill(), identityRole.getValidTill());
			if (identityRole.getAutomaticRole().equals(nodeA.getId())) {
				assertEquals(roleA, identityRole.getRole());
			}
			if (identityRole.getAutomaticRole().equals(nodeD.getId())) {
				assertEquals(roleB, identityRole.getRole());
			}
			if (identityRole.getAutomaticRole().equals(nodeF.getId())) {
				assertEquals(roleC, identityRole.getRole());
			}
		}
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(3, identityRoles.size());
		//
		// test after delete
		service.delete(contract);
		assertTrue(identityRoleService.findAllByIdentity(identity.getId()).isEmpty());
	}
	
	@Test
	public void testChangeContractValidityWithAssignedRoles() {
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setValidFrom(new LocalDate().minusDays(1));
		contract.setValidTill(new LocalDate().plusMonths(1));
		contract.setWorkPosition(nodeD.getId());
		contract = service.save(contract);
		//
		// test after create
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRoleDto identityRole : identityRoles) {
			assertEquals(contract.getValidFrom(), identityRole.getValidFrom());
			assertEquals(contract.getValidTill(), identityRole.getValidTill());
		};
		// test after change
		contract.setValidFrom(new LocalDate().minusDays(2));
		contract.setValidTill(new LocalDate().plusMonths(4));
		contract = service.save(contract);
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRoleDto identityRole : identityRoles) {
			assertEquals(contract.getValidFrom(), identityRole.getValidFrom());
			assertEquals(contract.getValidTill(), identityRole.getValidTill());
		}
	}
	
	@Test
	public void testChangeContractPositionWithAutomaticRolesAssigned() {
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setWorkPosition(nodeD.getId());
		contract = service.save(contract);
		// 
		// test after create
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRoleDto identityRole : identityRoles) {
			if (identityRole.getAutomaticRole().equals(nodeA.getId())) {
				assertEquals(roleA, identityRole.getRole());
			}
			if (identityRole.getAutomaticRole().equals(nodeD.getId())) {
				assertEquals(roleB, identityRole.getRole());
			}
			if (identityRole.getAutomaticRole().equals(nodeF.getId())) {
				assertEquals(roleC, identityRole.getRole());
			}
		}
		//
		contract.setWorkPosition(nodeE.getId());
		contract = service.save(contract);
		//
		// test after change
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRoleDto identityRole : identityRoles) {
			if (identityRole.getAutomaticRole().equals(nodeA.getId())) {
				assertEquals(roleA, identityRole.getRole());
			}
			if (identityRole.getAutomaticRole().equals(nodeD.getId())) {
				assertEquals(roleB, identityRole.getRole());
			}
			if (identityRole.getAutomaticRole().equals(nodeE.getId())) {
				assertEquals(roleD, identityRole.getRole());
			}
		}
	}
	
	@Test
	public void testDeleteAutomaticRoleWithContractAlreadyExists() {
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setWorkPosition(nodeC.getId());
		contract = service.save(contract);
		//
		assertEquals(1, identityRoleService.findAllByContract(contract.getId()).size());
		//
		deleteAutomaticRole(automaticRoleD);
		assertEquals(1, identityRoleService.findAllByContract(contract.getId()).size());
		//
		deleteAutomaticRole(automaticRoleA);
		assertTrue(identityRoleService.findAllByContract(contract.getId()).isEmpty());
	}
	
	@Test
	public void testDontRemoveSameRole() {
		automaticRoleF = new IdmRoleTreeNodeDto();
		automaticRoleF.setRecursionType(RecursionType.UP);
		automaticRoleF.setRole(roleA.getId());
		automaticRoleF.setTreeNode(nodeF.getId());
		automaticRoleF = saveAutomaticRole(automaticRoleF, false);
		//
		automaticRoleE = new IdmRoleTreeNodeDto();
		automaticRoleE.setRecursionType(RecursionType.NO);
		automaticRoleE.setRole(roleA.getId());
		automaticRoleE.setTreeNode(nodeE.getId());
		automaticRoleE = saveAutomaticRole(automaticRoleE, false);
		//
		// prepare identity and contract
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setWorkPosition(nodeF.getId());
		contract = service.save(contract);
		//
		// check assigned role after creation 
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(1, identityRoles.size());
		assertEquals(roleA.getId(), identityRoles.get(0).getRole());
		assertEquals(automaticRoleF.getId(), identityRoles.get(0).getAutomaticRole());
		//
		UUID id = identityRoles.get(0).getId();
		//
		// change
		contract.setWorkPosition(nodeE.getId());
		contract = service.save(contract);
		//
		// check assigned role after creation 
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(1, identityRoles.size());
		assertEquals(roleA.getId(), identityRoles.get(0).getRole());
		assertEquals(automaticRoleE.getId(), identityRoles.get(0).getAutomaticRole());
		assertEquals(id, identityRoles.get(0).getId());
	}
	
	@Test
	public void testAssingRoleByNewAutomaticRoleForExistingContracts() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setPosition("new position");
		contract = service.save(contract);
		//
		IdmIdentityContractDto contractF = new IdmIdentityContractDto();
		contractF.setIdentity(identity.getId());
		contractF.setWorkPosition(nodeF.getId());
		contractF = service.save(contractF);
		//
		IdmIdentityContractDto contractD = new IdmIdentityContractDto();
		contractD.setIdentity(identity.getId());
		contractD.setWorkPosition(nodeD.getId());
		contractD = service.save(contractD);
		//
		IdmIdentityContractDto contractB = new IdmIdentityContractDto();
		contractB.setIdentity(identity.getId());
		contractB.setWorkPosition(nodeB.getId());
		contractB = service.save(contractB);
		//
		// create new automatic role
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.DOWN);
		automaticRoleD.setRole(roleA.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = saveAutomaticRole(automaticRoleD, true);
		//
		// check
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contractB.getId());
		assertTrue(identityRoles.isEmpty());
		//
		identityRoles = identityRoleService.findAllByContract(contractD.getId());
		assertEquals(1, identityRoles.size());
		assertEquals(automaticRoleD.getId(), identityRoles.get(0).getAutomaticRole());
		//
		identityRoles = identityRoleService.findAllByContract(contractF.getId());
		assertEquals(1, identityRoles.size());
		assertEquals(automaticRoleD.getId(), identityRoles.get(0).getAutomaticRole());
	}
	
	@Test
	public void testAssingRoleForContractValidInTheFuture() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contractD = new IdmIdentityContractDto();
		contractD.setIdentity(identity.getId());
		contractD.setWorkPosition(nodeD.getId());
		contractD.setValidFrom(new LocalDate().plusDays(1));
		contractD = service.save(contractD);
		//
		// create new automatic role
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.NO);
		automaticRoleD.setRole(roleA.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = saveAutomaticRole(automaticRoleD, true);
		//
		// check
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contractD.getId());
		assertEquals(1, identityRoles.size());
		assertEquals(automaticRoleD.getId(), identityRoles.get(0).getAutomaticRole());
	}
	
	@Test
	public void testDontAssingRoleForContractValidInThePast() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contractD = new IdmIdentityContractDto();
		contractD.setIdentity(identity.getId());
		contractD.setWorkPosition(nodeD.getId());
		contractD.setValidTill(new LocalDate().minusDays(1));
		contractD = service.save(contractD);
		//
		// create new automatic role
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.NO);
		automaticRoleD.setRole(roleA.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = saveAutomaticRole(automaticRoleD, true);
		//
		// check
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contractD.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testDontAssingRoleForDisabledContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contractD = new IdmIdentityContractDto();
		contractD.setIdentity(identity.getId());
		contractD.setWorkPosition(nodeD.getId());
		contractD.setState(ContractState.DISABLED);
		contractD = service.save(contractD);
		//
		// create new automatic role
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.NO);
		automaticRoleD.setRole(roleA.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = saveAutomaticRole(automaticRoleD, true);
		//
		// check
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contractD.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testDisableContractWithAssignedRoles() {
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setValidFrom(new LocalDate().minusDays(1));
		contract.setValidTill(new LocalDate().plusMonths(1));
		contract.setWorkPosition(nodeD.getId());
		contract = service.save(contract);
		//
		// test after create
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRoleDto identityRole : identityRoles) {
			assertEquals(contract.getValidFrom(), identityRole.getValidFrom());
			assertEquals(contract.getValidTill(), identityRole.getValidTill());
		};
		// test after change
		contract.setState(ContractState.DISABLED);
		contract = service.save(contract);
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertTrue(identityRoles.isEmpty());
		// enable again
		contract.setState(null);
		contract = service.save(contract);
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testReferentialIntegrityOnRole() {
		// prepare data
		IdmRoleDto role = getHelper().createRole();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		// automatic role
		IdmRoleTreeNodeDto roleTreeNode = getHelper().createRoleTreeNode(role, treeNode, false);
		//
		assertNotNull(roleTreeNode.getId());
		assertEquals(roleTreeNode.getId(), roleTreeNodeService.get(roleTreeNode.getId()).getId());
		//
		getHelper().deleteRole(role.getId());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testReferentialIntegrityOnTreeNode() {
		// prepare data
		IdmRoleDto role = getHelper().createRole();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		// automatic role
		IdmRoleTreeNodeDto roleTreeNode = getHelper().createRoleTreeNode(role, treeNode, false);
		//
		assertNotNull(roleTreeNode.getId());
		assertEquals(roleTreeNode.getId(), roleTreeNodeService.get(roleTreeNode.getId()).getId());
		//
		getHelper().deleteTreeNode(treeNode.getId());
	}
	
	@Test
	public void testReferentialIntegrityOnIdentityDelete() {
		// prepare data
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityWithContract = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().createIdentityContact(identityWithContract);
		getHelper().createContractGuarantee(contract.getId(), identity.getId());
		//
		IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
		filter.setIdentityContractId(contract.getId());
		List<IdmContractGuaranteeDto> guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(1, guarantees.size());
		//
		getHelper().deleteIdentity(identity.getId());
		//
		guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(0, guarantees.size());
	}
	
	@Test
	public void testReferentialIntegrityOnContractDelete() {
		// prepare data
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityWithContract = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().createIdentityContact(identityWithContract);
		getHelper().createContractGuarantee(contract.getId(), identity.getId());
		//
		IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
		filter.setGuaranteeId(identity.getId());
		List<IdmContractGuaranteeDto> guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(1, guarantees.size());
		//
		getHelper().deleteIdentityContact(contract.getId());
		//
		guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(0, guarantees.size());
	}
	
	@Test
	public void testDontCreateContractByConfiguration() {
		configurationService.setBooleanValue(IdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT, false);
		//
		try {
			IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
			assertNull(service.getPrimeContract(identity.getId()));
		} finally {
			configurationService.setBooleanValue(IdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT, true);
		}		
	}
	
	@Test
	public void testSetStateByDateValidInFuture() {
		IdmIdentityContractDto contract = getHelper().createIdentityContact(getHelper().createIdentity((GuardedString) null), null, new LocalDate().plusDays(1), null);
		//
		Assert.assertNull(contract.getState());
		Assert.assertFalse(((IdmIdentityContract) lookupService.lookupEntity(IdmIdentityContractDto.class, contract.getId())).isDisabled());
	}
	
	@Test
	public void testSetStateByDateValidInPast() {
		IdmIdentityContractDto contract = getHelper().createIdentityContact(getHelper().createIdentity((GuardedString) null), null, new LocalDate().plusDays(1), new LocalDate().minusDays(1));
		//
		Assert.assertNull(contract.getState());
		Assert.assertFalse(((IdmIdentityContract) lookupService.lookupEntity(IdmIdentityContractDto.class, contract.getId())).isDisabled());
	}
	
	@Test
	public void textFilterTest(){
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity2 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity3 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity4 = getHelper().createIdentity((GuardedString) null);

		IdmTreeNodeDto node = getHelper().createTreeNode();
		node.setName("Position105");
		treeNodeService.save(node);

		IdmTreeNodeDto node2 = getHelper().createTreeNode();
		node2.setName("Position006");
		treeNodeService.save(node2);

		IdmTreeNodeDto node3 = getHelper().createTreeNode();
		node3.setCode("Position007");
		treeNodeService.save(node3);

		IdmTreeNodeDto node4 = getHelper().createTreeNode();
		node4.setCode("Position108");
		treeNodeService.save(node4);

		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity,node);
		IdmIdentityContractDto contract2 = getHelper().createIdentityContact(identity2,node2);
		IdmIdentityContractDto contract3 = getHelper().createIdentityContact(identity3,node3);
		IdmIdentityContractDto contract4 = getHelper().createIdentityContact(identity4,node4);

		contract.setPosition("Position001");
		contract = service.save(contract);

		contract2.setPosition("Position102");
		service.save(contract2);

		contract3.setPosition("Position103");
		service.save(contract3);

		contract4.setPosition("Position104");
		service.save(contract4);

		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setText("Position00");
		Page<IdmIdentityContractDto> result = service.find(filter,null);
		assertEquals("Wrong Text",3,result.getTotalElements());
		assertTrue(result.getContent().contains(contract));
		assertTrue(result.getContent().contains(contract2));
		assertTrue(result.getContent().contains(contract3));
	}

	@Test
	public void identityFilterTest(){
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		IdmTreeNodeDto node = getHelper().createTreeNode();
		IdmTreeNodeDto node2 = getHelper().createTreeNode();

		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity,node);
		IdmIdentityContractDto contract2 = getHelper().createIdentityContact(identity,node2);

		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setIdentity(identity.getId());
		Page<IdmIdentityContractDto> result = service.find(filter,null);
		assertEquals("Wrong Identity",3,result.getTotalElements());
		assertTrue(result.getContent().contains(service.getPrimeContract(identity.getId())));
		assertTrue(result.getContent().contains(contract));
		assertTrue(result.getContent().contains(contract2));
	}

	@Test
	public void datesValidFilterTest(){
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity2 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity3 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity4 = getHelper().createIdentity((GuardedString) null);

		IdmTreeNodeDto node = getHelper().createTreeNode();
		IdmTreeNodeDto node2 = getHelper().createTreeNode();
		IdmTreeNodeDto node3 = getHelper().createTreeNode();
		IdmTreeNodeDto node4 = getHelper().createTreeNode();

		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity,node, org.joda.time.LocalDate.now(),org.joda.time.LocalDate.parse("2021-06-05"));
		IdmIdentityContractDto contract2 = getHelper().createIdentityContact(identity2,node2,org.joda.time.LocalDate.now(),org.joda.time.LocalDate.parse("2020-05-05"));
		IdmIdentityContractDto contract3 = getHelper().createIdentityContact(identity3,node3,org.joda.time.LocalDate.now(),org.joda.time.LocalDate.parse("2016-05-05"));
		IdmIdentityContractDto contract4 = getHelper().createIdentityContact(identity4,node4,org.joda.time.LocalDate.parse("2018-05-05"),org.joda.time.LocalDate.parse("2025-05-05"));

		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setValidFrom(contract.getValidFrom());
		Page<IdmIdentityContractDto> result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract));

		filter.setValidFrom(null);
		filter.setValidTill(contract2.getValidTill());
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract2));

		filter.setValidTill(null);
		filter.setValid(true);
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract));
		assertTrue(result.getContent().contains(contract2));
		assertFalse(result.getContent().contains(contract3));

		filter.setValid(null);
		filter.setValidNowOrInFuture(true);
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract4));

		filter.setValidNowOrInFuture(false);
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract3));
	}

	@Test
	public void externeFilterTest(){
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity2 = getHelper().createIdentity((GuardedString) null);

		IdmTreeNodeDto node = getHelper().createTreeNode();
		IdmTreeNodeDto node2 = getHelper().createTreeNode();

		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity,node);
		IdmIdentityContractDto contract2 = getHelper().createIdentityContact(identity2,node2);

		contract.setExterne(true);
		service.save(contract);

		contract2.setExterne(false);
		service.save(contract2);

		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setExterne(true);
		Page<IdmIdentityContractDto> result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract));
		assertFalse(result.getContent().contains(contract2));

		filter.setExterne(false);
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract2));
		assertFalse(result.getContent().contains(contract));
	}

	@Test
	public void mainFilterTest() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity2 = getHelper().createIdentity((GuardedString) null);

		IdmTreeNodeDto node = getHelper().createTreeNode();
		IdmTreeNodeDto node2 = getHelper().createTreeNode();

		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity,node);
		IdmIdentityContractDto contract2 = getHelper().createIdentityContact(identity2,node2);

		contract.setMain(true);
		service.save(contract);

		contract2.setMain(false);
		service.save(contract2);

		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setMain(true);
		Page<IdmIdentityContractDto> result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract));
		assertFalse(result.getContent().contains(contract2));

		filter.setMain(false);
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract2));
		assertFalse(result.getContent().contains(contract));
	}
	
	@Test
	public void positionFilterTest() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setPosition(getHelper().createName());
		contract = getHelper().getService(IdmIdentityContractService.class).save(contract);
		//
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setPosition(contract.getPosition());
		Page<IdmIdentityContractDto> results = service.find(filter, null);
		Assert.assertTrue(results.getContent().contains(contract));
		Assert.assertEquals(1, results.getTotalElements());
	}
	
	@Test
	public void workPositionFilterTest() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setWorkPosition(treeNode.getId());
		contract = getHelper().getService(IdmIdentityContractService.class).save(contract);
		//
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setWorkPosition(treeNode.getId());
		Page<IdmIdentityContractDto> results = service.find(filter, null);
		Assert.assertTrue(results.getContent().contains(contract));
		Assert.assertEquals(1, results.getTotalElements());
	}
	
	@Test
	public void testFindAllValidContracts() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		List<IdmIdentityContractDto> contracts = service.findAllValidForDate(identity.getId(), LocalDate.now(), false);
		Assert.assertEquals(1, contracts.size());
		//
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setState(ContractState.DISABLED);
		contract = service.save(contract);
		//
		contracts = service.findAllValidForDate(identity.getId(), LocalDate.now(), false);
		Assert.assertEquals(0, contracts.size());
		//
		// invalid
		getHelper().createIdentityContact(identity, null, LocalDate.now().plusDays(1), null);
		contracts = service.findAllValidForDate(identity.getId(), LocalDate.now(), false);
		Assert.assertEquals(0, contracts.size());
		contracts = service.findAllValidForDate(identity.getId(), LocalDate.now().plusDays(1), false);
		Assert.assertEquals(1, contracts.size());
		//
		// externe
		contracts = service.findAllValidForDate(identity.getId(), LocalDate.now(), true);
		Assert.assertEquals(0, contracts.size());
		contract.setExterne(true);
		contract.setState(ContractState.EXCLUDED);
		contract = service.save(contract);
		contracts = service.findAllValidForDate(identity.getId(), LocalDate.now(), true);
		Assert.assertEquals(1, contracts.size());
	}
	
	@Test
	public void testFindExcludedContracts() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setState(ContractState.EXCLUDED);
		contract = service.save(contract);
		IdmIdentityContractDto disabled = getHelper().createIdentityContact(identity);
		disabled.setState(ContractState.DISABLED);
		contract = service.save(contract);
		IdmIdentityContractDto valid = getHelper().createIdentityContact(identity);
		//
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setIdentity(identity.getId());
		filter.setExcluded(Boolean.TRUE);
		List<IdmIdentityContractDto> contracts = service.find(filter, null).getContent();
		//
		Assert.assertEquals(1, contracts.size());
		Assert.assertEquals(contract.getId(), contracts.get(0).getId());
		//
		filter.setExcluded(Boolean.FALSE);
		contracts = service.find(filter, null).getContent();
		Assert.assertEquals(2, contracts.size());
		Assert.assertTrue(contracts.stream().anyMatch(c -> c.getId().equals(disabled.getId())));
		Assert.assertTrue(contracts.stream().anyMatch(c -> c.getId().equals(valid.getId())));
	}
	
	@Test
	public void testDisableIdentityAfterExcludeContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		Assert.assertFalse(identity.isDisabled());
		//
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setState(ContractState.EXCLUDED);
		service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
	}
	
	@Test
	public void testEnableIdentityMoreContracts() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		getHelper().createIdentityContact(identity);
		Assert.assertFalse(identity.isDisabled());
		//
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setState(ContractState.EXCLUDED);
		service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertFalse(identity.isDisabled());
	}
	
	@Test
	public void testEnableIdentityAfterIncludeContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setState(ContractState.EXCLUDED);
		contract = service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
		//
		contract.setState(null);
		contract = service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertFalse(identity.isDisabled());
	}
	
	@Test
	public void testEnableIdentityAfterEnableContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setState(ContractState.DISABLED);
		contract = service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
		//
		contract.setState(null);
		contract = service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertFalse(identity.isDisabled());
	}
	
	@Test
	public void testDisableIdentityAfterDisableContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setState(ContractState.DISABLED);
		contract = service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
	}
	
	@Test
	public void testDisableIdentityAfterInvalidateContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setValidFrom(LocalDate.now().plusDays(1));
		contract = service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
	}
	
	@Test
	public void testEnableIdentityAfterValidateContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setValidFrom(LocalDate.now().plusDays(1));
		contract = service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
		//
		contract.setValidFrom(LocalDate.now());
		service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertFalse(identity.isDisabled());
	}
	
	@Test
	public void testDisableIdentityAfterDeleteContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		service.delete(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());	
	}
	
	@Test
	public void testEnableIdentityAfterCreateValidContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		service.delete(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
		//
		getHelper().createIdentityContact(identity);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertFalse(identity.isDisabled());
	}
	
	@Test
	public void testDisableIdentityAfterCreateInvalidContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		service.delete(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
		//
		getHelper().createIdentityContact(identity, null, LocalDate.now().plusDays(1), null);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
	}
	
	@Test
	public void testDontAssingRoleForDisabledContractWhenPositionIsChanged() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contractD = new IdmIdentityContractDto();
		contractD.setIdentity(identity.getId());
		contractD.setState(ContractState.DISABLED);
		contractD = service.save(contractD);
		//
		// create new automatic role
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.NO);
		automaticRoleD.setRole(roleA.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = saveAutomaticRole(automaticRoleD, true);
		//
		contractD.setWorkPosition(nodeD.getId());
		contractD = service.save(contractD);
		//
		// check
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contractD.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testDontAssingRoleForInvalidContractWhenPositionIsChanged() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contractD = new IdmIdentityContractDto();
		contractD.setIdentity(identity.getId());
		contractD.setValidTill(LocalDate.now().minusDays(1));
		contractD = service.save(contractD);
		//
		// create new automatic role
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.NO);
		automaticRoleD.setRole(roleA.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = saveAutomaticRole(automaticRoleD, true);
		//
		contractD.setWorkPosition(nodeD.getId());
		contractD = service.save(contractD);
		//
		// check
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contractD.getId());
		assertEquals(0, identityRoles.size());
	}
}
