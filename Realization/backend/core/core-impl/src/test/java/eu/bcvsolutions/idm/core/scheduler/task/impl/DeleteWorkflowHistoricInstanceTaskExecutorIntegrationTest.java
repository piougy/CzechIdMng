package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;

/**
 * LRT integration test.
 * 
 * @author Radek Tomiška
 *
 */
public class DeleteWorkflowHistoricInstanceTaskExecutorIntegrationTest extends AbstractCoreWorkflowIntegrationTest {

	private static final String PROCESS_KEY = "testDeployAndRun";
	private static final String PROCESS_KEY_OTHER = "testDeployAndRunOther";
	//
	@Autowired private ProcessEngine processEngine;
	@Autowired private LongRunningTaskManager longRunningTaskManager;

	@Test
	@Deployment(resources = { 
			"eu/bcvsolutions/idm/workflow/deploy/testDeployAndRun.bpmn20.xml",
			"eu/bcvsolutions/idm/workflow/deploy/testDeployAndRunOther.bpmn20.xml"
	})
	public void testDeleteOldWorkflowHistoricInstances() {
		ProcessInstance instanceOne = processEngine.getRuntimeService().startProcessInstanceByKey(PROCESS_KEY);
		ProcessInstance instanceTwo = processEngine.getRuntimeService().startProcessInstanceByKey(PROCESS_KEY);
		ProcessInstance instanceOther = processEngine.getRuntimeService().startProcessInstanceByKey(PROCESS_KEY_OTHER);
		Assert.assertTrue(instanceOne.isEnded());
		Assert.assertTrue(instanceTwo.isEnded());
		Assert.assertTrue(instanceOther.isEnded());
		Assert.assertTrue(exists(instanceOne));
		Assert.assertTrue(exists(instanceTwo));
		Assert.assertTrue(exists(instanceOther));
		//
		DeleteWorkflowHistoricInstanceTaskExecutor taskExecutor = new DeleteWorkflowHistoricInstanceTaskExecutor();
		Map<String, Object> properties = new HashMap<>();
		properties.put(DeleteNotificationTaskExecutor.PARAMETER_NUMBER_OF_DAYS, 1);
		properties.put(DeleteWorkflowHistoricInstanceTaskExecutor.PARAMETER_PROCESS_DEFINITION_KEY, PROCESS_KEY);
		AutowireHelper.autowire(taskExecutor);
		taskExecutor.init(properties);
		//
		longRunningTaskManager.execute(taskExecutor);
		//
		Assert.assertTrue(exists(instanceOne));
		Assert.assertTrue(exists(instanceTwo));
		Assert.assertTrue(exists(instanceOther));
		//
		properties.put(DeleteNotificationTaskExecutor.PARAMETER_NUMBER_OF_DAYS, 0);
		taskExecutor = new DeleteWorkflowHistoricInstanceTaskExecutor();
		AutowireHelper.autowire(taskExecutor);
		taskExecutor.init(properties);
		//
		longRunningTaskManager.execute(taskExecutor);
		//
		Assert.assertFalse(exists(instanceOne));
		Assert.assertFalse(exists(instanceTwo));
		Assert.assertTrue(exists(instanceOther));
	}
	
	public boolean exists(ProcessInstance instanceOne) {
		return processEngine
				.getHistoryService()
				.createHistoricProcessInstanceQuery()
				.processInstanceId(instanceOne.getId())
				.count() > 0;
	}
}