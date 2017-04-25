package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.model.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for identity service find managers and role.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdentityFindPositionsTest extends AbstractIntegrationTest{

	@Autowired
	private IdmIdentityRepository identityRepository;	
	@Autowired
	private IdmIdentityService identityService;	
	@Autowired
	private IdmTreeNodeService treeNodeService;	
	@Autowired
	private IdmTreeTypeService treeTypeService;	
	@Autowired
	private IdmIdentityContractService identityContractService;	
	@Autowired
	private IdmContractGuaranteeService contractGuaranteeService;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Before
	public void init() {
		loginAsAdmin("admin");
	}
	
	@After
	public void deleteIdentity() {
		logout();
	}
	
	@Test
	public void findUser() {
		IdmIdentity identity = createAndSaveIdentity("test_identity");
		identity = identityRepository.save(identity);
		
		IdmIdentity foundIdentity = this.identityService.get(identity.getId());
		
		assertEquals(identity, foundIdentity);
	}
	
	@Test
	@Transactional
	public void findGuarantee() {
		IdmIdentity user = createAndSaveIdentity("test_find_managers_user");
		
		IdmIdentity quarantee1 = createAndSaveIdentity("test_find_managers_manager");
		
		IdmIdentity quarantee2 = createAndSaveIdentity("test_find_managers_manager2");
		
		createIdentityContract(user, quarantee1, null);
		
		createIdentityContract(user, quarantee2, null);
		
		List<IdmIdentity> result = identityService.findAllManagers(user, null);
		
		assertEquals(2, result.size());
	}
	
	@Test
	public void findManagers() {
		IdmIdentity user = createAndSaveIdentity("test_position_01");
		IdmIdentity user2 = createAndSaveIdentity("test_position_02");
		IdmIdentity user3 = createAndSaveIdentity("test_position_03");
		IdmIdentity user4 = createAndSaveIdentity("test_position_04");
		
		IdmTreeType treeTypeFirst = new IdmTreeType();
		treeTypeFirst.setCode("TEST_TYPE_CODE_FIRST");
		treeTypeFirst.setName("TEST_TYPE_NAME_FIRST");
		treeTypeService.save(treeTypeFirst);
		
		IdmTreeType treeTypeSecond = new IdmTreeType();
		treeTypeSecond.setCode("TEST_TYPE_CODE_SECOND");
		treeTypeSecond.setName("TEST_TYPE_NAME_SECOND");
		treeTypeService.save(treeTypeSecond);
		
		// create root for second type
		IdmTreeNode nodeRootSec = new IdmTreeNode();
		nodeRootSec.setName("TEST_NODE_NAME_ROOT_SEC");
		nodeRootSec.setCode("TEST_NODE_CODE_ROOT_SEC");
		nodeRootSec.setTreeType(treeTypeSecond);
		treeNodeService.save(nodeRootSec);
		
		// create root for first type
		IdmTreeNode nodeRoot = new IdmTreeNode();
		nodeRoot.setName("TEST_NODE_NAME_ROOT");
		nodeRoot.setCode("TEST_NODE_CODE_ROOT");
		nodeRoot.setTreeType(treeTypeFirst);
		treeNodeService.save(nodeRoot);
		
		// create one for first type
		IdmTreeNode nodeOne = new IdmTreeNode();
		nodeOne.setName("TEST_NODE_NAME_ONE");
		nodeOne.setCode("TEST_NODE_CODE_ONE");
		nodeOne.setParent(nodeRoot);
		nodeOne.setTreeType(treeTypeFirst);
		treeNodeService.save(nodeOne);
		
		// create two for first type
		IdmTreeNode nodeTwo = new IdmTreeNode();
		nodeTwo.setName("TEST_NODE_NAME_TWO");
		nodeTwo.setCode("TEST_NODE_CODE_TWO");
		nodeTwo.setParent(nodeOne);
		nodeTwo.setTreeType(treeTypeFirst);
		treeNodeService.save(nodeTwo);
		
		createIdentityContract(user, null, nodeRoot);
		createIdentityContract(user2, null, nodeOne);
		createIdentityContract(user3, null, nodeOne);
		createIdentityContract(user4, null, nodeTwo);
		// createIdentityContract(user, manager3, null);
		
		List<IdmIdentity> managersList = identityService.findAllManagers(user3, treeTypeFirst);
		assertEquals(1, managersList.size());
		
		IdmIdentity manager = managersList.get(0);
		assertEquals(user.getId(), manager.getId());
		
		managersList = identityService.findAllManagers(user4, treeTypeFirst);
		assertEquals(2, managersList.size());
		
		managersList = identityService.findAllManagers(user, treeTypeFirst);
		assertEquals(1, managersList.size());
		
		createIdentityContract(user, null, nodeTwo);
		managersList = identityService.findAllManagers(user, treeTypeFirst);
		assertEquals(2, managersList.size());
		
		List<IdmIdentity> managersListSec = identityService.findAllManagers(user, treeTypeSecond);
		
		// user with superAdminRole
		assertEquals(1, managersListSec.size());
	}
	
	@Test
	public void managerNotFound() {
		IdmIdentity user = createAndSaveIdentity("test_2");
		
		List<IdmIdentity> result = identityService.findAllManagers(user, null);
		
		assertEquals(1, result.size());
		
		IdmIdentity admin = result.get(0);
		
		assertNotNull(admin);
	}
	
	@Transactional
	private void deleteAllUser () {
		for	(IdmIdentity user : this.identityRepository.findAll()) {
			identityRepository.delete(user);
		}
	}
	
	private IdmIdentityContract createIdentityContract(IdmIdentity user, IdmIdentity guarantee, IdmTreeNode node) {
		IdmIdentityContract position = new IdmIdentityContract();
		position.setIdentity(user);
		position.setWorkPosition(node);
		
		position = identityContractService.save(position);
		
		if (guarantee != null) {
			contractGuaranteeService.save(new IdmContractGuaranteeDto(position.getId(), guarantee.getId()));
		}
		
		return position;
	}
	
	private IdmIdentity createAndSaveIdentity(String userName) {
		IdmIdentity user = constructTestIdentity();
		user.setUsername(userName);
		return identityRepository.save(user);
	}
	
	private IdmIdentity constructTestIdentity() {
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername("service_test_user");
		identity.setLastName("Service");
		return identity;
	}
}
