package eu.bcvsolutions.idm.core.scheduler.task.impl.password;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.TestHelper;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmScheduledTaskService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.utils.SchedulerTestUtils;

/**
 * Send password warning mesage test
 * 
 * @author Radek Tomiška
 *
 */
public class PasswordExpirationWarningIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private TestHelper helper;
	@Autowired private IdmPasswordService passwordService;
	@Autowired private IdmScheduledTaskService scheduledTaskService;
	@Autowired private IdmLongRunningTaskService longRunningService;
	@Autowired private IdmProcessedTaskItemService itemService;
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testSimpleWarningMessageDry() {
		// prepare date
		IdmIdentityDto identity = helper.createIdentity();
		IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
		password.setValidTill(new LocalDate().plusDays(1));		
		passwordService.save(password);
		// prepare task
		IdmScheduledTaskDto scheduledTask = scheduledTaskService.save(SchedulerTestUtils.createIdmScheduledTask(UUID.randomUUID().toString()));
		IdmLongRunningTaskDto longRunningTask = longRunningService.save(SchedulerTestUtils.createIdmLongRunningTask(scheduledTask, PasswordExpirationWarningTaskExecutor.class));
		PasswordExpirationWarningTaskExecutor executor = AutowireHelper.autowireBean(new PasswordExpirationWarningTaskExecutor());
		executor.setLongRunningTaskId(longRunningTask.getId());
		executor.init(ImmutableMap.of(PasswordExpirationWarningTaskExecutor.PARAMETER_DAYS_BEFORE, "2"));
		// first process
		Boolean result = executor.process();
		Page<IdmProcessedTaskItemDto> queueItems = itemService.findQueueItems(scheduledTask, null);
		Page<IdmProcessedTaskItemDto> logItems = itemService.findLogItems(longRunningTask, null);
		// first check
		assertTrue(result);
		assertTrue(executor.getCount() > 0);
		assertTrue(queueItems.getTotalElements() > 0);
		assertTrue(logItems.getTotalElements() > 0);
		assertTrue(logItems.getContent()
				.stream()
				.map(IdmProcessedTaskItemDto::getReferencedEntityId)
				.anyMatch(password.getId()::equals));
		// second process
		longRunningTask = longRunningService.save(SchedulerTestUtils.createIdmLongRunningTask(scheduledTask, PasswordExpirationWarningTaskExecutor.class));
		executor.setLongRunningTaskId(longRunningTask.getId());
		executor.init(ImmutableMap.of(PasswordExpirationWarningTaskExecutor.PARAMETER_DAYS_BEFORE, "2"));
		result = executor.process();
		itemService.findQueueItems(scheduledTask, null);
		logItems = itemService.findLogItems(longRunningTask, null);
		// second check
		assertTrue(result);
		assertEquals(Long.valueOf(0), executor.getCount());
		assertTrue(queueItems.getTotalElements() > 0);
		assertEquals(0, logItems.getTotalElements());
	}	
}
