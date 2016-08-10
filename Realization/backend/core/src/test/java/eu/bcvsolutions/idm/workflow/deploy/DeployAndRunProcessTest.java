package eu.bcvsolutions.idm.core.workflow.deploy;

import static org.junit.Assert.assertEquals;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.bcvsolutions.idm.core.AbstractWorkflowTest;

/**
 * Test deploy and run process with groovy script
 * @author svandav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class DeployAndRunProcessTest extends AbstractWorkflowTest {

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
