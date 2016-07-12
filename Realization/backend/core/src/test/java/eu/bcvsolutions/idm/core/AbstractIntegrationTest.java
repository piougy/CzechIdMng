package eu.bcvsolutions.idm.core;

import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.IdmApplication;
import eu.bcvsolutions.idm.core.security.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.core.security.domain.IdmJwtAuthentication;

/**
 * Integration test will be based on spring integration tests and testNG framework
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@ActiveProfiles("test")
@IntegrationTest("server.port:0")
@SpringApplicationConfiguration(IdmApplication.class)
@Transactional
@Rollback(true)
@Test(enabled = false)
@TestExecutionListeners(inheritListeners = false, listeners = {
	TransactionalTestExecutionListener.class,
	DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class }
)
public class AbstractIntegrationTest extends AbstractTestNGSpringContextTests {

	public void login(String username){
		DefaultGrantedAuthority superAdminRoleAuthority = new DefaultGrantedAuthority("superAdminRole");
		SecurityContextHolder.getContext().setAuthentication(new IdmJwtAuthentication(username, null, Lists.newArrayList(superAdminRoleAuthority)));
	}
	
	public void logout(){
		SecurityContextHolder.clearContext();
	}
}
