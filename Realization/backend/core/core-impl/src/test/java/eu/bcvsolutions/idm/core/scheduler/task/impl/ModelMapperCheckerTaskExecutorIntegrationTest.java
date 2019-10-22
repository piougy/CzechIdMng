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
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test ModelMapperChecker execution by LRT.
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class ModelMapperCheckerTaskExecutorIntegrationTest  extends AbstractIntegrationTest {
	
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	
	@Test
	public void testVerify() throws InterruptedException, ExecutionException {
		ModelMapperCheckerTaskExecutor executor = new ModelMapperCheckerTaskExecutor();
		AutowireHelper.autowire(executor);
		LongRunningFutureTask<Boolean> execute = longRunningTaskManager.execute(executor);
		//
		Assert.assertTrue(execute.getFutureTask().get());		
	}
	
	@Test
	public void testVerifyIsDisabled() {
		try {
			getHelper().setConfigurationValue(ModelMapperChecker.PROPERTY_ENABLED, false);
			//
			ModelMapperCheckerTaskExecutor executor = new ModelMapperCheckerTaskExecutor();
			AutowireHelper.autowire(executor);
			LongRunningFutureTask<Boolean> execute = longRunningTaskManager.execute(executor);
			//
			IdmLongRunningTaskDto longRunningTask = longRunningTaskManager.getLongRunningTask(execute.getExecutor().getLongRunningTaskId());
			Assert.assertEquals(CoreResultCode.CONFIGURATION_DISABLED.name(), longRunningTask.getResult().getModel().getStatusEnum());
		} finally {
			getHelper().setConfigurationValue(ModelMapperChecker.PROPERTY_ENABLED, ModelMapperChecker.DEFAULT_ENABLED);
		}
	}

}
