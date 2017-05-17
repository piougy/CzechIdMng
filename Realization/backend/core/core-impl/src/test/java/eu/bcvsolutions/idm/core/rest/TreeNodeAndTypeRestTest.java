package eu.bcvsolutions.idm.core.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.filter.TreeNodeFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.utils.AuthenticationTestUtils;

public class TreeNodeAndTypeRestTest extends AbstractRestTest {
	
	@Autowired
	private IdmTreeTypeRepository treeTypeRepository;
	
	@Autowired
	private IdmTreeNodeRepository treeNodeRepository;

	@Autowired
	IdmTreeNodeService treeNodeService;
	
	@Test
	public void testCreateNode() {
		IdmTreeNode node = getIdmTreeNode(null, null, "TEST_NODE", "TEST_NODE");
		
		Exception ex = null;
		try {
			treeNodeRepository.save(node);
		} catch (Exception e) {
			ex = e;
		}
		
		assertNotNull(ex);

		IdmTreeType type = getIdmTreeType("TEST_TYPE_A", "TEST_TYPE_A");
		treeTypeRepository.save(type);
		node.setTreeType(type);
		
		ex = null;
		try {
			treeNodeRepository.save(node);
		} catch (Exception e) {
			ex = e;
		}
		
		assertNull(ex);
	}

	@Test
	public void testCreateRootNodeTwice() {
		IdmTreeType type = getIdmTreeType("TEST_TYPE_ROOT", "TEST_TYPE_ROOT");
		treeTypeRepository.save(type);
		
		IdmTreeNode root = new IdmTreeNode();
		root.setCode("TEST_ROOT");
		root.setName("TEST_ROOT");
		root.setTreeType(type);
		
		// save first root
		Exception ex = null;
		try {
			treeNodeRepository.save(root);
		} catch (Exception e) {
			ex = e;
		}
		
		assertNull(ex);
		
		// save second root, same type
		Map<String, String> body = new HashMap<>();
		body.put("code", "TEST_ROOT_second");
		body.put("name", "TEST_ROOT_second");
		body.put("treeType", "treetypes/" + type.getId().toString());
		
		String jsonContent = toJson(body);
		
		ex = null;
		int status = 0;
		try {
			status = getMockMvc().perform(post(BaseEntityController.BASE_PATH + "/tree-nodes")
					.with(authentication(getAuthentication()))
					.content(jsonContent)
					.contentType(MediaType.APPLICATION_JSON))
					.andReturn()
					.getResponse()
					.getStatus();
		} catch (Exception e) {
			ex = e;
		}
		assertNull(ex);
		assertEquals(201, status);
	}
	
	@Test
	public void addChildrenToParent() {
		IdmTreeType type = getIdmTreeType("TEST_TYPE", "TEST_TYPE");
		treeTypeRepository.save(type);
		
		IdmTreeNode node1 = getIdmTreeNode(type, null, "TEST_ROOT", "TEST_ROOT");
		IdmTreeNode node2 = getIdmTreeNode(type, node1, "TEST_NODE_2", "TEST_NODE_2");
		IdmTreeNode node3 = getIdmTreeNode(type, node2, "TEST_NODE_3", "TEST_NODE_2");
		IdmTreeNode node4 = getIdmTreeNode(type, node3, "TEST_NODE_4", "TEST_NODE_2");
		
		treeNodeRepository.save(node1);
		treeNodeRepository.save(node2);
		treeNodeRepository.save(node3);
		treeNodeRepository.save(node4);
		
		// set parent of node4 to his children
		Map<String, String> body = new HashMap<>();
		body.put("id", node2.getId().toString());
		body.put("code", "TEST_NODE_2_update");
		body.put("name", "TEST_NODE_2_update");
		body.put("treeType", "treeTypes/" + node4.getTreeType().getId().toString());
		body.put("parent", "treeNodes/" + node4.getId().toString());
		
		String jsonContent = toJson(body);
		
		int status = 0;
		Exception ex = null;
		try {
			status = getMockMvc().perform(post(BaseEntityController.BASE_PATH + "/tree-nodes")
					.with(authentication(getAuthentication()))
					.content(jsonContent)
					.contentType(MediaType.APPLICATION_JSON))
					.andReturn()
					.getResponse()
					.getStatus();
		} catch (Exception e) {
			ex = e;
		}
		assertNull(ex);
		assertEquals(400, status);
	}

	@Test
	public void changeType() {
		IdmTreeType type = getIdmTreeType("TEST_TYPE_1", "TEST_TYPE_1");
		treeTypeRepository.save(type);

		IdmTreeType type2 = getIdmTreeType("TEST_TYPE_2", "TEST_TYPE_2");
		treeTypeRepository.save(type2);
		
		// save node trought rest
		Map<String, String> body = new HashMap<>();
		body.put("code", "TEST_NODE");
		body.put("name", "TEST_NODE");
		body.put("treeType", "treeTypes/" + type.getId().toString());
		
		String jsonContent = toJson(body);
		
		int status = 0;
		Exception ex = null;
		// test save without privileges
		try {
			status = getMockMvc().perform(post(BaseEntityController.BASE_PATH + "/tree-nodes")
					.content(jsonContent)
					.contentType(MediaType.APPLICATION_JSON))
					.andReturn()
					.getResponse()
					.getStatus();
		} catch (Exception e) {
			ex = e;
		}
		assertNull(ex);
		assertEquals(403, status);
		
		// test with privileges
		try {
			status = getMockMvc().perform(post(BaseEntityController.BASE_PATH + "/tree-nodes")
					.with(authentication(getAuthentication()))
					.content(jsonContent)
					.contentType(MediaType.APPLICATION_JSON))
					.andReturn()
					.getResponse()
					.getStatus();
		} catch (Exception e) {
			ex = e;
		}
		assertNull(ex);
		assertEquals(201, status);
		
		Page<IdmTreeNode> nodes = this.treeNodeRepository.findChildren(type.getId(), null, new PageRequest(0, 1));
		assertFalse(nodes.getContent().isEmpty());
		IdmTreeNode node = nodes.getContent().get(0);
		
		// change treeType
		body.put("id", node.getId().toString());
		body.put("name", node.getName() + "_update");
		body.put("treeType", "tree-types/" + type2.getId().toString());
		
		jsonContent = toJson(body);
		
		status = 0;
		ex = null;
		try {
			status = getMockMvc().perform(post(BaseEntityController.BASE_PATH + "/tree-nodes/")
					.with(authentication(getAuthentication()))
					.content(jsonContent)
					.contentType(MediaType.APPLICATION_JSON))
					.andReturn()
					.getResponse()
					.getStatus();
		} catch (Exception e) {
			ex = e;
		}
		assertEquals(400, status);
		assertNull(ex);
	}


	@Test
	public void testFilters() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		// ****** PREPARE DATA ******
		final IdmTreeType t1 = getIdmTreeType("TYPE1", "TYPE1");
		final IdmTreeType t2 = getIdmTreeType("TYPE2", "TYPE2");
		final UUID t1Id = treeTypeRepository.save(t1).getId();
		final UUID t2Id = treeTypeRepository.save(t2).getId();
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
		fullTextFilter2.setText("NODE%");
		Page<IdmTreeNode> res10 = treeNodeService.find(fullTextFilter2, null);
		assertEquals(7, res10.getTotalElements());
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

	private String toJson(Map<String, String> body) {
		final ObjectMapper mapper = new ObjectMapper();
		String json = null;
		try {
			json = mapper.writeValueAsString(body);
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
		return json;
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
	
	private Authentication getAuthentication() {
		return AuthenticationTestUtils.getSystemAuthentication();
	}
}
