package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeForestContentService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tree nodes 
 * - filters
 * - indexing
 * 
 * @author Peter Šourek
 * @author Radek Tomiška
 */
public class IdmTreeNodeServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmTreeTypeService treeTypeService;
	@Autowired private IdmTreeNodeService treeNodeService;
	@Autowired private IdmTreeNodeForestContentService treeNodeForestContentService;
	
	@Before
	public void init() {
		getHelper().loginAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}
	
	@Transactional
	@Test(expected = ResultCodeException.class)
	public void testReferentialIntegrity() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		getHelper().createIdentityContact(identity, treeNode);
	    // tree node cannot be deleted, when some contract are defined on this node
	    treeNodeService.delete(treeNode);
	}
	
	@Test
	@Transactional
	public void testCreateNode() {	
		Exception ex = null;
		try {
			getHelper().createTreeNode(null, "TEST_NODE", null);
		} catch (Exception e) {
			ex = e;
		}
		
		Assert.assertNotNull(ex);

		IdmTreeTypeDto type = getHelper().createTreeType("TEST_TYPE_A");
		type = treeTypeService.save(type);
		
		ex = null;
		try {
			getHelper().createTreeNode(type, "TEST_NODE", null);
		} catch (Exception e) {
			ex = e;
		}
		
		Assert.assertNull(ex);
	}

	@Test
	@Transactional
	public void testFilters() {
		// ****** PREPARE DATA ******
		IdmTreeTypeDto t1 =  treeTypeService.save(getHelper().createTreeType("TYPE1"));
		IdmTreeTypeDto t2 =  treeTypeService.save(getHelper().createTreeType("TYPE2"));
		UUID t1Id = t1.getId();
		UUID t2Id = t2.getId();
		/*
		        o r1
		       / \
		   n1 o   o n3
		  / \
	      n2 o   o n4
		      \
		       o n5
		*/
		IdmTreeNodeDto r1 = getHelper().createTreeNode(t1, "ROOT1", null);
		IdmTreeNodeDto n1 = getHelper().createTreeNode(t1, "NODE1", r1);
		getHelper().createTreeNode(t1, "NODE2", n1);
		getHelper().createTreeNode(t1, "NODE3", r1);
		IdmTreeNodeDto n4 = getHelper().createTreeNode(t1, "NODE4", n1);
		IdmTreeNodeDto n5 = getHelper().createTreeNode(t1, "NODE5", n4);
		/*
		         o r2
		        /
		 n12 o-o n11
		 */
		IdmTreeNodeDto r2 = getHelper().createTreeNode(t2, "ROOT2", null);
		IdmTreeNodeDto n11 = getHelper().createTreeNode(t2, "NODE11", r2);
		IdmTreeNodeDto n12 = getHelper().createTreeNode(t2, "NODE12", n11);
		//
		final UUID r1Uuid = r1.getId();
		final UUID n1Uuid = n1.getId();
		final UUID n5Uuid = n5.getId();
		final UUID r2Uuid = r2.getId();
		final UUID n12Uuid = n12.getId();
		//
		// ******* TEST *******
		//
		final IdmTreeNodeFilter t1Flter = new IdmTreeNodeFilter();
		t1Flter.setTreeTypeId(t1Id);
		Page<IdmTreeNodeDto> res1 = treeNodeService.find(t1Flter, null);
		Assert.assertEquals(6, res1.getTotalElements());
		//
		final IdmTreeNodeFilter t2Flter = new IdmTreeNodeFilter();
		t2Flter.setTreeTypeId(t2Id);
		Page<IdmTreeNodeDto> res2 = treeNodeService.find(t2Flter, null);
		Assert.assertEquals(3, res2.getTotalElements());
		//
		// Subtrees
		//
		final IdmTreeNodeFilter subTreeFilter1 = new IdmTreeNodeFilter();
		subTreeFilter1.setTreeNode(n1Uuid);
		subTreeFilter1.setTreeTypeId(t1Id);
		subTreeFilter1.setRecursively(true);
		Page<IdmTreeNodeDto> res3 = treeNodeService.find(subTreeFilter1, null);
		Assert.assertEquals(3, res3.getTotalElements());
		//
		final IdmTreeNodeFilter subTreeFilter2 = new IdmTreeNodeFilter();
		subTreeFilter2.setTreeNode(n1Uuid);
		subTreeFilter2.setRecursively(false);
		Page<IdmTreeNodeDto> res4 = treeNodeService.find(subTreeFilter2, null);
		Assert.assertEquals(2, res4.getTotalElements());
		//
		final IdmTreeNodeFilter subTreeFilter3 = new IdmTreeNodeFilter();
		subTreeFilter3.setTreeNode(r2Uuid);
		subTreeFilter3.setRecursively(false);
		Page<IdmTreeNodeDto> res5 = treeNodeService.find(subTreeFilter3, null);
		Assert.assertEquals(1, res5.getTotalElements());
		//
		final IdmTreeNodeFilter subTreeFilter4 = new IdmTreeNodeFilter();
		subTreeFilter4.setTreeNode(r2Uuid);
		subTreeFilter4.setTreeTypeId(t2Id);
		subTreeFilter4.setRecursively(true);
		Page<IdmTreeNodeDto> res6 = treeNodeService.find(subTreeFilter4, null);
		Assert.assertEquals(2, res6.getTotalElements());
		//
		final IdmTreeNodeFilter subTreeFilter5 = new IdmTreeNodeFilter();
		subTreeFilter5.setTreeNode(n12Uuid);
		subTreeFilter5.setTreeTypeId(t2Id);
		subTreeFilter5.setRecursively(true);
		Page<IdmTreeNodeDto> res7 = treeNodeService.find(subTreeFilter5, null);
		Assert.assertEquals(0, res7.getTotalElements());
		//
		final IdmTreeNodeFilter subTreeFilter6 = new IdmTreeNodeFilter();
		subTreeFilter6.setTreeNode(n12Uuid);
		subTreeFilter6.setTreeTypeId(t2Id);
		subTreeFilter6.setRecursively(false);
		Page<IdmTreeNodeDto> res8 = treeNodeService.find(subTreeFilter6, null);
		Assert.assertEquals(0, res8.getTotalElements());
		//
		// Fulltext
		//
		final IdmTreeNodeFilter fullTextFilter1 = new IdmTreeNodeFilter();
		fullTextFilter1.setText("NODE5");
		Page<IdmTreeNodeDto> res9 = treeNodeService.find(fullTextFilter1, null);
		Assert.assertEquals(1, res9.getTotalElements());
		Assert.assertEquals(n5Uuid, res9.getContent().get(0).getId());
		//
		final IdmTreeNodeFilter fullTextFilter2 = new IdmTreeNodeFilter();
		fullTextFilter2.setText("NODE");
		fullTextFilter2.setTreeTypeId(t1Id);
		Page<IdmTreeNodeDto> res10 = treeNodeService.find(fullTextFilter2, null);
		Assert.assertEquals(5, res10.getTotalElements());
		//
		final IdmTreeNodeFilter fullTextFilter3 = new IdmTreeNodeFilter();
		fullTextFilter3.setText("odE");
		fullTextFilter3.setTreeTypeId(t1Id);
		Page<IdmTreeNodeDto> res13 = treeNodeService.find(fullTextFilter3, null);
		Assert.assertEquals(5, res13.getTotalElements());
		//
		// Property - value pairs
		//
		final IdmTreeNodeFilter dynPropFilter1 = new IdmTreeNodeFilter();
		dynPropFilter1.setProperty("name");
		dynPropFilter1.setValue("ROOT1");
		Page<IdmTreeNodeDto> res11 = treeNodeService.find(dynPropFilter1, null);
		Assert.assertEquals(1, res11.getTotalElements());
		Assert.assertEquals(r1Uuid, res11.getContent().get(0).getId());
		//
		final IdmTreeNodeFilter dynPropFilter2 = new IdmTreeNodeFilter();
		dynPropFilter2.setProperty("code");
		dynPropFilter2.setValue("ROOT2");
		Page<IdmTreeNodeDto> res12 = treeNodeService.find(dynPropFilter2, null);
		Assert.assertEquals(1, res12.getTotalElements());
		Assert.assertEquals(r2Uuid, res12.getContent().get(0).getId());

	}
	
	@Test
	// @Transactional - toho - fix recount index in transaction
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
	public void testFindChildrenRecursivelyWithoutTreeTypeSpecified() {
		IdmTreeTypeDto treeTypeOne = getHelper().createTreeType();
		IdmTreeTypeDto treeTypeTwo = getHelper().createTreeType();
		//
		IdmTreeNodeDto rootOne = getHelper().createTreeNode(treeTypeOne, null);
		IdmTreeNodeDto childOne = getHelper().createTreeNode(treeTypeOne, rootOne);
		IdmTreeNodeDto rootTwo = getHelper().createTreeNode(treeTypeTwo, null);
		getHelper().createTreeNode(treeTypeTwo, rootTwo);
		//
		IdmTreeNodeFilter filter = new IdmTreeNodeFilter();
		filter.setRecursively(true);
		filter.setTreeNode(rootOne.getId());
		//
		List<IdmTreeNodeDto> results = treeNodeService.find(filter, null).getContent();
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(childOne, results.get(0));
	}
}
