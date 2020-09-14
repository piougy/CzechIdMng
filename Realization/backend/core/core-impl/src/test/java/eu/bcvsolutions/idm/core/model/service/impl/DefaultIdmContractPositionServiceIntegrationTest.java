package eu.bcvsolutions.idm.core.model.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.testng.collections.Lists;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractPositionFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ProcessAutomaticRoleByTreeTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Integration tests with contract positions:
 * - work with assigned roles, when contract is changed (disable, etc.)
 * - automatic role is defined, changed
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmContractPositionServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private ApplicationContext context;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private LongRunningTaskManager taskManager;
	@Autowired private IdmIdentityContractService contractService;
	//
	private DefaultIdmContractPositionService service;

	@Before
	public void init() {
		service = context.getAutowireCapableBeanFactory().createBean(DefaultIdmContractPositionService.class);
	}
	
	@Test
	@Transactional
	public void testReferentialIntegritye() {
		// prepare data
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().createContract(identity);
		IdmContractPositionDto contractPosition = getHelper().createContractPosition(contract);
		IdmRoleDto role = getHelper().createRole();
		//
		IdmIdentityRoleDto identityRole = getHelper().createIdentityRole(contractPosition, role);
		Assert.assertNotNull(identityRoleService.get(identityRole));
		//
		service.delete(contractPosition);
		//
		Assert.assertNull(identityRoleService.get(identityRole));
	}
	
	@Test
	@Transactional
	public void testReferentialIntegrityOnContractDelete() {
		// prepare data
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().createContract(identity);
		getHelper().createContractPosition(contract);
		//
		IdmContractPositionFilter positionFilter = new IdmContractPositionFilter();
		positionFilter.setIdentityContractId(contract.getId());
		List<IdmContractPositionDto> positions = service.find(positionFilter, null).getContent();
		Assert.assertEquals(1, positions.size());
		//
		getHelper().deleteContract(contract.getId());
		//
		positions = service.find(positionFilter, null).getContent();
		Assert.assertTrue(positions.isEmpty());
	}
	
	@Test
	@Transactional
	public void testAssignAutomaticRole() {
		IdmRoleDto role = getHelper().createRole();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmRoleTreeNodeDto automaticRole = getHelper().createAutomaticRole(role, treeNode);
		//
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		IdmContractPositionDto position = getHelper().createContractPosition(contract, treeNode);
		getHelper().createContractPosition(contract, getHelper().createTreeNode()); // other
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		Assert.assertEquals(position.getId(), assignedRoles.get(0).getContractPosition());
	}
	
	@Test
	@Transactional
	public void testChangePositionWithAutomaticRolesAssigned() {
		IdmRoleDto role = getHelper().createRole();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmRoleTreeNodeDto automaticRole = getHelper().createAutomaticRole(role, treeNode);
		//
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		IdmContractPositionDto position = getHelper().createContractPosition(contract);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		position.setWorkPosition(treeNode.getId());
		position = service.save(position);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		Assert.assertEquals(position.getId(), assignedRoles.get(0).getContractPosition());
		//
		position.setWorkPosition(null);
		position = service.save(position);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
	}
	
	@Test
	public void testDeleteAutomaticRoleWithContractAlreadyExists() {
		IdmRoleDto role = getHelper().createRole();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmRoleTreeNodeDto automaticRole = getHelper().createAutomaticRole(role, treeNode);
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		IdmContractPositionDto position = getHelper().createContractPosition(contract, treeNode);
		getHelper().createContractPosition(contract, getHelper().createTreeNode()); // other
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		Assert.assertEquals(position.getId(), assignedRoles.get(0).getContractPosition());
		//
		// delete definition
		roleTreeNodeService.delete(automaticRole);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
	}
	
	@Test
	@Transactional
	public void testDontRemoveSameRole() {
		IdmRoleDto role = getHelper().createRole();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmRoleTreeNodeDto automaticRole = getHelper().createAutomaticRole(role, treeNode);
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		IdmContractPositionDto position = getHelper().createContractPosition(contract, treeNode);
		getHelper().createContractPosition(contract, getHelper().createTreeNode()); // other
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		Assert.assertEquals(position.getId(), assignedRoles.get(0).getContractPosition());
		IdmIdentityRoleDto assignedRole = assignedRoles.get(0);
		//
		IdmTreeNodeDto treeNodeOther = getHelper().createTreeNode();
		IdmRoleTreeNodeDto automaticRoleOther = getHelper().createAutomaticRole(role, treeNodeOther);
		//
		// change position
		position.setWorkPosition(treeNodeOther.getId());
		position = service.save(position);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRoleOther.getId(), assignedRoles.get(0).getAutomaticRole());
		Assert.assertEquals(position.getId(), assignedRoles.get(0).getContractPosition());
		Assert.assertEquals(assignedRole.getId(), assignedRoles.get(0).getId());
	}
	
	@Test
	public void testAssingRoleByNewAutomaticRoleForExistingContracts() {
		IdmRoleDto role = getHelper().createRole();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		IdmContractPositionDto position = getHelper().createContractPosition(contract, treeNode);
		getHelper().createContractPosition(contract, getHelper().createTreeNode()); // other
		//
		// TODO: this is really strange ... automatic role is assigned without automatic role id.
		IdmRoleTreeNodeDto automaticRole = new IdmRoleTreeNodeDto();
		automaticRole.setRecursionType(RecursionType.NO);
		automaticRole.setRole(role.getId());
		automaticRole.setTreeNode(treeNode.getId());
		automaticRole = saveAutomaticRole(automaticRole, true);
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		Assert.assertEquals(position.getId(), assignedRoles.get(0).getContractPosition());
	}
	
	private IdmRoleTreeNodeDto saveAutomaticRole(IdmRoleTreeNodeDto automaticRole, boolean withLongRunningTask) {
		automaticRole.setName("default"); // default name
		IdmRoleTreeNodeDto roleTreeNode = roleTreeNodeService.saveInternal(automaticRole);
		//
		if (withLongRunningTask) {
			ProcessAutomaticRoleByTreeTaskExecutor task = new ProcessAutomaticRoleByTreeTaskExecutor();
			task.setAutomaticRoles(Lists.newArrayList(roleTreeNode.getId()));
			taskManager.executeSync(task);
		}
		//
		return roleTreeNodeService.get(roleTreeNode.getId());
	}
	
	@Test
	@Transactional
	public void testChangeContractValidityWithAssignedRoles() {
		IdmRoleDto role = getHelper().createRole();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmRoleTreeNodeDto automaticRole = getHelper().createAutomaticRole(role, treeNode);
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		IdmContractPositionDto position = getHelper().createContractPosition(contract, treeNode);
		getHelper().createContractPosition(contract, getHelper().createTreeNode()); // other
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		Assert.assertEquals(position.getId(), assignedRoles.get(0).getContractPosition());
		Assert.assertNull(assignedRoles.get(0).getValidTill());
		//
		contract.setValidTill(LocalDate.now().plusDays(2));
		contract = contractService.save(contract);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(contract.getValidTill(), assignedRoles.get(0).getValidTill());
	}
	
	@Test
	@Transactional
	public void testDontAssingRoleForInvalidContractWhenPositionIsChanged() {
		IdmRoleDto role = getHelper().createRole();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		getHelper().createAutomaticRole(role, treeNode);
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().createContract(identity, null, null, LocalDate.now().minusDays(1));
		IdmContractPositionDto position = getHelper().createContractPosition(contract, null);
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		position.setWorkPosition(treeNode.getId());
		position = service.save(position);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
	}
	
	@Test
	public void testDontAssingRoleForDisabledContractWhenPositionIsChanged() {
		IdmRoleDto role = getHelper().createRole();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		getHelper().createAutomaticRole(role, treeNode);
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().createContract(identity);
		contract.setState(ContractState.DISABLED);
		contract = contractService.save(contract);
		IdmContractPositionDto position = getHelper().createContractPosition(contract, null);
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		position.setWorkPosition(treeNode.getId());
		position = service.save(position);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
	}
	
	@Test
	public void testAssingRoleForContractValidInTheFuture() {
		IdmRoleDto role = getHelper().createRole();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmRoleTreeNodeDto automaticRole = getHelper().createAutomaticRole(role, treeNode);
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().createContract(identity, null, LocalDate.now().plusDays(2), null);
		IdmContractPositionDto position = getHelper().createContractPosition(contract, null);
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		position.setWorkPosition(treeNode.getId());
		position = service.save(position);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		Assert.assertEquals(position.getId(), assignedRoles.get(0).getContractPosition());
	}
	
	@Test
	public void testDontAssingRoleForContractValidInThePast() {
		IdmRoleDto role = getHelper().createRole();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		getHelper().createAutomaticRole(role, treeNode);
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().createContract(identity, null, null, LocalDate.now().minusDays(2));
		IdmContractPositionDto position = getHelper().createContractPosition(contract, null);
		//
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		position.setWorkPosition(treeNode.getId());
		position = service.save(position);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
	}
	
	/**
	 * Prevent to remove automatic roles assigned by contract position, when contract primary position is changed.
	 */
	@Test
	public void testPreventToRemoveRoles() {
		IdmRoleDto roleOne = getHelper().createRole();
		IdmTreeNodeDto treeNodeOne = getHelper().createTreeNode();
		IdmRoleTreeNodeDto automaticRoleOne = getHelper().createAutomaticRole(roleOne, treeNodeOne);
		//
		IdmRoleDto roleTwo = getHelper().createRole();
		IdmTreeNodeDto treeNodeTwo = getHelper().createTreeNode();
		IdmRoleTreeNodeDto automaticRoleTwo = getHelper().createAutomaticRole(roleTwo, treeNodeTwo);
		//
		// create identity
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		//
		// empty roles
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(identity.getId());
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.find(filter, null).getContent();
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		// create contract positon
		IdmContractPositionDto contractPosition = getHelper().createContractPosition(contract, treeNodeOne);
		assignedRoles = identityRoleService.find(filter, null).getContent();
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getAutomaticRole().equals(automaticRoleOne.getId())));
		//
		// update contract
		contract.setWorkPosition(treeNodeTwo.getId());
		contract = contractService.save(contract);
		//
		assignedRoles = identityRoleService.find(filter, null).getContent();
		Assert.assertEquals(2, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getAutomaticRole().equals(automaticRoleOne.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getAutomaticRole().equals(automaticRoleTwo.getId())));
		//
		// clear prime contract position
		contract.setWorkPosition(null);
		contract = contractService.save(contract);
		//
		assignedRoles = identityRoleService.find(filter, null).getContent();
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getAutomaticRole().equals(automaticRoleOne.getId())));
		//
		contract.setWorkPosition(treeNodeTwo.getId());
		contract = contractService.save(contract);
		//
		// remove contract 
		service.delete(contractPosition);
		//
		assignedRoles = identityRoleService.find(filter, null).getContent();
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getAutomaticRole().equals(automaticRoleTwo.getId())));
		// 
		// remove contract position
		contractService.delete(contract);
		//
		assignedRoles = identityRoleService.find(filter, null).getContent();
		Assert.assertTrue(assignedRoles.isEmpty());
	}
	
	@Test
	public void testAssignSameAutomaticRoleAsContract() {
		// create automatic role on tree node
		IdmRoleDto role = getHelper().createRole();
		IdmTreeNodeDto node = getHelper().createTreeNode();
		IdmRoleTreeNodeDto automaticRoleTwo = getHelper().createAutomaticRole(role, node);
		// create identity
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().createContract(identity, node);
		getHelper().createContractPosition(contract, node);
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(identity.getId());
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.find(filter, null).getContent();
		Assert.assertEquals(2, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().allMatch(ir -> ir.getAutomaticRole().equals(automaticRoleTwo.getId())));
	}
}
