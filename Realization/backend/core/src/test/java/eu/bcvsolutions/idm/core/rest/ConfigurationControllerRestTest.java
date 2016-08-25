package eu.bcvsolutions.idm.core.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.AbstractRestTest;
import eu.bcvsolutions.idm.security.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.security.service.SecurityService;


public class ConfigurationControllerRestTest extends AbstractRestTest {
	
	@Autowired
	private SecurityService securityService;

	public RequestPostProcessor adminSecurity() {
		SecurityContextHolder.getContext().setAuthentication(new IdmJwtAuthentication("[SYSTEM]", null, securityService.getAvailableAuthorities()));
        return SecurityMockMvcRequestPostProcessors.securityContext(SecurityContextHolder.getContext());
	}
	
	public RequestPostProcessor userSecurity() {
		SecurityContextHolder.getContext().setAuthentication(new IdmJwtAuthentication("[SYSTEM]", null, Lists.newArrayList()));
        return SecurityMockMvcRequestPostProcessors.securityContext(SecurityContextHolder.getContext());
	}
	
	@Test
    public void readAllPublic() throws Exception {
        mockMvc.perform(get("/api/public/configurations")
                .contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(status().isOk());
    }
}
