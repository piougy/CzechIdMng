package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationState;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * LRT integration test.
 * 
 * @author Radek Tomiška
 *
 */
public class DeleteNotificationTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmNotificationLogService service;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	
	@Test
	public void testDeleteOldSentNotifications() {
		// prepare provisioning operations
		ZonedDateTime createdOne = ZonedDateTime.now().minusDays(2);
		String subject = UUID.randomUUID().toString();
		IdmNotificationLogDto operationOne = createDto(subject, createdOne, NotificationState.ALL);
		// all other variants for not removal
		createDto(subject, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusMinutes(1), NotificationState.ALL);
		createDto(subject, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusMinutes(1), NotificationState.NOT);
		createDto(subject, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusMinutes(1), NotificationState.ALL);
		createDto(subject, ZonedDateTime.now().minusDays(2), NotificationState.PARTLY);
		createDto(subject, LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusHours(22), NotificationState.ALL);
		//
		Assert.assertEquals(createdOne, operationOne.getCreated());
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setText(subject);
		List<IdmNotificationLogDto> notifications = service.find(filter, null).getContent();
		Assert.assertEquals(6, notifications.size());
		//
		DeleteNotificationTaskExecutor taskExecutor = new DeleteNotificationTaskExecutor();
		Map<String, Object> properties = new HashMap<>();
		properties.put(DeleteNotificationTaskExecutor.PARAMETER_NUMBER_OF_DAYS, 1);
		properties.put(DeleteNotificationTaskExecutor.PARAMETER_SENT_ONLY, true);
		AutowireHelper.autowire(taskExecutor);
		taskExecutor.init(properties);
		//
		longRunningTaskManager.execute(taskExecutor);
		//
		notifications = service.find(filter, null).getContent();
		Assert.assertEquals(5, notifications.size());
		Assert.assertTrue(notifications.stream().allMatch(a -> !a.getId().equals(operationOne.getId())));		
	}
	
	private IdmNotificationLogDto createDto(String subject, ZonedDateTime created, NotificationState state) {
		IdmNotificationLogDto notification = new IdmNotificationLogDto();
		notification.setCreated(created);
		notification.setMessage(new IdmMessageDto
				.Builder(NotificationLevel.SUCCESS)
				.setSubject(subject)
				.setMessage("mock")
				.build());
		notification.setSent(state == NotificationState.ALL ? created : null);
		notification = service.save(notification);
		//
		if (state == NotificationState.ALL) {
			IdmNotificationLogDto childNotification = new IdmNotificationLogDto();
			childNotification.setCreated(created);
			childNotification.setMessage(new IdmMessageDto
					.Builder(NotificationLevel.SUCCESS)
					.setSubject("mock")
					.setMessage("mock")
					.build());
			childNotification.setParent(notification.getId());
			childNotification.setSent(created);
			//
			service.save(childNotification);
		}
		//
		return notification;
	}
}
