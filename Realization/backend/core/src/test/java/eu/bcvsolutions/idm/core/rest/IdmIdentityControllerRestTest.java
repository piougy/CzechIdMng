package eu.bcvsolutions.idm.core.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.AbstractRestTest;
import eu.bcvsolutions.idm.core.TestUtils;
import eu.bcvsolutions.idm.security.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.security.domain.IdmJwtAuthentication;


public class IdmIdentityControllerRestTest extends AbstractRestTest {

	public static RequestPostProcessor security() {
		DefaultGrantedAuthority superAdminRoleAuthority = new DefaultGrantedAuthority("SYSTEM_ADMIN");
		SecurityContextHolder.getContext().setAuthentication(new IdmJwtAuthentication("[SYSTEM]", null, Lists.newArrayList(superAdminRoleAuthority)));
        return SecurityMockMvcRequestPostProcessors.securityContext(SecurityContextHolder.getContext());
	}
	
	@Test
    public void userNotFound() throws Exception {
        mockMvc.perform(get("/api/identities/n_a_user")
        		.with(security())
                .contentType(TestUtils.HAL_CONTENT_TYPE))
                .andExpect(status().isNotFound());
    }
	
	@Test
    public void userFoundByUsername() throws Exception {
        mockMvc.perform(get("/api/identities/" + TestUtils.TEST_USERNAME)
        		.with(security())
                .contentType(TestUtils.HAL_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(TestUtils.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.username", equalTo(TestUtils.TEST_USERNAME)));
    }
}
