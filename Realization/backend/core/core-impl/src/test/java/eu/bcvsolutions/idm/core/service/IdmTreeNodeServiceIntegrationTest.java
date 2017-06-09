package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.filter.TreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tree nodes - filters etc.
 * 
 * TODO: TestHelper could be used
 * 
 * @author Peter Šourek
 */
public class IdmTreeNodeServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmTreeTypeService treeTypeService;
	@Autowired private IdmTreeNodeService treeNodeService;
	
	@Test
	public void testCreateNode() {
		IdmTreeNode node = getIdmTreeNode(null, null, "TEST_NODE", "TEST_NODE");
		
		Exception ex = null;
		try {
			treeNodeService.save(node);
		} catch (Exception e) {
			ex = e;
		}
		
		assertNotNull(ex);

		IdmTreeType type = getIdmTreeType("TEST_TYPE_A", "TEST_TYPE_A");
		treeTypeService.save(type);
		node.setTreeType(type);
		
		ex = null;
		try {
			treeNodeService.save(node);
		} catch (Exception e) {
			ex = e;
		}
		
		assertNull(ex);
	}

	@Test
	public void testFilters() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// ****** PREPARE DATA ******
		final IdmTreeType t1 = getIdmTreeType("TYPE1", "TYPE1");
		final IdmTreeType t2 = getIdmTreeType("TYPE2", "TYPE2");
		final UUID t1Id = treeTypeService.save(t1).getId();
		final UUID t2Id = treeTypeService.save(t2).getId();
		/*
		        o r1
		       / \
		   n1 o   o n3
		  / \
	      n2 o   o n4
		      \
		       o n5
		*/
		final IdmTreeNode r1 = getIdmTreeNode(t1, null, "ROOT1", "ROOT1");
		final IdmTreeNode n1 = getIdmTreeNode(t1, r1, "NODE1", "NODE1");
		final IdmTreeNode n2 = getIdmTreeNode(t1, n1, "NODE2", "NODE2");
		final IdmTreeNode n3 = getIdmTreeNode(t1, r1, "NODE3", "NODE3");
		final IdmTreeNode n4 = getIdmTreeNode(t1, n1, "NODE4", "NODE4");
		final IdmTreeNode n5 = getIdmTreeNode(t1, n4, "NODE5", "NODE5");
		/*
		         o r2
		        /
		 n12 o-o n11
		 */
		final IdmTreeNode r2 = getIdmTreeNode(t2, null, "ROOT2", "ROOT2");
		final IdmTreeNode n11 = getIdmTreeNode(t2, r2, "NODE11", "NODE11");
		final IdmTreeNode n12 = getIdmTreeNode(t2, n11, "NODE12", "NODE12");
		//
		final UUID r1Uuid = treeNodeService.save(r1).getId();
		final UUID n1Uuid = treeNodeService.save(n1).getId();
		final UUID n2Uuid = treeNodeService.save(n2).getId();
		final UUID n3Uuid = treeNodeService.save(n3).getId();
		final UUID n4Uuid = treeNodeService.save(n4).getId();
		final UUID n5Uuid = treeNodeService.save(n5).getId();
		final UUID r2Uuid = treeNodeService.save(r2).getId();
		final UUID n11Uuid = treeNodeService.save(n11).getId();
		final UUID n12Uuid = treeNodeService.save(n12).getId();
		//
		// ******* TEST *******
		//
		final TreeNodeFilter t1Flter = new TreeNodeFilter();
		t1Flter.setTreeTypeId(t1Id);
		Page<IdmTreeNode> res1 = treeNodeService.find(t1Flter, null);
		assertEquals(6, res1.getTotalElements());
		//
		final TreeNodeFilter t2Flter = new TreeNodeFilter();
		t2Flter.setTreeTypeId(t2Id);
		Page<IdmTreeNode> res2 = treeNodeService.find(t2Flter, null);
		assertEquals(3, res2.getTotalElements());
		//
		// Subtrees
		//
		final TreeNodeFilter subTreeFilter1 = new TreeNodeFilter();
		subTreeFilter1.setTreeNode(n1Uuid);
		subTreeFilter1.setTreeTypeId(t1Id);
		subTreeFilter1.setRecursively(true);
		Page<IdmTreeNode> res3 = treeNodeService.find(subTreeFilter1, null);
		assertEquals(3, res3.getTotalElements());
		//
		final TreeNodeFilter subTreeFilter2 = new TreeNodeFilter();
		subTreeFilter2.setTreeNode(n1Uuid);
		subTreeFilter2.setRecursively(false);
		Page<IdmTreeNode> res4 = treeNodeService.find(subTreeFilter2, null);
		assertEquals(2, res4.getTotalElements());
		//
		final TreeNodeFilter subTreeFilter3 = new TreeNodeFilter();
		subTreeFilter3.setTreeNode(r2Uuid);
		subTreeFilter3.setRecursively(false);
		Page<IdmTreeNode> res5 = treeNodeService.find(subTreeFilter3, null);
		assertEquals(1, res5.getTotalElements());
		//
		final TreeNodeFilter subTreeFilter4 = new TreeNodeFilter();
		subTreeFilter4.setTreeNode(r2Uuid);
		subTreeFilter4.setTreeTypeId(t2Id);
		subTreeFilter4.setRecursively(true);
		Page<IdmTreeNode> res6 = treeNodeService.find(subTreeFilter4, null);
		assertEquals(2, res6.getTotalElements());
		//
		final TreeNodeFilter subTreeFilter5 = new TreeNodeFilter();
		subTreeFilter5.setTreeNode(n12Uuid);
		subTreeFilter5.setTreeTypeId(t2Id);
		subTreeFilter5.setRecursively(true);
		Page<IdmTreeNode> res7 = treeNodeService.find(subTreeFilter5, null);
		assertEquals(0, res7.getTotalElements());
		//
		final TreeNodeFilter subTreeFilter6 = new TreeNodeFilter();
		subTreeFilter6.setTreeNode(n12Uuid);
		subTreeFilter6.setTreeTypeId(t2Id);
		subTreeFilter6.setRecursively(false);
		Page<IdmTreeNode> res8 = treeNodeService.find(subTreeFilter6, null);
		assertEquals(0, res8.getTotalElements());
		//
		// Fulltext
		//
		final TreeNodeFilter fullTextFilter1 = new TreeNodeFilter();
		fullTextFilter1.setText("NODE5");
		Page<IdmTreeNode> res9 = treeNodeService.find(fullTextFilter1, null);
		assertEquals(1, res9.getTotalElements());
		assertEquals(n5Uuid, res9.getContent().get(0).getId());
		//
		final TreeNodeFilter fullTextFilter2 = new TreeNodeFilter();
		fullTextFilter2.setText("NODE");
		fullTextFilter2.setTreeTypeId(t1Id);
		Page<IdmTreeNode> res10 = treeNodeService.find(fullTextFilter2, null);
		assertEquals(5, res10.getTotalElements());
		//
		final TreeNodeFilter fullTextFilter3 = new TreeNodeFilter();
		fullTextFilter3.setText("odE");
		fullTextFilter3.setTreeTypeId(t1Id);
		Page<IdmTreeNode> res13 = treeNodeService.find(fullTextFilter3, null);
		assertEquals(5, res13.getTotalElements());
		//
		// Property - value pairs
		//
		final TreeNodeFilter dynPropFilter1 = new TreeNodeFilter();
		dynPropFilter1.setProperty("name");
		dynPropFilter1.setValue("ROOT1");
		Page<IdmTreeNode> res11 = treeNodeService.find(dynPropFilter1, null);
		assertEquals(1, res11.getTotalElements());
		assertEquals(r1Uuid, res11.getContent().get(0).getId());
		//
		final TreeNodeFilter dynPropFilter2 = new TreeNodeFilter();
		dynPropFilter2.setProperty("code");
		dynPropFilter2.setValue("ROOT2");
		Page<IdmTreeNode> res12 = treeNodeService.find(dynPropFilter2, null);
		assertEquals(1, res12.getTotalElements());
		assertEquals(r2Uuid, res12.getContent().get(0).getId());

	}

	private IdmTreeNode getIdmTreeNode(IdmTreeType type, IdmTreeNode parent, String code, String name) {
		IdmTreeNode node2 = new IdmTreeNode();
		node2.setCode(code);
		node2.setName(name);
		node2.setTreeType(type);
		node2.setParent(parent);
		return node2;
	}

	private IdmTreeType getIdmTreeType(String test_type_a, String test_type_aa) {
		IdmTreeType type = new IdmTreeType();
		type.setCode(test_type_a);
		type.setName(test_type_aa);
		return type;
	}
}
