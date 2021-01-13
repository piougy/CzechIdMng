package eu.bcvsolutions.idm.core.model.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.testng.collections.Lists;

import eu.bcvsolutions.forest.index.service.api.ForestIndexService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AutomaticRoleManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent.TreeNodeEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeForestContentService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ProcessAllAutomaticRoleByTreeTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ProcessAutomaticRoleByTreeTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ProcessSkippedAutomaticRoleByTreeTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tree nodes 
 * - referential integrity
 * - node move operation
 * - reindex
 * 
 * @author Radek Tomiška
 */
public class DefaultIdmTreeNodeServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private ApplicationContext context;
	@Autowired private IdmTreeNodeForestContentService treeNodeForestContentService;
	@Autowired private ForestIndexService<IdmForestIndexEntity, UUID> forestIndexService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	@Autowired private EntityStateManager entityStateManager;
	//
	private DefaultIdmTreeNodeService service;
	
	@Before
	public void init() {
		service = context.getAutowireCapableBeanFactory().createBean(DefaultIdmTreeNodeService.class);
	}
	
	@Transactional
	@Test(expected = TreeNodeException.class)
	public void testReferentialIntegrityDeleteNodeWithContracts() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		getHelper().createContract(identity, treeNode);
	    // tree node cannot be deleted, when some contract are defined on this node
	    service.delete(treeNode);
	}
	
	@Transactional
	@Test(expected = TreeNodeException.class)
	public void testReferentialIntegrityDeleteNodeWithChildren() {
		IdmTreeTypeDto treeType = getHelper().createTreeType();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode(treeType, null);
		getHelper().createTreeNode(treeType, treeNode);
	    // tree node cannot be deleted, when some contract are defined on this node
	    service.delete(treeNode);
	}
	
	@Transactional
	@Test(expected = TreeNodeException.class)
	public void testReferentialIntegrityDeleteNodeWithContractPositions() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmIdentityContractDto contract = getHelper().createContract(identity);
		getHelper().createContractPosition(contract, treeNode);
	    // tree node cannot be deleted, when some contract are defined on this node
	    service.delete(treeNode);
	}
	
	@Test
	// @Transactional - TODO: fix recount index in transaction
	public void testForestIndexAfterBulkMoveWithoutTransaction() {
		int rootCount = 5;
		// prepare new tree type
		IdmTreeTypeDto treeType = getHelper().createTreeType();
		// create root nodes
		for (int i = 0; i < rootCount; i++) {
			getHelper().createTreeNode(treeType, null);
		}
		// move nodes to the first node
		IdmTreeNodeFilter filter = new IdmTreeNodeFilter();
		filter.setTreeTypeId(treeType.getId());
		List<IdmTreeNodeDto> nodes = service.find(filter, null).getContent();
		IdmTreeNodeDto root = nodes.get(0);
		for (int i = 0; i < nodes.size(); i++) {
			IdmTreeNodeDto node = nodes.get(i);
			if (node.equals(root)) {
				continue;
			}
			node.setParent(root.getId());
			node = service.save(node);
		}		
		// check
		Assert.assertEquals(1L, service.findRoots(treeType.getId(), null).getTotalElements());
		Assert.assertEquals(rootCount - 1, service.findChildrenByParent(root.getId(), null).getTotalElements());
		Assert.assertEquals(rootCount - 1, treeNodeForestContentService.findDirectChildren(root.getId(), null).getTotalElements());
		Assert.assertEquals(rootCount - 1, treeNodeForestContentService.findAllChildren(root.getId(), null).getTotalElements());
	}
	
	@Test
	@Transactional
	public void testForestIndexAfterBulkMoveWithTransaction() {
		int rootCount = 5;
		// prepare new tree type
		IdmTreeTypeDto treeType = getHelper().createTreeType();
		// create root nodes
		for (int i = 0; i < rootCount; i++) {
			getHelper().createTreeNode(treeType, null);
		}
		// move nodes to the first node
		IdmTreeNodeFilter filter = new IdmTreeNodeFilter();
		filter.setTreeTypeId(treeType.getId());
		List<IdmTreeNodeDto> nodes = service.find(filter, null).getContent();
		IdmTreeNodeDto root = nodes.get(0);
		for (int i = 0; i < nodes.size(); i++) {
			IdmTreeNodeDto node = nodes.get(i);
			if (node.equals(root)) {
				continue;
			}
			node.setParent(root.getId());
			node = service.save(node);
		}		
		// check
		Assert.assertEquals(1L, service.findRoots(treeType.getId(), null).getTotalElements());
		Assert.assertEquals(rootCount - 1, service.findChildrenByParent(root.getId(), null).getTotalElements());
		Assert.assertEquals(rootCount - 1, treeNodeForestContentService.findDirectChildren(root.getId(), null).getTotalElements());
		Assert.assertEquals(rootCount - 1, treeNodeForestContentService.findAllChildren(root.getId(), null).getTotalElements());
	}
	
	/**
	 * Move childern to new parent a delete previous parent
	 * 
	 */
	@Test
	@Transactional
	public void testMoveChildren() {
		IdmTreeTypeDto treeType = getHelper().createTreeType();
		// create root node
		IdmTreeNodeDto root = getHelper().createTreeNode(treeType, null);
		IdmTreeNodeDto subRoot = getHelper().createTreeNode(treeType, root);
		// create children
		IdmTreeNodeDto nodeOne = getHelper().createTreeNode(treeType, subRoot);
		IdmTreeNodeDto nodeTwo = getHelper().createTreeNode(treeType, subRoot);
		IdmTreeNodeDto nodeThree = getHelper().createTreeNode(treeType, subRoot);
		//
		Assert.assertEquals(4, treeNodeForestContentService.findAllChildren(root.getId(), null).getTotalElements());
		Assert.assertEquals(3, treeNodeForestContentService.findAllChildren(subRoot.getId(), null).getTotalElements());
		//
		// move children to other parent
		IdmTreeNodeDto subRootTwo = getHelper().createTreeNode(treeType, root);
		nodeOne.setParent(subRootTwo.getId());
		nodeOne = service.save(nodeOne);
		nodeTwo.setParent(subRootTwo.getId());
		nodeTwo = service.save(nodeTwo);
		nodeThree.setParent(subRootTwo.getId());
		nodeThree = service.save(nodeThree);
		//
		// delete previous parent
		service.delete(subRoot);
		//
		Assert.assertEquals(4, treeNodeForestContentService.findAllChildren(root.getId(), null).getTotalElements());
		Assert.assertEquals(3, treeNodeForestContentService.findAllChildren(subRootTwo.getId(), null).getTotalElements());
	}
	
	@Test
	@Transactional
	public void testBadTreeTypeUpdate() {
		IdmTreeTypeDto parent1 = getHelper().createTreeType();
		IdmTreeTypeDto parent2 = getHelper().createTreeType();
		//
		IdmTreeNodeDto node1 = getHelper().createTreeNode(parent1, null);
		IdmTreeNodeDto node2 = getHelper().createTreeNode(parent1, node1);
		IdmTreeNodeDto node3 = getHelper().createTreeNode(parent1, node2);
		//
		node3.setTreeType(parent2.getId());
		try {
			node3 = service.save(node3);
			Assert.fail();
		} catch (TreeNodeException ex) { 
			Assert.assertTrue(ex.getMessage().contains("bad type"));
		} catch (Exception e) {
			Assert.fail();
		}
		//
		node1.setTreeType(parent2.getId());
		try {
			node1 = service.save(node1);
			Assert.fail();
		} catch (TreeNodeException ex) { 
			Assert.assertTrue(ex.getMessage().contains("bad type"));
		} catch (Exception e) {
			Assert.fail();
		}
	}
	
	@Test
	@Transactional
	public void testBadTreeTypeCreate() {
		IdmTreeTypeDto parent1 = getHelper().createTreeType();
		IdmTreeTypeDto parent2 = getHelper().createTreeType();
		//
		IdmTreeNodeDto node1 = getHelper().createTreeNode(parent1, null);
		IdmTreeNodeDto node2 = getHelper().createTreeNode(parent1, node1);
		IdmTreeNodeDto node3 = getHelper().createTreeNode(parent1, node2);
		//
		try {
			getHelper().createTreeNode(parent2, node1);
			Assert.fail();
		} catch (TreeNodeException ex) { 
			Assert.assertTrue(ex.getMessage().contains("bad type"));
		} catch (Exception e) {
			Assert.fail();
		}
		//
		try {
			getHelper().createTreeNode(parent2, node3);
			Assert.fail();
		} catch (TreeNodeException ex) { 
			Assert.assertTrue(ex.getMessage().contains("bad type"));
		} catch (Exception e) {
			Assert.fail();
		}
	}
	
	@Test
	@Transactional
	public void testFindAllParents() {
		// TODO: map in filter and move to rest test
		//
		IdmTreeTypeDto treeType = getHelper().createTreeType();
		IdmTreeNodeDto node1 = getHelper().createTreeNode(treeType, null);
		IdmTreeNodeDto node2 = getHelper().createTreeNode(treeType, node1);
		IdmTreeNodeDto node3 = getHelper().createTreeNode(treeType, node2);
		getHelper().createTreeNode(treeType, node3);
		//
		List<IdmTreeNodeDto> parents = service.findAllParents(node3.getId(), null);
		//
		Assert.assertEquals(2, parents.size());
		Assert.assertTrue(parents.stream().anyMatch(n -> n.equals(node1)));
		Assert.assertTrue(parents.stream().anyMatch(n -> n.equals(node2)));
	}
	
	@Test
	public void testRebuildIndex() {
		IdmTreeTypeDto treeType = getHelper().createTreeType();
		IdmTreeNodeDto node1 = getHelper().createTreeNode(treeType, null);
		IdmTreeNodeDto node2 = getHelper().createTreeNode(treeType, node1);
		IdmTreeNodeDto node3 = getHelper().createTreeNode(treeType, node2);
		IdmTreeNodeDto node4 = getHelper().createTreeNode(treeType, node3);
		// before index will be droped
		IdmTreeNodeFilter filter = new IdmTreeNodeFilter();
		filter.setParent(node2.getId());
		filter.setRecursively(true);
		//
		List<IdmTreeNodeDto> results = service.find(filter, null).getContent();
		//
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(n -> n.equals(node3)));
		Assert.assertTrue(results.stream().anyMatch(n -> n.equals(node4)));
		//
		// drop indexes
		forestIndexService.dropIndexes(IdmTreeNode.toForestTreeType(treeType.getId()));
		//
		results = service.find(filter, null).getContent();
		Assert.assertEquals(0, results.size());
		//
		// reindex tree type
		service.rebuildIndexes(treeType.getId());
		//
		results = service.find(filter, null).getContent();
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(n -> n.equals(node3)));
		Assert.assertTrue(results.stream().anyMatch(n -> n.equals(node4)));
	}

	@Transactional
	@Test(expected = ResultCodeException.class)
	public void testNullCode() {
		IdmTreeTypeDto treeType = getHelper().createTreeType();
		IdmTreeNodeDto node = new IdmTreeNodeDto();
		node.setTreeType(treeType.getId());
		node.setName("test-" + getHelper().createName());
		node.setCode(null);
		
		service.save(node);
	}

	@Transactional
	@Test(expected = ResultCodeException.class)
	public void testEmptyCode() {
		IdmTreeTypeDto treeType = getHelper().createTreeType();
		IdmTreeNodeDto node = new IdmTreeNodeDto();
		node.setTreeType(treeType.getId());
		node.setName("test-" + getHelper().createName());
		node.setCode("    ");
		
		service.save(node);
	}
	
	@Test
	public void testAssignAutomaticRoleAfterNodeIsMovedWithDownRecursion() {
		IdmTreeNodeDto parentNode = getHelper().createTreeNode();
		IdmTreeNodeDto node = getHelper().createTreeNode();
		// define automatic role for parent
		IdmRoleDto role = getHelper().createRole();
		IdmRoleTreeNodeDto automaticRole = getHelper().createRoleTreeNode(role, parentNode, RecursionType.DOWN, true);
		// create identity with contract on node
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		getHelper().createContract(identity, node);
		// no role should be assigned now
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		node.setParent(parentNode.getId());
		node = service.save(node);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		//
		IdmTreeNodeDto otherNode = getHelper().createTreeNode();
		IdmRoleTreeNodeDto otherAutomaticRole = getHelper().createRoleTreeNode(role, otherNode, RecursionType.DOWN, true);
		node.setParent(otherNode.getId());
		node = service.save(node);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		UUID assignedRoleId = assignedRoles.get(0).getId();
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(otherAutomaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		//
		// recalculate role => nothing happend
		ProcessAutomaticRoleByTreeTaskExecutor automaticRoleTask = AutowireHelper.createBean(ProcessAutomaticRoleByTreeTaskExecutor.class);
		automaticRoleTask.setAutomaticRoles(Lists.newArrayList(otherAutomaticRole.getId()));
		longRunningTaskManager.execute(automaticRoleTask);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(otherAutomaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		Assert.assertEquals(assignedRoleId, assignedRoles.get(0).getId());
		//
		// move node deeper in sub tree => nothing should happend
		IdmTreeNodeDto subNode = getHelper().createTreeNode(null, null, getHelper().createTreeNode(null, null, otherNode));
		node.setParent(subNode.getId());
		node = service.save(node);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(otherAutomaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		Assert.assertEquals(assignedRoleId, assignedRoles.get(0).getId());
	}
	
	@Test
	public void testAssignAutomaticRoleAfterNodeIsMovedWithUpRecursion() {
		IdmTreeNodeDto parentNode = getHelper().createTreeNode();
		IdmTreeNodeDto node = getHelper().createTreeNode();
		// define automatic role for parent
		IdmRoleDto role = getHelper().createRole();
		IdmRoleTreeNodeDto automaticRole = getHelper().createRoleTreeNode(role, node, RecursionType.UP, true);
		// create identity with contract on node
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		getHelper().createContract(identity, parentNode);
		// no role should be assigned now
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		node.setParent(parentNode.getId());
		node = service.save(node);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		//
		IdmTreeNodeDto otherNode = getHelper().createTreeNode(null, null, node);
		IdmRoleDto roleOther = getHelper().createRole();
		IdmRoleTreeNodeDto otherAutomaticRole = getHelper().createRoleTreeNode(roleOther, otherNode, RecursionType.UP, false);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(2, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRole.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> otherAutomaticRole.getId().equals(ir.getAutomaticRole())));
	}
	
	@Test
	public void testDontAssignAutomaticRoleAfterNodeIsMovedToInvalidContract() {
		IdmTreeNodeDto parentNode = getHelper().createTreeNode();
		IdmTreeNodeDto node = getHelper().createTreeNode();
		// define automatic role for parent
		IdmRoleDto role = getHelper().createRole();
		getHelper().createRoleTreeNode(role, node, RecursionType.UP, true);
		// create identity with contract on node
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		getHelper().createContract(identity, parentNode, null, LocalDate.now().minusDays(1));
		// no role should be assigned now
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		node.setParent(parentNode.getId());
		node = service.save(node);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
	}
	
	@Test
	public void testAssignAutomaticRoleOnPositionAfterNodeIsMovedWithDownRecursion() {
		IdmTreeNodeDto parentNode = getHelper().createTreeNode();
		IdmTreeNodeDto node = getHelper().createTreeNode();
		// define automatic role for parent
		IdmRoleDto role = getHelper().createRole();
		IdmRoleTreeNodeDto automaticRole = getHelper().createRoleTreeNode(role, parentNode, RecursionType.DOWN, true);
		// create identity with contract on node
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		getHelper().createContractPosition(getHelper().getPrimeContract(identity), node);
		// no role should be assigned now
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		node.setParent(parentNode.getId());
		node = service.save(node);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		//
		IdmTreeNodeDto otherNode = getHelper().createTreeNode();
		IdmRoleTreeNodeDto otherAutomaticRole = getHelper().createRoleTreeNode(role, otherNode, RecursionType.DOWN, true);
		node.setParent(otherNode.getId());
		node = service.save(node);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		UUID assignedRoleId = assignedRoles.get(0).getId();
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(otherAutomaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		//
		// recalculate role => nothing happend
		ProcessAutomaticRoleByTreeTaskExecutor automaticRoleTask = AutowireHelper.createBean(ProcessAutomaticRoleByTreeTaskExecutor.class);
		automaticRoleTask.setAutomaticRoles(Lists.newArrayList(otherAutomaticRole.getId()));
		longRunningTaskManager.execute(automaticRoleTask);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(otherAutomaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		Assert.assertEquals(assignedRoleId, assignedRoles.get(0).getId());
		//
		// move node deeper in sub tree => nothing should happend
		IdmTreeNodeDto subNode = getHelper().createTreeNode(null, null, getHelper().createTreeNode(null, null, otherNode));
		node.setParent(subNode.getId());
		node = service.save(node);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(otherAutomaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		Assert.assertEquals(assignedRoleId, assignedRoles.get(0).getId());
	}
	
	@Test
	public void testAssignAutomaticRoleOnPositionAfterNodeIsMovedWithUpRecursion() {
		IdmTreeNodeDto parentNode = getHelper().createTreeNode();
		IdmTreeNodeDto node = getHelper().createTreeNode();
		// define automatic role for parent
		IdmRoleDto role = getHelper().createRole();
		IdmRoleTreeNodeDto automaticRole = getHelper().createRoleTreeNode(role, node, RecursionType.UP, true);
		// create identity with contract on node
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		getHelper().createContractPosition(getHelper().getPrimeContract(identity), parentNode);
		// no role should be assigned now
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		node.setParent(parentNode.getId());
		node = service.save(node);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		//
		IdmTreeNodeDto otherNode = getHelper().createTreeNode(null, null, node);
		IdmRoleDto roleOther = getHelper().createRole();
		IdmRoleTreeNodeDto otherAutomaticRole = getHelper().createRoleTreeNode(roleOther, otherNode, RecursionType.UP, false);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(2, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRole.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> otherAutomaticRole.getId().equals(ir.getAutomaticRole())));
	}
	
	@Test
	public void testDontAssignAutomaticRoleOnPositionAfterNodeIsMovedToInvalidContract() {
		IdmTreeNodeDto parentNode = getHelper().createTreeNode();
		IdmTreeNodeDto node = getHelper().createTreeNode();
		// define automatic role for parent
		IdmRoleDto role = getHelper().createRole();
		getHelper().createRoleTreeNode(role, node, RecursionType.UP, true);
		// create identity with contract on node
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().createContract(identity, null, null, LocalDate.now().minusDays(1));
		getHelper().createContractPosition(contract, parentNode);
		// no role should be assigned now
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		node.setParent(parentNode.getId());
		node = service.save(node);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
	}
	
	@Test
	@Transactional
	public void testChangeParentToRoot() {
		IdmTreeNodeDto parent = getHelper().createTreeNode();
		IdmTreeNodeDto node = getHelper().createTreeNode((IdmTreeTypeDto) null, parent);
		//
		Assert.assertEquals(parent.getId(), node.getParent());
		// set parent as root
		node.setParent(null);
		node = service.save(node);
		//
		Assert.assertNull(node.getParent());
	}
	
	@Test
	public void testSkipAndAssignAutomaticRoleAfterNodeIsMovedWithUpRecursion() {
		IdmTreeNodeDto parentNode = getHelper().createTreeNode();
		IdmTreeNodeDto node = getHelper().createTreeNode();
		// define automatic role for parent
		IdmRoleDto role = getHelper().createRole();
		IdmRoleTreeNodeDto automaticRole = getHelper().createRoleTreeNode(role, node, RecursionType.UP, true);
		// create identity with contract on node
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		getHelper().createContract(identity, parentNode);
		// no role should be assigned now
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		node.setParent(parentNode.getId());
		EntityEvent<IdmTreeNodeDto> event = new TreeNodeEvent(TreeNodeEventType.UPDATE, node);
		event.getProperties().put(AutomaticRoleManager.SKIP_RECALCULATION, Boolean.TRUE);
		node = service.publish(event).getContent();
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		// recount skipped automatic roles
		longRunningTaskManager.execute(new ProcessSkippedAutomaticRoleByTreeTaskExecutor());
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		//
		IdmTreeNodeDto otherNode = getHelper().createTreeNode(null, null, node);
		IdmRoleDto roleOther = getHelper().createRole();
		IdmRoleTreeNodeDto otherAutomaticRole = getHelper().createRoleTreeNode(roleOther, otherNode, RecursionType.UP, false);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(2, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRole.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> otherAutomaticRole.getId().equals(ir.getAutomaticRole())));
	}
	
	@Test
	public void testSkipAndAssignAutomaticRoleOnPositionAfterNodeIsMovedWithUpRecursion() {
		IdmTreeNodeDto parentNode = getHelper().createTreeNode();
		IdmTreeNodeDto node = getHelper().createTreeNode();
		// define automatic role for parent
		IdmRoleDto role = getHelper().createRole();
		IdmRoleTreeNodeDto automaticRole = getHelper().createRoleTreeNode(role, node, RecursionType.UP, true);
		// create identity with contract on node
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		getHelper().createContractPosition(getHelper().getPrimeContract(identity), parentNode);
		// no role should be assigned now
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		node.setParent(parentNode.getId());
		EntityEvent<IdmTreeNodeDto> event = new TreeNodeEvent(TreeNodeEventType.UPDATE, node);
		event.getProperties().put(AutomaticRoleManager.SKIP_RECALCULATION, Boolean.TRUE);
		node = service.publish(event).getContent();
		IdmEntityStateFilter filter = new IdmEntityStateFilter();
		filter.setStates(Lists.newArrayList(OperationState.BLOCKED));
		filter.setResultCode(CoreResultCode.AUTOMATIC_ROLE_SKIPPED.getCode());
		filter.setOwnerType(entityStateManager.getOwnerType(IdmRoleTreeNodeDto.class));
		List<IdmEntityStateDto> skippedStates = entityStateManager.findStates(filter, null).getContent();
		Assert.assertTrue(skippedStates.stream().anyMatch(s -> s.getOwnerId().equals(automaticRole.getId())));
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		// recount skipped automatic roles
		longRunningTaskManager.execute(new ProcessSkippedAutomaticRoleByTreeTaskExecutor());
		skippedStates = entityStateManager.findStates(filter, null).getContent();
		Assert.assertFalse(skippedStates.stream().anyMatch(s -> s.getOwnerId().equals(automaticRole.getId())));
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		//
		IdmTreeNodeDto otherNode = getHelper().createTreeNode(null, null, node);
		IdmRoleDto roleOther = getHelper().createRole();
		IdmRoleTreeNodeDto otherAutomaticRole = getHelper().createRoleTreeNode(roleOther, otherNode, RecursionType.UP, false);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(2, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> automaticRole.getId().equals(ir.getAutomaticRole())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> otherAutomaticRole.getId().equals(ir.getAutomaticRole())));
	}
	
	@Test
	public void testRecountAutomaticRoleWithMissingContent() {
		// create state with missing content
		IdmEntityStateDto state = new IdmEntityStateDto();
		UUID ownerId = UUID.randomUUID();
		state.setOwnerId(ownerId);
		state.setOwnerType(entityStateManager.getOwnerType(IdmRoleTreeNodeDto.class));
		state.setResult(
				new OperationResultDto
					.Builder(OperationState.BLOCKED)
					.setModel(new DefaultResultModel(CoreResultCode.AUTOMATIC_ROLE_SKIPPED))
					.build());
		entityStateManager.saveState(null, state);
		//
		state = new IdmEntityStateDto();
		state.setOwnerId(ownerId);
		state.setOwnerType(entityStateManager.getOwnerType(IdmRoleTreeNodeDto.class));
		state.setResult(
				new OperationResultDto
					.Builder(OperationState.BLOCKED)
					.setModel(new DefaultResultModel(CoreResultCode.AUTOMATIC_ROLE_SKIPPED))
					.build());
		entityStateManager.saveState(null, state);
		//
		// recount skipped automatic roles
		LongRunningFutureTask<Boolean> executor = longRunningTaskManager.execute(new ProcessSkippedAutomaticRoleByTreeTaskExecutor());
		IdmLongRunningTaskDto longRunningTask = longRunningTaskManager.getLongRunningTask(executor);
		Assert.assertTrue(longRunningTask.getWarningItemCount() > 0);
	}
	
	@Test
	public void testRecountAutomaticRoleMultipleTimes() {
		IdmTreeNodeDto node = getHelper().createTreeNode();
		// define automatic role for parent
		IdmRoleDto role = getHelper().createRole();
		IdmRoleTreeNodeDto automaticRole = getHelper().createRoleTreeNode(role, node, RecursionType.NO, true);
		// create identity with contract on node
		entityStateManager.createState(automaticRole, OperationState.BLOCKED, CoreResultCode.AUTOMATIC_ROLE_SKIPPED, null);
		entityStateManager.createState(automaticRole, OperationState.BLOCKED, CoreResultCode.AUTOMATIC_ROLE_SKIPPED, null);
		Assert.assertEquals(2, entityStateManager.findStates(automaticRole, null).getTotalElements());
		//
		// recount skipped automatic roles
		LongRunningFutureTask<Boolean> executor = longRunningTaskManager.execute(new ProcessSkippedAutomaticRoleByTreeTaskExecutor());
		IdmLongRunningTaskDto longRunningTask = longRunningTaskManager.getLongRunningTask(executor);
		Assert.assertEquals(Long.valueOf(2), longRunningTask.getSuccessItemCount());
	}
	
	@Test
	public void testRecalculateAllAutomaticRoles() {
		IdmTreeNodeDto parentNode = getHelper().createTreeNode();
		IdmTreeNodeDto node = getHelper().createTreeNode();
		// define automatic role for parent
		IdmRoleDto role = getHelper().createRole();
		IdmRoleTreeNodeDto automaticRole = getHelper().createRoleTreeNode(role, node, RecursionType.UP, true);
		// create identity with contract on node
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().createContract(identity, parentNode);
		entityStateManager.createState(contract, OperationState.BLOCKED, CoreResultCode.AUTOMATIC_ROLE_SKIPPED, null);
		Assert.assertEquals(1, entityStateManager.findStates(contract, null).getTotalElements());
		// no role should be assigned now
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		node.setParent(parentNode.getId());
		EntityEvent<IdmTreeNodeDto> event = new TreeNodeEvent(TreeNodeEventType.UPDATE, node);
		event.getProperties().put(AutomaticRoleManager.SKIP_RECALCULATION, Boolean.TRUE);
		node = service.publish(event).getContent();
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertTrue(assignedRoles.isEmpty());
		//
		// recount skipped automatic roles
		ProcessAllAutomaticRoleByTreeTaskExecutor taskExecutor = AutowireHelper.createBean(ProcessAllAutomaticRoleByTreeTaskExecutor.class);
		taskExecutor.init(null);
		longRunningTaskManager.execute(taskExecutor);
		//
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertEquals(automaticRole.getId(), assignedRoles.get(0).getAutomaticRole());
		Assert.assertEquals(0, entityStateManager.findStates(contract, null).getTotalElements());
	}
}
