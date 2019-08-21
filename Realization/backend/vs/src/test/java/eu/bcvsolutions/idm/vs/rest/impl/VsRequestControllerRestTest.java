package eu.bcvsolutions.idm.vs.rest.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.activiti.engine.impl.util.json.JSONException;
import org.activiti.engine.impl.util.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.vs.TestHelper;
import eu.bcvsolutions.idm.vs.dto.VsRequestDto;
import eu.bcvsolutions.idm.vs.dto.filter.VsRequestFilter;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;

/**
 * Tests for VsRequestController and get method
 * 
 * TODO: I'm not able extends from AbstractReadWriteDtoControllerRestTest and test whole controller.
 *
 * @author Ondrej Kopr
 *
 */
public class VsRequestControllerRestTest extends AbstractRestTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private VsRequestService requestService;

	@Before
	public void login() {
		loginAsAdmin();
	}

	@Test
	public void checkGetMethodWithoutExitingRequest() throws Exception {
		String url = String.format("%s%s/%s", BaseDtoController.BASE_PATH, "/vs/requests", UUID.randomUUID());
		getMockMvc().perform(get(url)
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isNotFound());

	}

	@Test
	public void checkGetMethod() throws Exception {
		SysSystemDto virtualSystem = helper.createVirtualSystem(helper.createName());
		IdmRoleDto roleOne = helper.createRole(helper.createName());
		IdmIdentityDto identity = helper.createIdentity((GuardedString) null);
		
		// Assign system to role
		helper.createRoleSystem(roleOne, virtualSystem);
		helper.assignRoles(helper.getPrimeContract(identity.getId()), false, roleOne);
		
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(virtualSystem.getId());
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto vsRequestDto = requests.get(0);

		String url = String.format("%s%s/%s", BaseDtoController.BASE_PATH, "/vs/requests", vsRequestDto.getId());
		ResultActions andExpect = getMockMvc().perform(get(url)
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.id", equalTo(vsRequestDto.getId().toString())));
		assertNotNull(andExpect);
		String contentAsString = andExpect.andReturn().getResponse().getContentAsString();
		
		JSONObject tObject = new JSONObject(contentAsString);
		String embeddedString = tObject.get("_embedded").toString();
		tObject = new JSONObject(embeddedString);
		String requestAsString = tObject.get(IdmConceptRoleRequestService.ROLE_REQUEST_FIELD).toString();
		assertNotNull(requestAsString);

		// Request as string cannot be transformed into class because:
		// Can not construct instance of eu.bcvsolutions.idm.core.api.dto.ResultModel
		// problem: abstract types either need to be mapped to concrete types, have custom deserializer,
		// or be instantiated with additional type information
//		IdmRoleRequestDto roleRequest = mapper.readValue(requestAsString, IdmRoleRequestDto.class);
//		assertNotNull(roleRequest);

		// Creator in embbedded
		assertTrue(requestAsString.contains("\"" + AbstractEntity_.creator.getName() + "\"" + ":{"));
	}

	@Test
	public void checkGetMethodWithoutRequest() throws Exception {
		SysSystemDto virtualSystem = helper.createVirtualSystem(helper.createName());
		IdmRoleDto roleOne = helper.createRole(helper.createName());
		IdmIdentityDto identity = helper.createIdentity((GuardedString) null);
		
		// Assign system to role
		helper.createRoleSystem(roleOne, virtualSystem);
		helper.assignRoles(helper.getPrimeContract(identity.getId()), false, roleOne);
		
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(virtualSystem.getId());
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto vsRequestDto = requests.get(0);

		vsRequestDto.setRoleRequestId(null);
		vsRequestDto = requestService.save(vsRequestDto);

		String url = String.format("%s%s/%s", BaseDtoController.BASE_PATH, "/vs/requests", vsRequestDto.getId());
		ResultActions andExpect = getMockMvc().perform(get(url)
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.id", equalTo(vsRequestDto.getId().toString())));
		assertNotNull(andExpect);
		String contentAsString = andExpect.andReturn().getResponse().getContentAsString();
		
		JSONObject tObject = new JSONObject(contentAsString);
		String embeddedString = tObject.get("_embedded").toString();
		tObject = new JSONObject(embeddedString);
		try {
			tObject.get(IdmConceptRoleRequestService.ROLE_REQUEST_FIELD).toString();
			fail();
		} catch (JSONException e) {
			assertTrue(e.getMessage().contains(IdmConceptRoleRequestService.ROLE_REQUEST_FIELD));
		}
	}

	@Test
	public void checkGetMethodMissingRequest() throws Exception {
		SysSystemDto virtualSystem = helper.createVirtualSystem(helper.createName());
		IdmRoleDto roleOne = helper.createRole(helper.createName());
		IdmIdentityDto identity = helper.createIdentity((GuardedString) null);
		
		// Assign system to role
		helper.createRoleSystem(roleOne, virtualSystem);
		helper.assignRoles(helper.getPrimeContract(identity.getId()), false, roleOne);
		
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(virtualSystem.getId());
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto vsRequestDto = requests.get(0);

		vsRequestDto.setRoleRequestId(UUID.randomUUID());
		vsRequestDto = requestService.save(vsRequestDto);

		String url = String.format("%s%s/%s", BaseDtoController.BASE_PATH, "/vs/requests", vsRequestDto.getId());
		ResultActions andExpect = getMockMvc().perform(get(url)
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.id", equalTo(vsRequestDto.getId().toString())));
		assertNotNull(andExpect);
		String contentAsString = andExpect.andReturn().getResponse().getContentAsString();
		
		JSONObject tObject = new JSONObject(contentAsString);
		String embeddedString = tObject.get("_embedded").toString();
		tObject = new JSONObject(embeddedString);
		try {
			tObject.get(IdmConceptRoleRequestService.ROLE_REQUEST_FIELD).toString();
			fail();
		} catch (JSONException e) {
			assertTrue(e.getMessage().contains(IdmConceptRoleRequestService.ROLE_REQUEST_FIELD));
		}
	}
}
