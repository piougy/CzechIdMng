package eu.bcvsolutions.idm.example.service.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;

/**
 * Example service - rest tests
 * 
 * @author Radek Tomiška
 * 
 */
public class ExampleControllerRestTest extends AbstractRestTest {

	@Autowired private IdmIdentityService identityService;
	@Autowired private ModuleService moduleService;
	
	private Authentication getAuthentication() {
		return new IdmJwtAuthentication(identityService.getByUsername(InitTestData.TEST_ADMIN_USERNAME), null, Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority()), "test");
	}
	
	@Before
	public void enableModule() {
		// enable example module
		moduleService.enable(ExampleModuleDescriptor.MODULE_ID);
	}
	
	@Test
	public void testPing() throws Exception {
		String message = "test";
		getMockMvc().perform(get(BaseController.BASE_PATH + "/examples/ping")
				.with(authentication(getAuthentication()))
				.param("message", message)
				.contentType(InitTestData.HAL_CONTENT_TYPE))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.message", equalTo(message)));
    }
}
