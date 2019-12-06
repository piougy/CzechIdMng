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
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.rest.impl.IdmTreeNodeControllerRestTest;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;

/**
 * Tree nodes endpoint tests
 * 
 * TODO: move to {@link IdmTreeNodeControllerRestTest}
 * 
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
@Transactional
public class TreeNodeAndTypeRestTest extends AbstractRestTest {
	
	@Autowired private IdmTreeNodeRepository treeNodeRepository;
	@Autowired private IdmTreeNodeService treeNodeService;

	@Test
	public void testCreateRootNodeTwice() {
		IdmTreeTypeDto type = getHelper().createTreeType();
		
		IdmTreeNodeDto root = new IdmTreeNodeDto();
		root.setCode(getHelper().createName());
		root.setName(getHelper().createName());
		root.setTreeType(type.getId());
		
		// save first root
		Exception ex = null;
		try {
			treeNodeService.save(root);
		} catch (Exception e) {
			ex = e;
		}
		
		assertNull(ex);
		
		// save second root, same type
		Map<String, String> body = new HashMap<>();
		body.put("code", getHelper().createName());
		body.put("name", getHelper().createName());
		body.put("treeType", type.getId().toString());
		
		String jsonContent = toJson(body);
		
		ex = null;
		int status = 0;
		try {
			status = getMockMvc().perform(post(BaseDtoController.BASE_PATH + "/tree-nodes")
					.with(authentication(getAdminAuthentication()))
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
		IdmTreeTypeDto type = getHelper().createTreeType();
		
		IdmTreeNodeDto node1 = getHelper().createTreeNode(type, null);
		IdmTreeNodeDto node2 = getHelper().createTreeNode(type, node1);
		IdmTreeNodeDto node3 = getHelper().createTreeNode(type, node2);
		IdmTreeNodeDto node4 = getHelper().createTreeNode(type, node3);
		
		// set parent of node4 to his children
		Map<String, String> body = new HashMap<>();
		body.put("id", node2.getId().toString());
		body.put("code", "TEST_NODE_2_update");
		body.put("name", "TEST_NODE_2_update");
		body.put("treeType", node4.getTreeType().toString());
		body.put("parent", node4.getId().toString());
		
		String jsonContent = toJson(body);
		
		int status = 0;
		Exception ex = null;
		try {
			status = getMockMvc().perform(post(BaseDtoController.BASE_PATH + "/tree-nodes")
					.with(authentication(getAdminAuthentication()))
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
		IdmTreeTypeDto type = getHelper().createTreeType();
		IdmTreeTypeDto type2 = getHelper().createTreeType();
		
		// save node trought rest
		Map<String, String> body = new HashMap<>();
		body.put("code", getHelper().createName());
		body.put("name", getHelper().createName());
		body.put("treeType", type.getId().toString());
		
		String jsonContent = toJson(body);
		
		int status = 0;
		Exception ex = null;
		// test save without privileges
		try {
			status = getMockMvc().perform(post(BaseDtoController.BASE_PATH + "/tree-nodes")
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
			status = getMockMvc().perform(post(BaseDtoController.BASE_PATH + "/tree-nodes")
					.with(authentication(getAdminAuthentication()))
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
		
		Page<IdmTreeNode> nodes = this.treeNodeRepository.findChildren(type.getId(), null, PageRequest.of(0, 1));
		assertFalse(nodes.getContent().isEmpty());
		IdmTreeNode node = nodes.getContent().get(0);
		
		// change treeType
		body.put("id", node.getId().toString());
		body.put("name", node.getName() + "_update");
		body.put("treeType", type2.getId().toString());
		
		jsonContent = toJson(body);
		
		status = 0;
		ex = null;
		try {
			status = getMockMvc().perform(post(BaseDtoController.BASE_PATH + "/tree-nodes/")
					.with(authentication(getAdminAuthentication()))
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
}
