package eu.bcvsolutions.idm.core.workflow.deploy;

import static org.junit.Assert.assertEquals;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;

/**
 * Test deploy and run process with groovy script
 * 
 * @author svandav
 * @author Radek Tomiška
 */
public class DeployAndRunProcessTest extends AbstractCoreWorkflowIntegrationTest {

	private static final String PROCESS_KEY = "testDeployAndRun";
	private static final String TEST_PROCESS_KEY_OVERRIDE = "testOverride";
	private static final String TEST_PROCESS_KEY_TWO = "testEmailerTwo";
	//
	@Autowired private WorkflowProcessDefinitionService definitionService;

	@Test
	@Deployment(resources = { "eu/bcvsolutions/idm/workflow/deploy/testDeployAndRun.bpmn20.xml" })
	public void deployAndRunScript() {
		RuntimeService runtimeService = activitiRule.getRuntimeService();
		ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
		assertEquals(instance.getActivityId(), "endevent");
		long count = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY).count();
		assertEquals(0, count);
	}
	
	@Test
	public void testMultipleAutoDeployDefinitionLocations() {
		WorkflowProcessDefinitionDto definitionTwo = definitionService.getByName(TEST_PROCESS_KEY_TWO);
		//
		Assert.assertNotNull(definitionTwo);
	}
	
	@Test
	public void testOverrideAutoDeployedDefinition() {
		WorkflowProcessDefinitionDto definitionOne = definitionService.getByName(TEST_PROCESS_KEY_OVERRIDE);
		//
		Assert.assertNotNull(definitionOne);
		Assert.assertEquals("Process for test deploy and run Override", definitionOne.getName());
	}
}
