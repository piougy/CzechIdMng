package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationState;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test sms sender. Covers product provided AbstractSmsNotificationSender mainly.
 *  
 * @author Radek Tomiška
 */
public class TestSmsNotificationSenderIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdmNotificationConfigurationService notificationConfigurationService;
	@Autowired private TestSmsNotificationSender smsNotificationSender;
	@Autowired private NotificationManager notificationManager;
	@Autowired private IdmNotificationLogService notificationLogService;
	
	@Test
	public void testSetParentNotificationState() {		
		NotificationConfigurationDto config = new NotificationConfigurationDto();
		config.setTopic(getHelper().createName());
		config.setNotificationType(smsNotificationSender.getType());
		notificationConfigurationService.save(config);
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		notificationManager.send(config.getTopic(), new IdmMessageDto.Builder().setMessage(getHelper().createName()).build(), identity);
		// check sms log
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setNotificationType(smsNotificationSender.getNotificationType());
		filter.setTopic(config.getTopic());
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		Assert.assertEquals(1, notifications.size());
		Assert.assertNotNull(notifications.get(0).getSent());
		// check parent notification
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		Assert.assertEquals(1, notifications.size());
		Assert.assertNotNull(notifications.get(0).getSent());
		Assert.assertEquals(NotificationState.ALL, notifications.get(0).getState());
	}
}
