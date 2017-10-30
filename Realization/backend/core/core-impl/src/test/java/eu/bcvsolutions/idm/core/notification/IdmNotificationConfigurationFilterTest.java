package eu.bcvsolutions.idm.core.notification;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationConfigurationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

public class IdmNotificationConfigurationFilterTest extends AbstractIntegrationTest{
	@Autowired private IdmNotificationConfigurationService idmNotificationConfService;
	@Autowired private TestHelper testHelper;

	@Before
	public void login(){
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout(){
		super.logout();
	}
	
	@Test
	public void testTopicFilter(){		
		NotificationConfigurationDto notification = createNotification(NotificationLevel.SUCCESS, "core:test", "sms", UUID.randomUUID());
		IdmNotificationConfigurationFilter filter = new IdmNotificationConfigurationFilter();
		filter.setText("core:test");
		Page<NotificationConfigurationDto> result = idmNotificationConfService.find(filter, null);
		assertEquals(1, result.getTotalElements());
		assertEquals(notification.getId(),result.getContent().get(0).getId());
	}
	
	private NotificationConfigurationDto createNotification(NotificationLevel level, String topic, String notificationType, UUID templateId) {
		NotificationConfigurationDto notification = new NotificationConfigurationDto();
		notification.setLevel(level);
		notification.setTopic(topic);
		notification.setNotificationType(notificationType);
		notification.setTemplate(templateId);
		notification = idmNotificationConfService.save(notification);
		return notification;
	}
}
