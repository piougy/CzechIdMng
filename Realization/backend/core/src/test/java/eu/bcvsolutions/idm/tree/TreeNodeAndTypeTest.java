package eu.bcvsolutions.idm.tree;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.AbstractRestTest;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.security.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.security.service.SecurityService;

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
		
		node.setName("TEST_NODE");
		
		Exception ex = null;
		try {
			treeNodeRepository.save(node);
		} catch (Exception e) {
			ex = e;
		}
		
		assertNotNull(ex);
		
		IdmTreeType type = new IdmTreeType();
		type.setName("TEST_TYPE");
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
		type.setName("TEST_TYPE_ROOT");
		treeTypeRepository.save(type);
		
		IdmTreeNode root = new IdmTreeNode();
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
		body.put("name", "TEST_ROOT_second");
		body.put("treeType", "treetypes/" + type.getId().toString());
		
		String jsonContent = toJson(body);
		
		ex = null;
		int status = 0;
		try {
			status = mockMvc.perform(post("/api/treenodes").with(authentication(getAuthentication()))
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
	public void addChildrenToParent() {
		IdmTreeType type = new IdmTreeType();
		type.setName("TEST_TYPE");
		treeTypeRepository.save(type);
		
		IdmTreeNode node1 = new IdmTreeNode();
		node1.setName("TEST_ROOT");
		node1.setTreeType(type);
		
		IdmTreeNode node2 = new IdmTreeNode();
		node2.setName("TEST_NODE_2");
		node2.setTreeType(type);
		node2.setParent(node1);
		
		IdmTreeNode node3 = new IdmTreeNode();
		node3.setName("TEST_NODE_3");
		node3.setTreeType(type);
		node3.setParent(node2);
		
		IdmTreeNode node4 = new IdmTreeNode();
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
		body.put("name", "TEST_NODE_2_update");
		body.put("parent", "treenodes/" + node4.getId().toString());
		
		String jsonContent = toJson(body);
		
		int status = 0;
		Exception ex = null;
		try {
			status = mockMvc.perform(post("/api/treenodes").with(authentication(getAuthentication()))
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
		type.setName("TEST_TYPE_1");
		treeTypeRepository.save(type);
		
		IdmTreeType type2 = new IdmTreeType();
		type2.setName("TEST_TYPE_2");
		treeTypeRepository.save(type2);
		
		// save node trought rest
		Map<String, String> body = new HashMap<>();
		body.put("name", "TEST_NODE");
		body.put("treeType", "treetypes/" + type.getId().toString());
		
		String jsonContent = toJson(body);
		
		int status = 0;
		Exception ex = null;
		// test save without privileges
		try {
			status = mockMvc.perform(post("/api/treenodes")
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
			status = mockMvc.perform(post("/api/treenodes").with(authentication(getAuthentication()))
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
		
		List<IdmTreeNode> nodes = this.treeNodeRepository.findRoots(type.getId());
		assertFalse(nodes.isEmpty());
		IdmTreeNode node = nodes.get(0);
		
		// change treeType
		body.put("id", node.getId().toString());
		body.put("name", node.getName() + "_update");
		body.put("treeType", "treetypes/" + type2.getId().toString());
		
		jsonContent = toJson(body);
		
		status = 0;
		ex = null;
		try {
			status = mockMvc.perform(post("/api/treenodes/").with(authentication(getAuthentication()))
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
