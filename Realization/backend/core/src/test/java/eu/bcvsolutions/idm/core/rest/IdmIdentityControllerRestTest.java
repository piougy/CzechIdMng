package eu.bcvsolutions.idm.core.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;

import eu.bcvsolutions.idm.core.AbstractRestTest;
import eu.bcvsolutions.idm.core.TestUtils;

public class IdmIdentityControllerRestTest extends AbstractRestTest {

	@Test
    public void userNotFound() throws Exception {
        mockMvc.perform(get("/api/identities/n_a_user")
                .contentType(TestUtils.HAL_CONTENT_TYPE))
                .andExpect(status().isNotFound());
    }
	
	@Test
    public void userFoundByUsername() throws Exception {
        mockMvc.perform(get("/api/identities/" + TestUtils.TEST_USERNAME)
                .contentType(TestUtils.HAL_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(TestUtils.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.username", equalTo(TestUtils.TEST_USERNAME)));
    }
}
