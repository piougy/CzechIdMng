package eu.bcvsolutions.idm.core.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.security.api.service.SecurityService;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;

public class IdmIdentityControllerRestTest extends AbstractRestTest {
	
	@Autowired
	private SecurityService securityService;
	
	private Authentication getAuthentication() {
		return new IdmJwtAuthentication("[SYSTEM]", null, securityService.getAllAvailableAuthorities());
	}
	
	@After
	public void logout() {
		SecurityContextHolder.clearContext();
	}
	
	@Test
    public void userNotFound() throws Exception {
        mockMvc.perform(get(BaseEntityController.BASE_PATH + "/identities/n_a_user")
        		.with(authentication(getAuthentication()))
                .contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(status().isNotFound());
    }
	
	@Test
    public void userFoundByUsername() throws Exception {
        mockMvc.perform(get(BaseEntityController.BASE_PATH + "/identities/" + InitTestData.TEST_ADMIN_USERNAME)
        		.with(authentication(getAuthentication()))
                .contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.username", equalTo(InitTestData.TEST_ADMIN_USERNAME)));
    }
}
