package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.*;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRoleValidRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Integration test for {@link IdmIdentityRoleValidRequest}
 * Delete:
 * - Role
 * - Identity
 * - IdentityRole
 * - IdentityContract
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdentityRoleValidRequestIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	private IdmRoleService roleService;
	
	@Autowired
	private IdmTreeNodeService treeNodeService;
	
	@Autowired
	private IdmTreeTypeService treeTypeService;
	
	@Autowired
	private IdmIdentityContractService identityContractService;
	
	@Autowired
	private IdmIdentityRoleService idmIdentityRoleSerivce;
	
	@Autowired
	private IdmIdentityRoleValidRequestService identityRoleValidRequestService;
	
	@Before
	public void loginAndInit() {
		loginAsAdmin("admin");
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void deleteIdentity() {
		IdmIdentity identity = createAndSaveIdentity();
		IdmRole role = createAndSaveRole();
		IdmTreeType treeType = createAndSaveTreeType();
		IdmTreeNode treeNode = createAndSaveTreeNode(treeType);
		IdmIdentityContract identityContract = createAndSaveIdentityContract(identity, treeNode);
		LocalDate from = new LocalDate();
		from = from.plusDays(5);
		createAndSaveIdentityRole(identityContract, role, null, from);
		
		List<IdmIdentityRoleValidRequestDto> list = identityRoleValidRequestService.findDto(null).getContent();
		int size = list.size();
		
		identityService.delete(identity);
		
		list = identityRoleValidRequestService.findDto(null).getContent();
		
		assertNotEquals(size, list.size());
		list = identityRoleValidRequestService.findAllValidRequestForIdentityId(identity.getId());
		assertEquals(true, list.isEmpty());
	}
	
	@Test(expected = ResultCodeException.class)
	public void deleteRole() {
		IdmIdentity identity = createAndSaveIdentity();
		IdmRole role = createAndSaveRole();
		IdmTreeType treeType = createAndSaveTreeType();
		IdmTreeNode treeNode = createAndSaveTreeNode(treeType);
		IdmIdentityContract identityContract = createAndSaveIdentityContract(identity, treeNode);
		LocalDate from = new LocalDate();
		from = from.plusDays(5);
		createAndSaveIdentityRole(identityContract, role, null, from);
		
		List<IdmIdentityRoleValidRequestDto> list = identityRoleValidRequestService.findDto(null).getContent();
		int size = list.size();
		
		// role has identity, ok - throw error
		roleService.delete(role);
		
		list = identityRoleValidRequestService.findDto(null).getContent();
		
		assertNotEquals(size, list.size());
		list = identityRoleValidRequestService.findAllValidRequestForRoleId(role.getId());
		assertEquals(true, list.isEmpty());
	}
	
	@Test
	public void deleteIdentityContract() {
		IdmIdentity identity = createAndSaveIdentity();
		IdmRole role = createAndSaveRole();
		IdmTreeType treeType = createAndSaveTreeType();
		IdmTreeNode treeNode = createAndSaveTreeNode(treeType);
		IdmIdentityContract identityContract = createAndSaveIdentityContract(identity, treeNode);
		LocalDate from = new LocalDate();
		from = from.plusDays(5);
		createAndSaveIdentityRole(identityContract, role, null, from);
		
		List<IdmIdentityRoleValidRequestDto> list = identityRoleValidRequestService.findDto(null).getContent();
		int size = list.size();
		
		identityContractService.delete(identityContract);
		
		list = identityRoleValidRequestService.findDto(null).getContent();
		
		assertNotEquals(size, list.size());
		list = identityRoleValidRequestService.findAllValidRequestForIdentityContractId(identityContract.getId());
		assertEquals(true, list.isEmpty());
	}
	
	@Test
	public void deleteIdentityRole() {
		IdmIdentity identity = createAndSaveIdentity();
		IdmRole role = createAndSaveRole();
		IdmTreeType treeType = createAndSaveTreeType();
		IdmTreeNode treeNode = createAndSaveTreeNode(treeType);
		IdmIdentityContract identityContract = createAndSaveIdentityContract(identity, treeNode);
		LocalDate from = new LocalDate();
		from = from.plusDays(5);
		IdmIdentityRole identityRole = createAndSaveIdentityRole(identityContract, role, null, from);
		
		List<IdmIdentityRoleValidRequestDto> list = identityRoleValidRequestService.findDto(null).getContent();
		int size = list.size();
		
		idmIdentityRoleSerivce.delete(identityRole);
		
		list = identityRoleValidRequestService.findDto(null).getContent();
		
		assertNotEquals(size, list.size());
		list = identityRoleValidRequestService.findAllValidRequestForIdentityRoleId(identityRole.getId());
		assertEquals(true, list.isEmpty());
	}
	
	private IdmIdentity createAndSaveIdentity() {
		IdmIdentity entity = new IdmIdentity();
		entity.setUsername("valid_identity_" + System.currentTimeMillis());
		entity.setLastName("valid_last_name");
		return saveInTransaction(entity, identityService);
	}
	
	private IdmRole createAndSaveRole() {
		IdmRole entity = new IdmRole();
		entity.setName("valid_role_" + System.currentTimeMillis());
		return saveInTransaction(entity, roleService);
	}

	
	private IdmTreeType createAndSaveTreeType() {
		IdmTreeType entity = new IdmTreeType();
		entity.setName("valid_tree_type_" + System.currentTimeMillis());
		entity.setCode("valid_tree_type_" + System.currentTimeMillis());
		return saveInTransaction(entity, treeTypeService);
	}
	
	private IdmTreeNode createAndSaveTreeNode(IdmTreeType treeType) {
		IdmTreeNode entity = new IdmTreeNode();
		entity.setCode("valid_tree_node_" + System.currentTimeMillis());
		entity.setName("valid_tree_node_" + System.currentTimeMillis());
		entity.setTreeType(treeType);
		return saveInTransaction(entity, treeNodeService);
	}
	
	private IdmIdentityContract createAndSaveIdentityContract(IdmIdentity user, IdmTreeNode node) {
		IdmIdentityContract entity = new IdmIdentityContract();
		entity.setIdentity(user);
		entity.setWorkPosition(node);
		return saveInTransaction(entity, identityContractService);
	}
	
	private IdmIdentityRole createAndSaveIdentityRole(IdmIdentityContract identityContract, IdmRole role, LocalDate validTill, LocalDate validFrom) {
		IdmIdentityRole entity = new IdmIdentityRole();
		entity.setValidTill(validTill);
		entity.setValidFrom(validFrom);
		entity.setRole(role);
		entity.setIdentityContract(identityContract);
		return saveInTransaction(entity, idmIdentityRoleSerivce);
	}
}
