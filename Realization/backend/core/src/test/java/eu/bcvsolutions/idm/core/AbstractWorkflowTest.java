package eu.bcvsolutions.idm.core;

import org.activiti.engine.IdentityService;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.IdmApplication;
import eu.bcvsolutions.idm.core.security.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.core.security.domain.IdmJwtAuthentication;

/**
 * 
 * Super class for activiti workflow tests
 * 
 * @author svandav
 *
 */
public abstract class AbstractWorkflowTest extends AbstractIntegrationTest {

    @Autowired @Rule
    public ActivitiRule activitiRule;
    
    @Autowired
	private IdentityService workflowIdentityService;
	
	public void login(String username){
		DefaultGrantedAuthority superAdminRoleAuthority = new DefaultGrantedAuthority("SYSTEM_ADMIN");
		SecurityContextHolder.getContext().setAuthentication(new IdmJwtAuthentication("[SYSTEM]", null, Lists.newArrayList(superAdminRoleAuthority)));
		workflowIdentityService.setAuthenticatedUserId(username);
	}
	
	public void logout(){
		SecurityContextHolder.clearContext();
		workflowIdentityService.setAuthenticatedUserId(null);
	}
}
