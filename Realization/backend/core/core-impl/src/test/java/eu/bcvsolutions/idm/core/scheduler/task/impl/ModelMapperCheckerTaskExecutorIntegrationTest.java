package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.config.ModelMapperChecker;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test ModelMapperChecker execution by LRT.
 * 
 * @author Radek Tomiška
 *
 */
@SuppressWarnings("deprecation")
@Transactional
public class ModelMapperCheckerTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	
	@Test
	public void testVerify() throws InterruptedException, ExecutionException {
		ModelMapperCheckerTaskExecutor executor = new ModelMapperCheckerTaskExecutor();
		AutowireHelper.autowire(executor);
		//
		Assert.assertTrue(longRunningTaskManager.executeSync(executor));		
	}
	
	@Test
	public void testVerifyIsDisabled() {
		try {
			getHelper().setConfigurationValue(ModelMapperChecker.PROPERTY_ENABLED, false);
			//
			ModelMapperCheckerTaskExecutor executor = new ModelMapperCheckerTaskExecutor();
			AutowireHelper.autowire(executor);
			longRunningTaskManager.executeSync(executor);
			//
			IdmLongRunningTaskDto longRunningTask = longRunningTaskManager.getLongRunningTask(executor.getLongRunningTaskId());
			Assert.assertEquals(CoreResultCode.CONFIGURATION_DISABLED.name(), longRunningTask.getResult().getModel().getStatusEnum());
		} finally {
			getHelper().setConfigurationValue(ModelMapperChecker.PROPERTY_ENABLED, ModelMapperChecker.DEFAULT_ENABLED);
		}
	}

}
