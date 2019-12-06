package eu.bcvsolutions.idm.core.workflow.deploy;

import java.io.InputStream;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Assert;
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
		//
		Assert.assertNotNull(deploymentDto);
		filter.setText(EMAIL_TEXT);
		filter.setRecipient(EMAIL_RECIPIENT);
		Assert.assertEquals(0, emailLogService.find(filter, null).getTotalElements());
		RuntimeService runtimeService = activitiRule.getRuntimeService();
		ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
		//
		Assert.assertTrue(instance.isEnded());
		long count = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY).count();
		Assert.assertEquals(0, count);
		Assert.assertEquals(1, emailLogService.find(filter, null).getTotalElements());
	}
}
