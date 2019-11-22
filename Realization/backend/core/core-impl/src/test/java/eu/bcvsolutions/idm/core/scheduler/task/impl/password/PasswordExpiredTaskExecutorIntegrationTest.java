package eu.bcvsolutions.idm.core.scheduler.task.impl.password;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmScheduledTaskService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.utils.SchedulerTestUtils;

/**
 * Send password expired message test.
 * 
 * @author Radek Tomiška
 *
 */
public class PasswordExpiredTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmPasswordService passwordService;
	@Autowired private IdmScheduledTaskService scheduledTaskService;
	@Autowired private IdmLongRunningTaskService longRunningService;
	@Autowired private IdmProcessedTaskItemService itemService;
	@Autowired private IdmIdentityService identityService;

	@Test
	public void testSimpleMessageDry() {
		// prepare date
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		try {
			IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
			password.setValidTill(LocalDate.now().minusDays(1));		
			password = passwordService.save(password);
			// prepare task
			IdmScheduledTaskDto scheduledTask = scheduledTaskService.save(SchedulerTestUtils.createIdmScheduledTask(UUID.randomUUID().toString()));
			IdmLongRunningTaskDto longRunningTask = longRunningService.save(SchedulerTestUtils.createIdmLongRunningTask(scheduledTask, PasswordExpiredTaskExecutor.class));
			PasswordExpiredTaskExecutor executor = AutowireHelper.autowireBean(new PasswordExpiredTaskExecutor());
			executor.setLongRunningTaskId(longRunningTask.getId());
			executor.init(new HashMap<>());
			// first process
			Boolean result = executor.process();
			Page<IdmProcessedTaskItemDto> queueItems = itemService.findQueueItems(scheduledTask, null);
			Page<IdmProcessedTaskItemDto> logItems = itemService.findLogItems(longRunningTask, null);
			// first check
			Assert.assertTrue(result);
			Assert.assertTrue(executor.getCount() > 0);
			Assert.assertTrue(queueItems.getTotalElements() > 0);
			Assert.assertTrue(logItems.getTotalElements() > 0);
			Assert.assertTrue(logItems.getContent()
					.stream()
					.map(IdmProcessedTaskItemDto::getReferencedEntityId)
					.anyMatch(password.getId()::equals));
			// second process
			longRunningTask = longRunningService.save(SchedulerTestUtils.createIdmLongRunningTask(scheduledTask, PasswordExpiredTaskExecutor.class));
			executor.setLongRunningTaskId(longRunningTask.getId());
			executor.init(new HashMap<>());
			result = executor.process();
			itemService.findQueueItems(scheduledTask, null);
			logItems = itemService.findLogItems(longRunningTask, null);
			// second check
			Assert.assertTrue(result);
			Assert.assertEquals(Long.valueOf(0), executor.getCount());
			Assert.assertTrue(queueItems.getTotalElements() > 0);
			Assert.assertEquals(0, logItems.getTotalElements());
		} finally {
			identityService.delete(identity);
		}
	}
	
	@Test
	public void testNotSendMessageToDisabledIdentity() {
		// prepare date
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		try{
			IdmPasswordDto preparedPassword = passwordService.findOneByIdentity(identity.getId());
			preparedPassword.setValidTill(LocalDate.now().minusDays(1));		
			IdmPasswordDto password = passwordService.save(preparedPassword);
			// disable identity
			identity.setState(IdentityState.DISABLED_MANUALLY);
			identityService.save(identity);
			// prepare task
			IdmScheduledTaskDto scheduledTask = scheduledTaskService.save(SchedulerTestUtils.createIdmScheduledTask(UUID.randomUUID().toString()));
			IdmLongRunningTaskDto longRunningTask = longRunningService.save(SchedulerTestUtils.createIdmLongRunningTask(scheduledTask, PasswordExpiredTaskExecutor.class));
			PasswordExpiredTaskExecutor executor = AutowireHelper.autowireBean(new PasswordExpiredTaskExecutor());
			executor.setLongRunningTaskId(longRunningTask.getId());
			executor.init(new HashMap<>());
			// first process
			Boolean result = executor.process();
			Page<IdmProcessedTaskItemDto> logItems = itemService.findLogItems(longRunningTask, null);
			// check
			Assert.assertTrue(result);
			Assert.assertFalse(logItems
					.getContent()
					.stream()
					.map(IdmProcessedTaskItemDto::getReferencedEntityId)
					.anyMatch(password.getId()::equals));
		} finally {
			identityService.delete(identity);
		}
	}
	
	@Test
	public void testNotSendMessageValidTillToday() {
		// prepare date
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		try{
			IdmPasswordDto password = passwordService.findOneByIdentity(identity.getId());
			password.setValidTill(LocalDate.now());		
			passwordService.save(password);
			// prepare task
			IdmScheduledTaskDto scheduledTask = scheduledTaskService.save(SchedulerTestUtils.createIdmScheduledTask(UUID.randomUUID().toString()));
			IdmLongRunningTaskDto longRunningTask = longRunningService.save(SchedulerTestUtils.createIdmLongRunningTask(scheduledTask, PasswordExpiredTaskExecutor.class));
			PasswordExpiredTaskExecutor executor = AutowireHelper.autowireBean(new PasswordExpiredTaskExecutor());
			executor.setLongRunningTaskId(longRunningTask.getId());
			executor.init(new HashMap<>());
			// first process
			Boolean result = executor.process();
			Page<IdmProcessedTaskItemDto> logItems = itemService.findLogItems(longRunningTask, null);
			// check
			Assert.assertTrue(result);
			Assert.assertTrue(logItems
					.getContent()
					.stream()
					.anyMatch(pi -> {
						return pi.getReferencedEntityId().equals(password.getId()) && pi.getOperationResult().getState() == OperationState.NOT_EXECUTED;
					}));
		} finally {
			identityService.delete(identity);
		}
	}
}
