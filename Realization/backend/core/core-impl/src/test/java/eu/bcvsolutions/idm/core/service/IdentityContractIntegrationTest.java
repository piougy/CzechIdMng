package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
import eu.bcvsolutions.idm.core.model.repository.IdmRoleTreeNodeRepository;
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
	@Autowired
	private IdmRoleTreeNodeRepository roleTreeNodeRepository;
	//
	private IdmTreeType treeType;
	private IdmTreeNode nodeA;
	private IdmTreeNode nodeB;
	private IdmTreeNode nodeC;
	private IdmTreeNode nodeD;
	private IdmTreeNode nodeE;
	private IdmTreeNode nodeF;
	private IdmRole roleA;
	private IdmRole roleB;
	private IdmRole roleC;
	private IdmRole roleD;
	private IdmRoleTreeNodeDto automaticRoleA;
	private IdmRoleTreeNodeDto automaticRoleD;
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_USER_1);
		prepareTreeStructureAndRoles();
	}
	
	@After 
	public void logout() {
		roleTreeNodeRepository.findAll().forEach(entity -> {
			roleTreeNodeService.deleteInternalById(entity.getId());
		});
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
		
		IdmRoleTreeNodeDto automaticRoleF = new IdmRoleTreeNodeDto();
		automaticRoleF.setRecursionType(RecursionType.UP);
		automaticRoleF.setRole(roleC.getId());
		automaticRoleF.setTreeNode(nodeF.getId());
		automaticRoleF = roleTreeNodeService.save(automaticRoleF);
		
		IdmRoleTreeNodeDto automaticRoleE = new IdmRoleTreeNodeDto();
		automaticRoleE.setRecursionType(RecursionType.NO);
		automaticRoleE.setRole(roleD.getId());
		automaticRoleE.setTreeNode(nodeE.getId());
		automaticRoleE = roleTreeNodeService.save(automaticRoleE);
	}
	
	@Test
	public void testFindAutomaticRoleWithoutRecursion() {
		// prepare
		IdmRoleTreeNodeDto automaticRoleNoRecursion = new IdmRoleTreeNodeDto();
		automaticRoleNoRecursion.setRecursionType(RecursionType.NO);
		automaticRoleNoRecursion.setRole(roleA.getId());
		automaticRoleNoRecursion.setTreeNode(nodeD.getId());
		automaticRoleNoRecursion = roleTreeNodeService.save(automaticRoleNoRecursion);
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
		IdmRoleTreeNodeDto automaticRoleWithRecursion = new IdmRoleTreeNodeDto();
		automaticRoleWithRecursion.setRecursionType(RecursionType.DOWN);
		automaticRoleWithRecursion.setRole(roleA.getId());
		automaticRoleWithRecursion.setTreeNode(nodeD.getId());
		automaticRoleWithRecursion = roleTreeNodeService.save(automaticRoleWithRecursion);
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
		IdmRoleTreeNodeDto automaticRoleWithRecursion = new IdmRoleTreeNodeDto();
		automaticRoleWithRecursion.setRecursionType(RecursionType.UP);
		automaticRoleWithRecursion.setRole(roleA.getId());
		automaticRoleWithRecursion.setTreeNode(nodeD.getId());
		automaticRoleWithRecursion = roleTreeNodeService.save(automaticRoleWithRecursion);
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
		contract.setWorkingPosition(nodeD);
		contract.setMain(true);
		contract.setDescription("test-node-d");
		identityContractService.save(contract);
		contract = identityContractService.getPrimeContract(identity);
		//
		// test after create
		assertEquals(nodeD, contract.getWorkingPosition());
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
		contract.setWorkingPosition(nodeD);
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
		contract.setWorkingPosition(nodeD);
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
		contract.setWorkingPosition(nodeE);
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
	
	/**
	 * When contract already exist and automatic role is added, then nothing is done for now
	 * TODO: add automatic role?
	 */
	@Test
	public void testAddAutomaticRoleWithContractAlreadyExists() {
		// prepare identity and contract
		IdmIdentity identity = helper.createIdentity("test");
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		contract.setWorkingPosition(nodeD);
		contract = identityContractService.save(contract);
		//
		List<IdmIdentityRole> identityRoles = identityRoleService.getRoles(contract);
		assertTrue(identityRoles.isEmpty());
		//
		prepareAutomaticRoles();
		//
		identityRoles = identityRoleService.getRoles(contract);
		assertTrue(identityRoles.isEmpty());
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
		contract.setWorkingPosition(nodeC);
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
		IdmRoleTreeNodeDto automaticRoleF = new IdmRoleTreeNodeDto();
		automaticRoleF.setRecursionType(RecursionType.UP);
		automaticRoleF.setRole(roleA.getId());
		automaticRoleF.setTreeNode(nodeF.getId());
		automaticRoleF = roleTreeNodeService.save(automaticRoleF);
		//
		IdmRoleTreeNodeDto automaticRoleE = new IdmRoleTreeNodeDto();
		automaticRoleE.setRecursionType(RecursionType.NO);
		automaticRoleE.setRole(roleA.getId());
		automaticRoleE.setTreeNode(nodeE.getId());
		automaticRoleE = roleTreeNodeService.save(automaticRoleE);
		//
		// prepare identity and contract
		IdmIdentity identity = helper.createIdentity("test");
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		contract.setWorkingPosition(nodeF);
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
		contract.setWorkingPosition(nodeE);
		contract = identityContractService.save(contract);
		//
		// check assigned role after creation 
		identityRoles = identityRoleService.getRoles(contract);
		assertEquals(1, identityRoles.size());
		assertEquals(roleA, identityRoles.get(0).getRole());
		assertEquals(automaticRoleE.getId(), identityRoles.get(0).getRoleTreeNode().getId());
		assertEquals(id, identityRoles.get(0).getId());
	}
}
