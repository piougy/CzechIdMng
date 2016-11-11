package eu.bcvsolutions.idm.core;

import java.io.InputStream;

import org.activiti.engine.IdentityService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import eu.bcvsolutions.idm.core.workflow.domain.CustomActivityBehaviorFactory;
import eu.bcvsolutions.idm.workflow.api.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.workflow.api.service.WorkflowDeploymentService;

/**
 * 
 * Super class for activiti workflow tests
 * 
 * @author svandav
 *
 */
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
		CustomActivityBehaviorFactory customActivityBehaviorFactory = new CustomActivityBehaviorFactory();
		beanFactory.autowireBean(customActivityBehaviorFactory);
		// Evaluate expression in workflow
		customActivityBehaviorFactory.setExpressionManager(((SpringProcessEngineConfiguration) processEngineConfiguration).getExpressionManager());
		// For catch email
		((SpringProcessEngineConfiguration) processEngineConfiguration).getBpmnParser().setActivityBehaviorFactory(customActivityBehaviorFactory);
    }
	
    @Override
	public void loginAsAdmin(String username){
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
		return  processDeploymentService.create(xmlPath,
				xmlPath, is);
	}
}
