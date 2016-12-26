package eu.bcvsolutions.idm.core.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;

/**
 * Configuration controller tests
 * 
 * @author Radek Tomiška
 *
 */
public class ConfigurationControllerRestTest extends AbstractRestTest {
	
	@Test
    public void readAllPublic() throws Exception {
        getMockMvc().perform(get(BaseEntityController.BASE_PATH + "/public/configurations")
                .contentType(InitTestData.HAL_CONTENT_TYPE))
                .andExpect(status().isOk());
    }
}
