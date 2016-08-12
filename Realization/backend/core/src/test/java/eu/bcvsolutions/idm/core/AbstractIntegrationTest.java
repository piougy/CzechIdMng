package eu.bcvsolutions.idm.core;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.IdmApplication;
import eu.bcvsolutions.idm.security.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.security.domain.IdmJwtAuthentication;

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
@ActiveProfiles("test")
@Rollback(true)
@Transactional
public abstract class AbstractIntegrationTest {
	
	public void login(String username){
		// TODO: SYSTEM context
		SecurityContextHolder.getContext().setAuthentication(new IdmJwtAuthentication(
				"[SYSTEM]",
				null, 
				Lists.newArrayList(
						new DefaultGrantedAuthority("SYSTEM_ADMIN"),
						new DefaultGrantedAuthority("CONFIGURATION_WRITE")
						)));
	}
	
	public void logout(){
		SecurityContextHolder.clearContext();
	}
}
