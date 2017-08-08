package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.TestHelper;
import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.ContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.service.api.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.AddNewAutomaticRoleTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RemoveAutomaticRoleTaskExecutor;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Integration tests with identity contracts:
 * - work with assigned roles, when contract is changed (disable, etc.)
 * - automatic role is defined, changed
 * - expiration
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityContractIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper helper;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private LongRunningTaskManager taskManager;
	@Autowired private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired private ConfigurationService configurationService;
	//
	private IdmTreeType treeType = null;
	private IdmTreeNode nodeA = null;
	private IdmTreeNode nodeB = null;
	private IdmTreeNode nodeC = null;
	private IdmTreeNode nodeD = null;
	private IdmTreeNode nodeE = null;
	private IdmTreeNode nodeF = null;
	private IdmRole roleA = null;
	private IdmRole roleB = null;
	private IdmRole roleC = null;
	private IdmRole roleD = null;
	private IdmRoleTreeNodeDto automaticRoleA = null;
	private IdmRoleTreeNodeDto automaticRoleD = null;
	private IdmRoleTreeNodeDto automaticRoleE = null;
	private IdmRoleTreeNodeDto automaticRoleF = null;
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_USER_1);
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
		treeType = helper.createTreeType();
		// four levels
		// https://proj.bcvsolutions.eu/ngidm/doku.php?id=roztridit:standardni_procesy#role_pridelovane_na_zaklade_zarazeni_v_organizacni_strukture
		nodeA = helper.createTreeNode(treeType, null);
		nodeB = helper.createTreeNode(treeType, nodeA);
		nodeC = helper.createTreeNode(treeType, nodeB);
		nodeD = helper.createTreeNode(treeType, nodeB);
		nodeE = helper.createTreeNode(treeType, nodeD);
		nodeF = helper.createTreeNode(treeType, nodeD);
		// create roles
		roleA = helper.createRole();
		roleB = helper.createRole();
		roleC = helper.createRole();
		roleD = helper.createRole();
	}
	
	/**
	 * Save automatic role with repository and manual create and wait for task
	 * @param automaticRole
	 * @return
	 */
	private IdmRoleTreeNodeDto saveAutomaticRole(IdmRoleTreeNodeDto automaticRole, boolean withLongRunningTask) {
		IdmRoleTreeNodeDto roleTreeNode = roleTreeNodeService.saveInternal(automaticRole);
		//
		if (withLongRunningTask) {
			AddNewAutomaticRoleTaskExecutor task = new AddNewAutomaticRoleTaskExecutor();
			task.setRoleTreeNodeId(roleTreeNode.getId());
			taskManager.executeSync(task);
		}
		//
		return roleTreeNodeService.get(roleTreeNode.getId());
	}
	
	private void deleteAutomaticRole(IdmRoleTreeNodeDto automaticRole) {
		RemoveAutomaticRoleTaskExecutor task = new RemoveAutomaticRoleTaskExecutor();
		task.setRoleTreeNodeId(automaticRole.getId());
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
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(identity.getId());
		contract.setIdentity(identity.getId());
		contract.setValidFrom(new LocalDate().minusDays(1));
		contract.setValidTill(new LocalDate().plusMonths(1));
		contract.setWorkPosition(nodeD.getId());
		contract.setMain(true);
		contract.setDescription("test-node-d");
		identityContractService.save(contract);
		//
		contract = identityContractService.getPrimeContract(identity.getId());
		//
		// test after create
		assertEquals(nodeD.getId(), contract.getWorkPosition());
		assertEquals("test-node-d", contract.getDescription());
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRoleDto identityRole : identityRoles) {
			assertEquals(contract.getValidFrom(), identityRole.getValidFrom());
			assertEquals(contract.getValidTill(), identityRole.getValidTill());
			if (identityRole.getRoleTreeNode().equals(nodeA)) {
				assertEquals(roleA, identityRole.getRole());
			}
			if (identityRole.getRoleTreeNode().equals(nodeD)) {
				assertEquals(roleB, identityRole.getRole());
			}
			if (identityRole.getRoleTreeNode().equals(nodeF)) {
				assertEquals(roleC, identityRole.getRole());
			}
		}
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(3, identityRoles.size());
		//
		// test after delete
		identityContractService.delete(contract);
		assertTrue(identityRoleService.findAllByIdentity(identity.getId()).isEmpty());
	}
	
	@Test
	public void testChangeContractValidityWithAssignedRoles() {
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setValidFrom(new LocalDate().minusDays(1));
		contract.setValidTill(new LocalDate().plusMonths(1));
		contract.setWorkPosition(nodeD.getId());
		contract = identityContractService.save(contract);
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
		contract = identityContractService.save(contract);
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
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setWorkPosition(nodeD.getId());
		contract = identityContractService.save(contract);
		// 
		// test after create
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRoleDto identityRole : identityRoles) {
			if (identityRole.getRoleTreeNode().equals(nodeA)) {
				assertEquals(roleA, identityRole.getRole());
			}
			if (identityRole.getRoleTreeNode().equals(nodeD)) {
				assertEquals(roleB, identityRole.getRole());
			}
			if (identityRole.getRoleTreeNode().equals(nodeF)) {
				assertEquals(roleC, identityRole.getRole());
			}
		}
		//
		contract.setWorkPosition(nodeE.getId());
		contract = identityContractService.save(contract);
		//
		// test after change
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRoleDto identityRole : identityRoles) {
			if (identityRole.getRoleTreeNode().equals(nodeA)) {
				assertEquals(roleA, identityRole.getRole());
			}
			if (identityRole.getRoleTreeNode().equals(nodeD)) {
				assertEquals(roleB, identityRole.getRole());
			}
			if (identityRole.getRoleTreeNode().equals(nodeE)) {
				assertEquals(roleD, identityRole.getRole());
			}
		}
	}
	
	@Test
	public void testDeleteAutomaticRoleWithContractAlreadyExists() {
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setWorkPosition(nodeC.getId());
		contract = identityContractService.save(contract);
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
		IdmIdentityDto identity = helper.createIdentity("test");
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setWorkPosition(nodeF.getId());
		contract = identityContractService.save(contract);
		//
		// check assigned role after creation 
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(1, identityRoles.size());
		assertEquals(roleA.getId(), identityRoles.get(0).getRole());
		assertEquals(automaticRoleF.getId(), identityRoles.get(0).getRoleTreeNode());
		//
		UUID id = identityRoles.get(0).getId();
		//
		// change
		contract.setWorkPosition(nodeE.getId());
		contract = identityContractService.save(contract);
		//
		// check assigned role after creation 
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(1, identityRoles.size());
		assertEquals(roleA.getId(), identityRoles.get(0).getRole());
		assertEquals(automaticRoleE.getId(), identityRoles.get(0).getRoleTreeNode());
		assertEquals(id, identityRoles.get(0).getId());
	}
	
	@Test
	public void testAssingRoleByNewAutomaticRoleForExistingContracts() {
		IdmIdentityDto identity = helper.createIdentity("test-exists");
		//
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setPosition("new position");
		contract = identityContractService.save(contract);
		//
		IdmIdentityContractDto contractF = new IdmIdentityContractDto();
		contractF.setIdentity(identity.getId());
		contractF.setWorkPosition(nodeF.getId());
		contractF = identityContractService.save(contractF);
		//
		IdmIdentityContractDto contractD = new IdmIdentityContractDto();
		contractD.setIdentity(identity.getId());
		contractD.setWorkPosition(nodeD.getId());
		contractD = identityContractService.save(contractD);
		//
		IdmIdentityContractDto contractB = new IdmIdentityContractDto();
		contractB.setIdentity(identity.getId());
		contractB.setWorkPosition(nodeB.getId());
		contractB = identityContractService.save(contractB);
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
		assertEquals(automaticRoleD.getId(), identityRoles.get(0).getRoleTreeNode());
		//
		identityRoles = identityRoleService.findAllByContract(contractF.getId());
		assertEquals(1, identityRoles.size());
		assertEquals(automaticRoleD.getId(), identityRoles.get(0).getRoleTreeNode());
	}
	
	@Test
	public void testReferentialIntegrityOnRole() {
		// prepare data
		IdmRole role = helper.createRole();
		IdmTreeNode treeNode = helper.createTreeNode();
		// automatic role
		IdmRoleTreeNodeDto roleTreeNode = helper.createRoleTreeNode(role, treeNode, false);
		//
		assertNotNull(roleTreeNode.getId());
		assertEquals(roleTreeNode.getId(), roleTreeNodeService.get(roleTreeNode.getId()).getId());
		//
		helper.deleteRole(role.getId());
		//
		assertNull(roleTreeNodeService.get(roleTreeNode.getId()));		
	}
	
	@Test
	public void testReferentialIntegrityOnTreeType() {
		// prepare data
		IdmRole role = helper.createRole();
		IdmTreeNode treeNode = helper.createTreeNode();
		// automatic role
		IdmRoleTreeNodeDto roleTreeNode = helper.createRoleTreeNode(role, treeNode, false);
		//
		assertNotNull(roleTreeNode.getId());
		assertEquals(roleTreeNode.getId(), roleTreeNodeService.get(roleTreeNode.getId()).getId());
		//
		helper.deleteTreeNode(treeNode.getId());
		//
		assertNull(roleTreeNodeService.get(roleTreeNode.getId()));		
	}
	
	@Test
	public void testReferentialIntegrityOnIdentityDelete() {
		// prepare data
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identityWithContract = helper.createIdentity();
		IdmIdentityContractDto contract = helper.createIdentityContact(identityWithContract);
		helper.createContractGuarantee(contract.getId(), identity.getId());
		//
		ContractGuaranteeFilter filter = new ContractGuaranteeFilter();
		filter.setIdentityContractId(contract.getId());
		List<IdmContractGuaranteeDto> guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(1, guarantees.size());
		//
		helper.deleteIdentity(identity.getId());
		//
		guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(0, guarantees.size());
	}
	
	@Test
	public void testReferentialIntegrityOnContractDelete() {
		// prepare data
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identityWithContract = helper.createIdentity();
		IdmIdentityContractDto contract = helper.createIdentityContact(identityWithContract);
		helper.createContractGuarantee(contract.getId(), identity.getId());
		//
		ContractGuaranteeFilter filter = new ContractGuaranteeFilter();
		filter.setGuaranteeId(identity.getId());
		List<IdmContractGuaranteeDto> guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(1, guarantees.size());
		//
		helper.deleteIdentityContact(contract.getId());
		//
		guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(0, guarantees.size());
	}
	
	@Test
	public void testDontCreateContractByConfiguration() {
		configurationService.setBooleanValue(IdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT, false);
		//
		try {
			IdmIdentityDto identity = helper.createIdentity();
			assertNull(identityContractService.getPrimeContract(identity.getId()));
		} finally {
			configurationService.setBooleanValue(IdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT, true);
		}		
	}
}
