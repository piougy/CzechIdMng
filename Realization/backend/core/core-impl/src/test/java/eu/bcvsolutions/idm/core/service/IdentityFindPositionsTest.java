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

import eu.bcvsolutions.idm.core.AbstractIntegrationTest;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.IdmTreeTypeService;

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
	private IdmIdentityContractRepository identityContractRepository;
	
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
		
		IdmIdentity manager1 = createAndSaveIdentity("test_find_managers_manager");
		
		IdmIdentity manager2 = createAndSaveIdentity("test_find_managers_manager2");
		
		createIdentityContract(user, manager1, null);
		
		createIdentityContract(user, manager2, null);
		
		List<IdmIdentity> result = identityService.findAllManagers(user, null);
		
		assertEquals(2, result.size());

		String resutlString = identityService.findAllManagersAsString(user.getId());
		
		assertEquals(true, resutlString.contains(manager1.getUsername()));
		assertEquals(true, resutlString.contains(manager2.getUsername()));
	}
	
	@Test
	public void findManagers() {
		IdmIdentity user = createAndSaveIdentity("test_type_01");
		IdmIdentity manager = createAndSaveIdentity("test_type_manager_01");
		IdmIdentity manager2 = createAndSaveIdentity("test_type_manager_02");
		IdmIdentity manager3 = createAndSaveIdentity("test_type_manager_03");
		
		IdmTreeType treeType = new IdmTreeType();
		treeType.setCode("TEST_TYPE_CODE");
		treeType.setName("TEST_TYPE_NAME");
		treeTypeService.save(treeType);
		
		IdmTreeNode node = new IdmTreeNode();
		node.setName("TEST_NODE_NAME");
		node.setCode("TEST_NODE_CODE");
		node.setTreeType(treeType);
		treeNodeService.save(node);
		
		createIdentityContract(user, manager, node);
		createIdentityContract(user, manager2, node);
		createIdentityContract(user, manager3, node);
		createIdentityContract(user, manager3, null);
		
		List<IdmIdentity> managersList = identityService.findAllManagers(user, null);

		assertEquals(3, managersList.size());
		
		// TODO: findAllmanagers by tree structure.
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
	
	private IdmIdentityContract createIdentityContract(IdmIdentity user, IdmIdentity manager, IdmTreeNode node) {
		IdmIdentityContract position = new IdmIdentityContract();
		position.setIdentity(user);
		position.setGuarantee(manager);
		position.setWorkingPosition(node);
		
		return identityContractRepository.save(position);
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
