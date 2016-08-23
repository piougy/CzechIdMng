package eu.bcvsolutions.idm.workflow.deploy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.AbstractWorkflowTest;
import eu.bcvsolutions.idm.core.TestUtils;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowDeploymentService;
import eu.bcvsolutions.idm.notification.repository.IdmEmailLogRepository;

/**
 * Sipmle test for sending emails from workflow
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public class BasicEmailTest extends AbstractWorkflowTest {

	private static final String PROCESS_KEY = "testEmailer";
	private static final String EMAIL_TEXT = "wf_test";
	private static final String EMAIL_RECIPIENT = "tomiska";
	
	@Autowired
	private WorkflowDeploymentService processDeploymentService;
	
	@Autowired
	private IdmEmailLogRepository emailLogRepository;
	
	@Before
	public void login() {
		super.loginAsAdmin(TestUtils.TEST_USER_1);
	}
	
	@After
	public void logout() {
		super.logout();
	}

	/**
	 * Send email through {@link EmailService}
	 */
	@Test
	public void deployAndRunScript() {
		//Deploy process - annotation @Deployment can't be used - we need custom behavior to work
		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream("eu/bcvsolutions/idm/core/workflow/deploy/testEmailer.bpmn20.xml");
		WorkflowDeploymentDto deploymentDto = processDeploymentService.create(PROCESS_KEY, "testEmailer.bpmn20.xml", is);
		assertNotNull(deploymentDto);
		
		assertEquals(0, emailLogRepository.findByQuick(EMAIL_TEXT, null, EMAIL_RECIPIENT, null, null, null, null).getTotalElements());
		
		RuntimeService runtimeService = activitiRule.getRuntimeService();
		ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
		assertEquals(instance.getActivityId(), "endevent");
		long count = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY).count();
		assertEquals(0, count);
		assertEquals(1, emailLogRepository.findByQuick(EMAIL_TEXT, null, EMAIL_RECIPIENT, null, null, null, null).getTotalElements());
	}
}
