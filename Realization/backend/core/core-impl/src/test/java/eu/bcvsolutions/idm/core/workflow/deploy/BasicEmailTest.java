package eu.bcvsolutions.idm.core.workflow.deploy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.EmailNotificationSender;
import eu.bcvsolutions.idm.core.notification.api.service.IdmEmailLogService;
import eu.bcvsolutions.idm.core.workflow.api.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.core.workflow.api.service.WorkflowDeploymentService;

/**
 * Sipmle test for sending emails from workflow
 * 
 * @author Radek Tomiška 
 *
 */
public class BasicEmailTest extends AbstractCoreWorkflowIntegrationTest {

	private static final String PROCESS_KEY = "testEmailer";
	private static final String EMAIL_TEXT = "wf_test";
	private static final String EMAIL_RECIPIENT = InitTestData.TEST_USER_1;
	//
	@Autowired private WorkflowDeploymentService processDeploymentService;
	@Autowired private IdmEmailLogService emailLogService;
	
	@Before
	public void login() {
		super.loginAsAdmin(InitTestData.TEST_USER_1);
	}
	
	@After
	public void logout() {
		super.logout();
	}

	/**
	 * Send email through {@link EmailNotificationSender}
	 */
	@Test
	public void testSendEmailFromWorkflow() {
		//Deploy process - annotation @Deployment can't be used - we need custom behavior to work
		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream("eu/bcvsolutions/idm/workflow/deploy/testEmailer.bpmn20.xml");
		WorkflowDeploymentDto deploymentDto = processDeploymentService.create(PROCESS_KEY, "testEmailer.bpmn20.xml", is);
		IdmNotificationFilter filter = new IdmNotificationFilter();
		
		assertNotNull(deploymentDto);
		filter.setText(EMAIL_TEXT);
		filter.setRecipient(EMAIL_RECIPIENT);
		assertEquals(0, emailLogService.find(filter, null).getTotalElements());
		RuntimeService runtimeService = activitiRule.getRuntimeService();
		ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
		assertEquals(instance.getActivityId(), "endevent");
		long count = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY).count();
		assertEquals(0, count);
		assertEquals(1, emailLogService.find(filter, null).getTotalElements());
	}
}
