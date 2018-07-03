package eu.bcvsolutions.idm.core.scheduler.task.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventExceptionService;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventPropertyService;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.audit.task.impl.RemoveOldLogsTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.ObserveLongRunningTaskEndProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.service.impl.DefaultSchedulerManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for {@link RemoveOldLogsTaskExecutor}.
 * 
 * TODO: now db appender doesn't support h2 connection.
 * 
 * @author Ondrej Kopr <kopr@xy xy.cz>
 *
 */

public class RemoveOldLogsTaskExecutorTest extends AbstractIntegrationTest {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RemoveOldLogsTaskExecutorTest.class);
	
	@Autowired
	private IdmLoggingEventService loggingEventService;
	@Autowired
	private IdmLoggingEventExceptionService loggingEventExceptionService;
	@Autowired
	private IdmLoggingEventPropertyService loggingEventPropertyService;
	@Autowired
	private ApplicationContext context;
	@Autowired
	private ConfigurationService configurationService;

	private DefaultSchedulerManager manager;

	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultSchedulerManager.class);
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	@Ignore
	public void clearCurrentLogs() throws InterruptedException {
		// generate some logs
		String logError = "Test log error - " + System.currentTimeMillis();
		String logWarn = "Test log warning - " + System.currentTimeMillis();
		LOG.error(logError);
		LOG.warn(logWarn);

		long eventsCount = loggingEventService.find(null).getTotalElements();
		long eventsExceptionCount = loggingEventExceptionService.find(null).getTotalElements();
		long eveantsPropertyCount = loggingEventPropertyService.find(null).getTotalElements();

		Task task = new Task();
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(RemoveOldLogsTaskExecutor.class);
		task.addParameter("removeRecordOlderThan", "1");
		task = manager.createTask(task);

		ObserveLongRunningTaskEndProcessor.listenTask(task.getId());
		manager.runTask(task.getId());
		ObserveLongRunningTaskEndProcessor.waitForEnd(task.getId());
		OperationResult result = ObserveLongRunningTaskEndProcessor.getResult(task.getId());
		
		assertEquals(OperationState.EXECUTED, result.getState());
		
		long eventsCount2 = loggingEventService.find(null).getTotalElements();
		long eventsExceptionCount2 = loggingEventExceptionService.find(null).getTotalElements();
		long eventsPropertyCount2 = loggingEventPropertyService.find(null).getTotalElements();
		
		// Logback doesn't support H2 connection
		assertNotEquals(eventsCount, eventsCount2);
		assertNotEquals(eventsExceptionCount, eventsExceptionCount2);
		assertNotEquals(eveantsPropertyCount, eventsPropertyCount2);
	}
}
