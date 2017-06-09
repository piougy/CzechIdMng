package eu.bcvsolutions.idm.core.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URLEncoder;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;

public class IdmIdentityControllerRestTest extends AbstractRestTest {
	
	@Autowired private IdmIdentityService identityService;
	
	private Authentication getAuthentication() {
		return new IdmJwtAuthentication(identityService.getByUsername(InitTestData.TEST_ADMIN_USERNAME), null, Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority()), "test");
	}
	
	@After
	public void logout() {
		SecurityContextHolder.clearContext();
	}
	
	@Test
    public void userNotFound() throws Exception {
		getMockMvc().perform(get(BaseController.BASE_PATH + "/identities/n_a_user")
        		.with(authentication(getAuthentication()))
                .contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(status().isNotFound());
    }
	
	@Test
    public void userFoundByUsername() throws Exception {
		getMockMvc().perform(get(BaseController.BASE_PATH + "/identities/" + InitTestData.TEST_ADMIN_USERNAME)
        		.with(authentication(getAuthentication()))
                .contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.username", equalTo(InitTestData.TEST_ADMIN_USERNAME)));
    }
	
	@Test
	@Ignore // TODO: url decode does not works in test ... why? 
    public void testUsernameWithSpecialCharacters() throws Exception {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername("hh.hh#./sd");
		identity.setFirstName("test");
		identity.setLastName("test");
		identity = identityService.save(identity);
		//
		getMockMvc().perform(get(BaseController.BASE_PATH + "/identities/" + URLEncoder.encode(identity.getUsername(), "UTF-8"))
        		.with(authentication(getAuthentication()))
                .contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(jsonPath("$.username", equalTo(identity.getUsername())));
    }
}
