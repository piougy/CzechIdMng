package eu.bcvsolutions.idm.core;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import eu.bcvsolutions.idm.IdmApplication;

/**
 * Test rest services will be based on spring integration tests with MockMvc / hamcrest and junit test framework
 * 
 * http://docs.spring.io/spring-framework/docs/current/spring-framework-reference/html/integration-testing.html
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IdmApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class AbstractRestTest {

	protected MockMvc mockMvc;
	
	@Autowired
    private WebApplicationContext webApplicationContext;
	
	@Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
	}
}
