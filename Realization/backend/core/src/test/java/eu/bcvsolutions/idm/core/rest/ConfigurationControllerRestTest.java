package eu.bcvsolutions.idm.core.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.AbstractRestTest;


public class ConfigurationControllerRestTest extends AbstractRestTest {
	
//	@Autowired
//	private SecurityService securityService;

//	public RequestPostProcessor adminSecurity() {
//		SecurityContextHolder.getContext().setAuthentication(new IdmJwtAuthentication("[SYSTEM]", null, securityService.getAllAvailableAuthorities()));
//        return SecurityMockMvcRequestPostProcessors.securityContext(SecurityContextHolder.getContext());
//	}
//	
//	public RequestPostProcessor userSecurity() {
//		SecurityContextHolder.getContext().setAuthentication(new IdmJwtAuthentication("[SYSTEM]", null, Lists.newArrayList()));
//        return SecurityMockMvcRequestPostProcessors.securityContext(SecurityContextHolder.getContext());
//	}
	
	@Test
    public void readAllPublic() throws Exception {
        mockMvc.perform(get(BaseEntityController.BASE_PATH + "/public/configurations")
                .contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(status().isOk());
    }
}
