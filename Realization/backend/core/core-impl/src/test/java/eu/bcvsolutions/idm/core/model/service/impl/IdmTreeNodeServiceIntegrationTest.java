package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.forest.index.service.api.ForestIndexService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeForestContentService;
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
public class IdmTreeNodeServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmTreeNodeService treeNodeService;
	@Autowired private IdmTreeNodeForestContentService treeNodeForestContentService;
	@Autowired private ForestIndexService<IdmForestIndexEntity, UUID> forestIndexService;
	
	@Before
	public void init() {
		getHelper().loginAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}
	
	@Transactional
	@Test(expected = TreeNodeException.class)
	public void testReferentialIntegrityDeleteNodeWithContracts() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		getHelper().createIdentityContact(identity, treeNode);
	    // tree node cannot be deleted, when some contract are defined on this node
	    treeNodeService.delete(treeNode);
	}
	
	@Transactional
	@Test(expected = TreeNodeException.class)
	public void testReferentialIntegrityDeleteNodeWithChildren() {
		IdmTreeTypeDto treeType = getHelper().createTreeType();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode(treeType, null);
		getHelper().createTreeNode(treeType, treeNode);
	    // tree node cannot be deleted, when some contract are defined on this node
	    treeNodeService.delete(treeNode);
	}
	
	@Test
	// @Transactional - TODO: fix recount index in transaction
	public void testForestIndexAfterBulkMove() {
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
		List<IdmTreeNodeDto> nodes = treeNodeService.find(filter, null).getContent();
		IdmTreeNodeDto root = nodes.get(0);
		for (int i = 0; i < nodes.size(); i++) {
			IdmTreeNodeDto node = nodes.get(i);
			if (node.equals(root)) {
				continue;
			}
			node.setParent(root.getId());
			node = treeNodeService.save(node);
		}		
		// check
		Assert.assertEquals(1L, treeNodeService.findRoots(treeType.getId(), null).getTotalElements());
		Assert.assertEquals(rootCount - 1, treeNodeService.findChildrenByParent(root.getId(), null).getTotalElements());
		Assert.assertEquals(rootCount - 1, treeNodeForestContentService.findDirectChildren(root.getId(), null).getTotalElements());
		Assert.assertEquals(rootCount - 1, treeNodeForestContentService.findAllChildren(root.getId(), null).getTotalElements());
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
			node3 = treeNodeService.save(node3);
			Assert.fail();
		} catch (TreeNodeException ex) { 
			Assert.assertTrue(ex.getMessage().contains("bad type"));
		} catch (Exception e) {
			Assert.fail();
		}
		//
		node1.setTreeType(parent2.getId());
		try {
			node1 = treeNodeService.save(node1);
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
		List<IdmTreeNodeDto> parents = treeNodeService.findAllParents(node3.getId(), null);
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
		filter.setTreeNode(node2.getId());
		filter.setRecursively(true);
		//
		List<IdmTreeNodeDto> results = treeNodeService.find(filter, null).getContent();
		//
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(n -> n.equals(node3)));
		Assert.assertTrue(results.stream().anyMatch(n -> n.equals(node4)));
		//
		// drop indexes
		forestIndexService.dropIndexes(IdmTreeNode.toForestTreeType(treeType.getId()));
		//
		results = treeNodeService.find(filter, null).getContent();
		Assert.assertEquals(0, results.size());
		//
		// reindex tree type
		treeNodeService.rebuildIndexes(treeType.getId());
		//
		results = treeNodeService.find(filter, null).getContent();
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(n -> n.equals(node3)));
		Assert.assertTrue(results.stream().anyMatch(n -> n.equals(node4)));
	}
}
