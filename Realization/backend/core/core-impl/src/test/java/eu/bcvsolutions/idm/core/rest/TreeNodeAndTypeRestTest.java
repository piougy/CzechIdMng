package eu.bcvsolutions.idm.core.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.utils.AuthenticationTestUtils;

/**
 * Tree nodes endpoint tests
 * 
 * @author Ondřej Kopr
 *
 */
public class TreeNodeAndTypeRestTest extends AbstractRestTest {
	
	@Autowired private IdmTreeTypeRepository treeTypeRepository;
	@Autowired private IdmTreeNodeRepository treeNodeRepository;

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
