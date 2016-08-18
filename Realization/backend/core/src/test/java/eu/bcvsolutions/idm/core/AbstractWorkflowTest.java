package eu.bcvsolutions.idm.core;

import org.activiti.engine.IdentityService;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;

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
	
    @Override
	public void loginAsAdmin(String username){
		super.loginAsAdmin(username);
		workflowIdentityService.setAuthenticatedUserId(username);
	}
	
    @Override
	public void logout(){
		super.logout();
		workflowIdentityService.setAuthenticatedUserId(null);
	}
}
