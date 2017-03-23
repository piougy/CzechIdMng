package eu.bcvsolutions.idm.core.service;

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
import eu.bcvsolutions.idm.core.model.domain.RecursionType;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test with identity contracts:
 * - work with assigned roles, when contract is changed (disable, etc.)
 * - automatic role is defined, changed
 * - expiration
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityContractIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired 
	protected TestHelper helper;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmRoleTreeNodeService roleTreeNodeService;
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
		if(automaticRoleA != null) try { roleTreeNodeService.delete(automaticRoleA); } catch (EmptyResultDataAccessException ex) {} ;
		if(automaticRoleD != null) try { roleTreeNodeService.delete(automaticRoleD); } catch (EmptyResultDataAccessException ex) {} ;
		if(automaticRoleE != null) try { roleTreeNodeService.delete(automaticRoleE); } catch (EmptyResultDataAccessException ex) {} ;
		if(automaticRoleF != null) try { roleTreeNodeService.delete(automaticRoleF); } catch (EmptyResultDataAccessException ex) {} ;
		//
		super.logout();
	}	
	
	private void prepareTreeStructureAndRoles() {
		// tree type
		treeType = helper.createTreeType("test");
		// four levels
		// https://proj.bcvsolutions.eu/ngidm/doku.php?id=roztridit:standardni_procesy#role_pridelovane_na_zaklade_zarazeni_v_organizacni_strukture
		nodeA = helper.createTreeNode(treeType, "A", null);
		nodeB = helper.createTreeNode(treeType, "B", nodeA);
		nodeC = helper.createTreeNode(treeType, "C", nodeB);
		nodeD = helper.createTreeNode(treeType, "D", nodeB);
		nodeE = helper.createTreeNode(treeType, "E", nodeD);
		nodeF = helper.createTreeNode(treeType, "F", nodeD);
		// create roles
		roleA = helper.createRole("A");
		roleB = helper.createRole("B");
		roleC = helper.createRole("C");
		roleD = helper.createRole("D");
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
		automaticRoleA = roleTreeNodeService.save(automaticRoleA);	
		
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.DOWN);
		automaticRoleD.setRole(roleB.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = roleTreeNodeService.save(automaticRoleD);
		
		automaticRoleF = new IdmRoleTreeNodeDto();
		automaticRoleF.setRecursionType(RecursionType.UP);
		automaticRoleF.setRole(roleC.getId());
		automaticRoleF.setTreeNode(nodeF.getId());
		automaticRoleF = roleTreeNodeService.save(automaticRoleF);
		
		automaticRoleE = new IdmRoleTreeNodeDto();
		automaticRoleE.setRecursionType(RecursionType.NO);
		automaticRoleE.setRole(roleD.getId());
		automaticRoleE.setTreeNode(nodeE.getId());
		automaticRoleE = roleTreeNodeService.save(automaticRoleE);
	}
	
	@Test
	public void testFindAutomaticRoleWithoutRecursion() {
		// prepare
		automaticRoleA = new IdmRoleTreeNodeDto();
		automaticRoleA.setRecursionType(RecursionType.NO);
		automaticRoleA.setRole(roleA.getId());
		automaticRoleA.setTreeNode(nodeD.getId());
		automaticRoleA = roleTreeNodeService.save(automaticRoleA);
		//
		// test
		Set<IdmRoleTreeNode> automaticRoles = roleTreeNodeService.getAutomaticRoles(nodeD);
		assertEquals(1, automaticRoles.size());
		assertEquals(roleA, automaticRoles.iterator().next().getRole());
		assertTrue(roleTreeNodeService.getAutomaticRoles(nodeB).isEmpty());
		assertTrue(roleTreeNodeService.getAutomaticRoles(nodeF).isEmpty());
	}
	
	@Test
	public void testFindAutomaticRoleWithRecursionDown() {
		// prepare
		automaticRoleA = new IdmRoleTreeNodeDto();
		automaticRoleA.setRecursionType(RecursionType.DOWN);
		automaticRoleA.setRole(roleA.getId());
		automaticRoleA.setTreeNode(nodeD.getId());
		automaticRoleA = roleTreeNodeService.save(automaticRoleA);
		//
		// test
		Set<IdmRoleTreeNode> automaticRoles = roleTreeNodeService.getAutomaticRoles(nodeD);
		assertEquals(1, automaticRoles.size());
		assertEquals(roleA, automaticRoles.iterator().next().getRole());
		assertTrue(roleTreeNodeService.getAutomaticRoles(nodeB).isEmpty());
		automaticRoles = roleTreeNodeService.getAutomaticRoles(nodeF);
		assertEquals(1, automaticRoles.size());
		assertEquals(roleA, automaticRoles.iterator().next().getRole());
	}
	
	@Test
	public void testFindAutomaticRoleWithRecursionUp() {
		// prepare
		automaticRoleA = new IdmRoleTreeNodeDto();
		automaticRoleA.setRecursionType(RecursionType.UP);
		automaticRoleA.setRole(roleA.getId());
		automaticRoleA.setTreeNode(nodeD.getId());
		automaticRoleA = roleTreeNodeService.save(automaticRoleA);
		//
		// test
		Set<IdmRoleTreeNode> automaticRoles = roleTreeNodeService.getAutomaticRoles(nodeD);
		assertEquals(1, automaticRoles.size());
		assertEquals(roleA, automaticRoles.iterator().next().getRole());
		assertTrue(roleTreeNodeService.getAutomaticRoles(nodeF).isEmpty());
		automaticRoles = roleTreeNodeService.getAutomaticRoles(nodeB);
		assertEquals(1, automaticRoles.size());
		assertEquals(roleA, automaticRoles.iterator().next().getRole());
	}	
	
	@Test
	public void testCRUDContractWithAutomaticRoles() {
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentity identity = helper.createIdentity("test");
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		contract.setValidFrom(new LocalDate().minusDays(1));
		contract.setValidTill(new LocalDate().plusMonths(1));
		contract.setWorkPosition(nodeD);
		contract.setMain(true);
		contract.setDescription("test-node-d");
		identityContractService.save(contract);
		contract = identityContractService.getPrimeContract(identity);
		//
		// test after create
		assertEquals(nodeD, contract.getWorkPosition());
		assertEquals("test-node-d", contract.getDescription());
		List<IdmIdentityRole> identityRoles = identityRoleService.getRoles(contract);
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRole identityRole : identityRoles) {
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
		identityRoles = identityRoleService.getRoles(identity);
		assertEquals(3, identityRoles.size());
		//
		// test after disabled
		contract.setDisabled(true);
		identityContractService.save(contract);
		assertTrue(identityRoleService.getRoles(identity).isEmpty());
		//
		// test after enable
		contract.setDisabled(false);
		identityContractService.save(contract);
		assertEquals(3, identityRoleService.getRoles(identity).size());
		//
		// test after delete
		identityContractService.delete(contract);
		assertTrue(identityRoleService.getRoles(identity).isEmpty());
	}
	
	@Test
	public void testChangeContractValidityWithAssignedRoles() {
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentity identity = helper.createIdentity("test");
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		contract.setValidFrom(new LocalDate().minusDays(1));
		contract.setValidTill(new LocalDate().plusMonths(1));
		contract.setWorkPosition(nodeD);
		contract = identityContractService.save(contract);
		// 
		// test after create
		List<IdmIdentityRole> identityRoles = identityRoleService.getRoles(contract);
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRole identityRole : identityRoles) {
			assertEquals(contract.getValidFrom(), identityRole.getValidFrom());
			assertEquals(contract.getValidTill(), identityRole.getValidTill());
		};
		// test after change
		contract.setValidFrom(new LocalDate().minusDays(2));
		contract.setValidTill(new LocalDate().plusMonths(4));
		contract = identityContractService.save(contract);
		identityRoles = identityRoleService.getRoles(contract);
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRole identityRole : identityRoles) {
			assertEquals(contract.getValidFrom(), identityRole.getValidFrom());
			assertEquals(contract.getValidTill(), identityRole.getValidTill());
		}
	}
	
	@Test
	public void testChangeContractPositionWithAutomaticRolesAssigned() {
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentity identity = helper.createIdentity("test");
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		contract.setWorkPosition(nodeD);
		contract = identityContractService.save(contract);
		// 
		// test after create
		List<IdmIdentityRole> identityRoles = identityRoleService.getRoles(contract);
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRole identityRole : identityRoles) {
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
		contract.setWorkPosition(nodeE);
		contract = identityContractService.save(contract);
		//
		// test after change
		identityRoles = identityRoleService.getRoles(contract);
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRole identityRole : identityRoles) {
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
		//
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentity identity = helper.createIdentity("test");
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		contract.setWorkPosition(nodeC);
		contract = identityContractService.save(contract);
		//
		assertEquals(1, identityRoleService.getRoles(contract).size());
		//
		roleTreeNodeService.delete(automaticRoleD);
		assertEquals(1, identityRoleService.getRoles(contract).size());
		//
		roleTreeNodeService.delete(automaticRoleA);
		assertTrue(identityRoleService.getRoles(contract).isEmpty());
	}
	
	@Test
	public void testDontRemoveSameRole() {
		automaticRoleF = new IdmRoleTreeNodeDto();
		automaticRoleF.setRecursionType(RecursionType.UP);
		automaticRoleF.setRole(roleA.getId());
		automaticRoleF.setTreeNode(nodeF.getId());
		automaticRoleF = roleTreeNodeService.save(automaticRoleF);
		//
		automaticRoleE = new IdmRoleTreeNodeDto();
		automaticRoleE.setRecursionType(RecursionType.NO);
		automaticRoleE.setRole(roleA.getId());
		automaticRoleE.setTreeNode(nodeE.getId());
		automaticRoleE = roleTreeNodeService.save(automaticRoleE);
		//
		// prepare identity and contract
		IdmIdentity identity = helper.createIdentity("test");
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		contract.setWorkPosition(nodeF);
		contract = identityContractService.save(contract);
		//
		// check assigned role after creation 
		List<IdmIdentityRole> identityRoles = identityRoleService.getRoles(contract);
		assertEquals(1, identityRoles.size());
		assertEquals(roleA, identityRoles.get(0).getRole());
		assertEquals(automaticRoleF.getId(), identityRoles.get(0).getRoleTreeNode().getId());
		//
		UUID id = identityRoles.get(0).getId();
		//
		// change
		contract.setWorkPosition(nodeE);
		contract = identityContractService.save(contract);
		//
		// check assigned role after creation 
		identityRoles = identityRoleService.getRoles(contract);
		assertEquals(1, identityRoles.size());
		assertEquals(roleA, identityRoles.get(0).getRole());
		assertEquals(automaticRoleE.getId(), identityRoles.get(0).getRoleTreeNode().getId());
		assertEquals(id, identityRoles.get(0).getId());
	}
	
	@Test
	public void testAssingRoleByNewAutomaticRoleForExistingContracts() {
		// prepare contracts
		IdmIdentity identity = helper.createIdentity("test-exists");
		//
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		contract.setPosition("new position");
		contract = identityContractService.save(contract);
		//
		IdmIdentityContract contractF = new IdmIdentityContract();
		contractF.setIdentity(identity);
		contractF.setWorkPosition(nodeF);
		contractF = identityContractService.save(contractF);
		//
		IdmIdentityContract contractD = new IdmIdentityContract();
		contractD.setIdentity(identity);
		contractD.setWorkPosition(nodeD);
		contractD = identityContractService.save(contractD);
		//
		IdmIdentityContract contractB = new IdmIdentityContract();
		contractB.setIdentity(identity);
		contractB.setWorkPosition(nodeB);
		contractB = identityContractService.save(contractB);
		//
		// create new automatic role
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.DOWN);
		automaticRoleD.setRole(roleA.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = roleTreeNodeService.save(automaticRoleD);
		//
		// check
		List<IdmIdentityRole> identityRoles = identityRoleService.getRoles(contractB);
		assertTrue(identityRoles.isEmpty());
		//
		identityRoles = identityRoleService.getRoles(contractD);
		assertEquals(1, identityRoles.size());
		assertEquals(automaticRoleD.getId(), identityRoles.get(0).getRoleTreeNode().getId());
		//
		identityRoles = identityRoleService.getRoles(contractF);
		assertEquals(1, identityRoles.size());
		assertEquals(automaticRoleD.getId(), identityRoles.get(0).getRoleTreeNode().getId());
	}
	
	@Test
	public void testReferentialIntegrityOnRole() {
		// prepare data
		IdmRole role = helper.createRole();
		IdmTreeNode treeNode = helper.createTreeNode();
		// automatic role
		IdmRoleTreeNodeDto roleTreeNode = helper.createRoleTreeNode(role, treeNode);
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
		IdmRoleTreeNodeDto roleTreeNode = helper.createRoleTreeNode(role, treeNode);
		//
		assertNotNull(roleTreeNode.getId());
		assertEquals(roleTreeNode.getId(), roleTreeNodeService.get(roleTreeNode.getId()).getId());
		//
		helper.deleteTreeNode(treeNode.getId());
		//
		assertNull(roleTreeNodeService.get(roleTreeNode.getId()));		
	}
}
