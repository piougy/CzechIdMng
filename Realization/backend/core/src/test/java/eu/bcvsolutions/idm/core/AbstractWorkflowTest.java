package eu.bcvsolutions.idm.core;

import java.io.InputStream;

import org.activiti.engine.IdentityService;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowDeploymentService;

/**
 * 
 * Super class for activiti workflow tests
 * 
 * @author svandav
 *
 */
public abstract class AbstractWorkflowTest extends AbstractIntegrationTest {

	@Autowired
	@Rule
	public ActivitiRule activitiRule;

	@Autowired
	private IdentityService workflowIdentityService;

	@Autowired
	private WorkflowDeploymentService processDeploymentService;

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
		return  processDeploymentService.create(xmlPath,
				xmlPath, is);
	}
}
