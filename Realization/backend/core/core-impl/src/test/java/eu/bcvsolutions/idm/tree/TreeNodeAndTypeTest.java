package eu.bcvsolutions.idm.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

import eu.bcvsolutions.idm.core.AbstractRestTest;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.security.api.service.SecurityService;
import eu.bcvsolutions.idm.security.domain.IdmJwtAuthentication;

public class TreeNodeAndTypeTest extends AbstractRestTest {
	
	@Autowired
	private IdmTreeTypeRepository treeTypeRepository;
	
	@Autowired
	private IdmTreeNodeRepository treeNodeRepository;
	
	@Autowired
	private SecurityService securityService;
	
	@Test
	public void testCreateNode() {
		IdmTreeNode node = new IdmTreeNode();
		
		node.setCode("TEST_NODE");
		node.setName("TEST_NODE");
		
		Exception ex = null;
		try {
			treeNodeRepository.save(node);
		} catch (Exception e) {
			ex = e;
		}
		
		assertNotNull(ex);
		
		IdmTreeType type = new IdmTreeType();
		type.setCode("TEST_TYPE_A");
		type.setName("TEST_TYPE_A");
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
		IdmTreeType type = new IdmTreeType();
		type.setCode("TEST_TYPE_ROOT");
		type.setName("TEST_TYPE_ROOT");
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
			status = mockMvc.perform(post(BaseEntityController.BASE_PATH + "/tree/nodes").with(authentication(getAuthentication()))
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
		IdmTreeType type = new IdmTreeType();
		type.setCode("TEST_TYPE");
		type.setName("TEST_TYPE");
		treeTypeRepository.save(type);
		
		IdmTreeNode node1 = new IdmTreeNode();
		node1.setCode("TEST_ROOT");
		node1.setName("TEST_ROOT");
		node1.setTreeType(type);
		
		IdmTreeNode node2 = new IdmTreeNode();
		node2.setCode("TEST_NODE_2");
		node2.setName("TEST_NODE_2");
		node2.setTreeType(type);
		node2.setParent(node1);
		
		IdmTreeNode node3 = new IdmTreeNode();
		node3.setCode("TEST_NODE_3");
		node3.setName("TEST_NODE_3");
		node3.setTreeType(type);
		node3.setParent(node2);
		
		IdmTreeNode node4 = new IdmTreeNode();
		node4.setCode("TEST_NODE_4");
		node4.setName("TEST_NODE_4");
		node4.setTreeType(type);
		node4.setParent(node3);
		
		treeNodeRepository.save(node1);
		treeNodeRepository.save(node2);
		treeNodeRepository.save(node3);
		treeNodeRepository.save(node4);
		
		// set parent of node4 to his children
		Map<String, String> body = new HashMap<>();
		body.put("id", node2.getId().toString());
		body.put("code", "TEST_NODE_2_update");
		body.put("name", "TEST_NODE_2_update");
		body.put("treeType", "tree/types/" + node4.getTreeType().getId().toString());
		body.put("parent", "tree/nodes/" + node4.getId().toString());
		
		String jsonContent = toJson(body);
		
		int status = 0;
		Exception ex = null;
		try {
			status = mockMvc.perform(post(BaseEntityController.BASE_PATH + "/tree/nodes").with(authentication(getAuthentication()))
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
		IdmTreeType type = new IdmTreeType();
		type.setCode("TEST_TYPE_1");
		type.setName("TEST_TYPE_1");
		treeTypeRepository.save(type);
		
		IdmTreeType type2 = new IdmTreeType();
		type2.setCode("TEST_TYPE_2");
		type2.setName("TEST_TYPE_2");
		treeTypeRepository.save(type2);
		
		// save node trought rest
		Map<String, String> body = new HashMap<>();
		body.put("code", "TEST_NODE");
		body.put("name", "TEST_NODE");
		body.put("treeType", "treetypes/" + type.getId().toString());
		
		String jsonContent = toJson(body);
		
		int status = 0;
		Exception ex = null;
		// test save without privileges
		try {
			status = mockMvc.perform(post(BaseEntityController.BASE_PATH + "/treenodes")
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
			status = mockMvc.perform(post(BaseEntityController.BASE_PATH + "/tree/nodes").with(authentication(getAuthentication()))
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
		body.put("treeType", "tree/types/" + type2.getId().toString());
		
		jsonContent = toJson(body);
		
		status = 0;
		ex = null;
		try {
			status = mockMvc.perform(post(BaseEntityController.BASE_PATH + "/tree/nodes/").with(authentication(getAuthentication()))
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
	
	private Authentication getAuthentication() {
		return new IdmJwtAuthentication("[SYSTEM]", null, securityService.getAllAvailableAuthorities());
	}
}
