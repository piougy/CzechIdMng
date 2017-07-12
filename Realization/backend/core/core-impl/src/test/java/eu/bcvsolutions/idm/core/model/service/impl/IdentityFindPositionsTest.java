package eu.bcvsolutions.idm.core.model.service.impl;

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

import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
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
		IdmIdentityDto identity = createAndSaveIdentity("test_identity");
		
		IdmIdentityDto foundIdentity = this.identityService.get(identity.getId());
		
		assertEquals(identity, foundIdentity);
	}
	
	@Test
	@Transactional
	public void findGuarantee() {
		IdmIdentityDto user = createAndSaveIdentity("test_find_managers_user");
		
		IdmIdentityDto quarantee1 = createAndSaveIdentity("test_find_managers_manager");
		
		IdmIdentityDto quarantee2 = createAndSaveIdentity("test_find_managers_manager2");
		
		createIdentityContract(user, quarantee1, null);
		
		createIdentityContract(user, quarantee2, null);
		
		List<IdmIdentityDto> result = identityService.findAllManagers(user.getId(), null);
		
		assertEquals(2, result.size());
	}
	
	@Test
	public void findManagers() {
		IdmIdentityDto user = createAndSaveIdentity("test_position_01");
		IdmIdentityDto user2 = createAndSaveIdentity("test_position_02");
		IdmIdentityDto user3 = createAndSaveIdentity("test_position_03");
		IdmIdentityDto user4 = createAndSaveIdentity("test_position_04");
		
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
		
		List<IdmIdentityDto> managersList = identityService.findAllManagers(user3.getId(), treeTypeFirst.getId());
		assertEquals(1, managersList.size());
		
		IdmIdentityDto manager = managersList.get(0);
		assertEquals(user.getId(), manager.getId());
		
		managersList = identityService.findAllManagers(user4.getId(), treeTypeFirst.getId());
		assertEquals(2, managersList.size());
		
		managersList = identityService.findAllManagers(user.getId(), treeTypeFirst.getId());
		assertEquals(1, managersList.size());
		
		createIdentityContract(user, null, nodeTwo);
		managersList = identityService.findAllManagers(user.getId(), treeTypeFirst.getId());
		assertEquals(2, managersList.size());
		
		List<IdmIdentityDto> managersListSec = identityService.findAllManagers(user.getId(), treeTypeSecond.getId());
		
		// user with superAdminRole
		assertEquals(1, managersListSec.size());
	}
	
	@Test
	public void managerNotFound() {
		IdmIdentityDto user = createAndSaveIdentity("test_2");
		
		List<IdmIdentityDto> result = identityService.findAllManagers(user.getId(), null);
		
		assertEquals(1, result.size());
		
		IdmIdentityDto admin = result.get(0);
		
		assertNotNull(admin);
	}
	
	@Transactional
	private void deleteAllUser () {
		for	(IdmIdentity user : this.identityRepository.findAll()) {
			identityRepository.delete(user);
		}
	}
	
	private IdmIdentityContractDto createIdentityContract(IdmIdentityDto user, IdmIdentityDto guarantee, IdmTreeNode node) {
		IdmIdentityContractDto position = new IdmIdentityContractDto();
		position.setIdentity(user.getId());
		position.setWorkPosition(node == null ? null : node.getId());
		
		position = identityContractService.save(position);
		
		if (guarantee != null) {
			contractGuaranteeService.save(new IdmContractGuaranteeDto(position.getId(), guarantee.getId()));
		}
		
		return position;
	}
	
	private IdmIdentityDto createAndSaveIdentity(String userName) {
		IdmIdentityDto user = constructTestIdentity();
		user.setUsername(userName);
		return identityService.save(user);
	}
	
	private IdmIdentityDto constructTestIdentity() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername("service_test_user");
		identity.setLastName("Service");
		return identity;
	}
}
