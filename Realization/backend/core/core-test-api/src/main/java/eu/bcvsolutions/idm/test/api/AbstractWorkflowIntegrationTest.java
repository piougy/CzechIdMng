package eu.bcvsolutions.idm.test.api;

import java.io.InputStream;

import org.activiti.engine.IdentityService;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import eu.bcvsolutions.idm.core.workflow.api.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.core.workflow.api.service.WorkflowDeploymentService;

/**
 * 
 * Super class for activiti workflow tests
 * 
 * @author svandav
 *
 */
@Ignore
public abstract class AbstractWorkflowIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	@Rule
	public ActivitiRule activitiRule;
	@Autowired
	private IdentityService workflowIdentityService;
	@Autowired
	private WorkflowDeploymentService processDeploymentService;
	@Autowired
	private AutowireCapableBeanFactory beanFactory;
    
	/**
	 * Behavior injection from configuration doesn't work - we need to initialize it manually
	 */
	@Before
    public void initBehavior() {
    	ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl)activitiRule.getProcessEngine().getProcessEngineConfiguration();
    	DefaultActivityBehaviorFactory activityBehaviorFactory = getBehaviourFactory();
		beanFactory.autowireBean(activityBehaviorFactory);
		// Evaluate expression in workflow
		activityBehaviorFactory.setExpressionManager(((SpringProcessEngineConfiguration) processEngineConfiguration).getExpressionManager());
		// For catch email
		((SpringProcessEngineConfiguration) processEngineConfiguration).getBpmnParser().setActivityBehaviorFactory(activityBehaviorFactory);
    }
	
    @Override
	public void loginAsAdmin() {
    	super.loginAsAdmin();
    	workflowIdentityService.setAuthenticatedUserId(TestHelper.ADMIN_USERNAME);
	}
    
    /**
	 *  User will be logged as APP_ADMIN
	 *  
	 *  Lookout: security context is mocked
	 *  Better way is to prepare concrete identity in your test
	 * 
	 * @param username
	 */
    @Override
	public void loginAsAdmin(String username) {
    	super.loginAsAdmin(username);
		workflowIdentityService.setAuthenticatedUserId(username);
	}

	@Override
	public void logout() {
		super.logout();
		workflowIdentityService.setAuthenticatedUserId(null);
	}

	/**
	 * Deploy process by definition file path
	 * @param xmlPath
	 * @return
	 */
	public WorkflowDeploymentDto deployProcess(String xmlPath) {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(xmlPath);
		return processDeploymentService.create(xmlPath, xmlPath, is);
	}
	
	public abstract DefaultActivityBehaviorFactory getBehaviourFactory();
	
}
