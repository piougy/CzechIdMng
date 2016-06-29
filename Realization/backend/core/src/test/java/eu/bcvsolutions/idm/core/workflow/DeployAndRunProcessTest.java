package eu.bcvsolutions.idm.core.workflow;

import static org.junit.Assert.assertEquals;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.bcvsolutions.idm.core.security.service.impl.DefaultLoginService;

/**
 * Test deploy and run process with groovy script
 * @author svandav
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class DeployAndRunProcessTest extends AbstractSpringTest {

	private static final Logger log = LoggerFactory.getLogger(DefaultLoginService.class);
	private static final String PROCESS_KEY = "testDeployAndRun";


	@Test
	@Deployment(resources = { "eu/bcvsolutions/idm/core/workflow/testDeployAndRun.bpmn20.xml" })
	public void deployAndRunScript() {
		RuntimeService runtimeService = activitiRule.getRuntimeService();
		ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS_KEY);
		assertEquals(instance.getActivityId(), "endevent");
		assertEquals(0, runtimeService.createProcessInstanceQuery().count());
	}
}
