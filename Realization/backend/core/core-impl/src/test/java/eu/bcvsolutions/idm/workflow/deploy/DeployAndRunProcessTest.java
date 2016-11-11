package eu.bcvsolutions.idm.workflow.deploy;

import static org.junit.Assert.assertEquals;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.junit.Test;

import eu.bcvsolutions.idm.core.test.AbstractWorkflowIntegrationTest;

/**
 * Test deploy and run process with groovy script
 * @author svandav
 *
 */
public class DeployAndRunProcessTest extends AbstractWorkflowIntegrationTest {

	private static final String PROCESS_KEY = "testDeployAndRun";

	@Test
	@Deployment(resources = { "eu/bcvsolutions/idm/core/workflow/deploy/testDeployAndRun.bpmn20.xml" })
	public void deployAndRunScript() {
		RuntimeService runtimeService = activitiRule.getRuntimeService();
		ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
		assertEquals(instance.getActivityId(), "endevent");
		long count = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY).count();
		assertEquals(0, count);
	}
}
