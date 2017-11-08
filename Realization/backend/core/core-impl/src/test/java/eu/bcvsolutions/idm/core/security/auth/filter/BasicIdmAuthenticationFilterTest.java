package eu.bcvsolutions.idm.core.security.auth.filter;

import static eu.bcvsolutions.idm.InitTestData.HAL_CONTENT_TYPE;
import static eu.bcvsolutions.idm.InitTestData.TEST_ADMIN_PASSWORD;
import static eu.bcvsolutions.idm.InitTestData.TEST_ADMIN_USERNAME;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;

import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.utils.AuthenticationTestUtils;

/**
 * Test authentication using the basic scheme. 
 * 
 * @author Jan Helbich
 */
public class BasicIdmAuthenticationFilterTest extends AbstractRestTest {

	@Test
	public void testBasicAuthSuccess() throws Exception {
		String basedAuth = AuthenticationTestUtils.getBasicAuth(TEST_ADMIN_USERNAME, TEST_ADMIN_PASSWORD);
		
		getMockMvc().perform(get(AuthenticationTestUtils.getSelfPath(TEST_ADMIN_USERNAME))
				.header("Authorization", "Basic " + basedAuth)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(HAL_CONTENT_TYPE))
			.andExpect(jsonPath("$.username", equalTo(TEST_ADMIN_USERNAME)));
	}
	
	@Test
	public void testBasicAuthFail() throws Exception {
		String basedAuth = AuthenticationTestUtils.getBasicAuth(TEST_ADMIN_USERNAME, "");
		
		getMockMvc().perform(get(AuthenticationTestUtils.getSelfPath(TEST_ADMIN_USERNAME))
				.header("Authorization", "Basic " + basedAuth)
				.contentType(HAL_CONTENT_TYPE))
			.andExpect(status().is(403));
	}
}
