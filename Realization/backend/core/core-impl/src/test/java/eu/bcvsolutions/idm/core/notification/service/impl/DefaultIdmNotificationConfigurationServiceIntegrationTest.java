package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationSender;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Nofification configuration tests:
 * - configure sender for notification type
 * - default notification sender
 * - TODO: global default senders (after configuration will be implemented)
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmNotificationConfigurationServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private ApplicationContext context;
	@Autowired private ConfigurationService configurationService;
	//
	private DefaultIdmNotificationConfigurationService service;
	
	@Before
	public void init() {
		service = context.getAutowireCapableBeanFactory().createBean(DefaultIdmNotificationConfigurationService.class);
	}
	
	@Test
	public void testDefaultSenderOrdered() {
		MockSender sender = (MockSender) service.getSender(MockSender.NOTIFICATION_TYPE);
		//
		Assert.assertEquals(MockSenderConfiguration.DEFAULT_SENDER, sender.getTestName());
	}
	
	@Test
	public void testConfiguredSender() {
		MockSender sender = (MockSender) service.getSender(MockSender.NOTIFICATION_TYPE);
		Assert.assertEquals(MockSenderConfiguration.DEFAULT_SENDER, sender.getTestName());
		//
		configurationService.setValue(sender.getConfigurationPropertyName(ConfigurationService.PROPERTY_IMPLEMENTATION), MockSenderConfiguration.CONFIGURED_SENDER);
		try {
			sender = (MockSender) service.getSender(MockSender.NOTIFICATION_TYPE);
			Assert.assertEquals(MockSenderConfiguration.CONFIGURED_SENDER, sender.getTestName());
		} finally {
			configurationService.setValue(sender.getConfigurationPropertyName(ConfigurationService.PROPERTY_IMPLEMENTATION), null);
		}
		//
		sender = (MockSender) service.getSender(MockSender.NOTIFICATION_TYPE);
		Assert.assertEquals(MockSenderConfiguration.DEFAULT_SENDER, sender.getTestName());
	}
	
	@Test(expected = ResultCodeException.class)
	public void wrongSenderConfiguration() {
		NotificationSender<?> sender = service.getSender(MockSender.NOTIFICATION_TYPE);
		configurationService.setValue(
				sender.getConfigurationPropertyName(ConfigurationService.PROPERTY_IMPLEMENTATION),
				"wrongMockSender"
		);
		try {
			service.getSender(MockSender.NOTIFICATION_TYPE);
		} finally {
			configurationService.setValue(sender.getConfigurationPropertyName(ConfigurationService.PROPERTY_IMPLEMENTATION), null);
		}
	}
	
	@Test
	public void testGetRecipients() {
		NotificationConfigurationDto config = new NotificationConfigurationDto();
		List<IdmNotificationRecipientDto> recipients = service.getRecipients(config);
		Assert.assertTrue(recipients.isEmpty());
		//
		config.setRecipients("  one , two   ");
		recipients = service.getRecipients(config);
		Assert.assertEquals(2, recipients.size());
		Assert.assertTrue(recipients.stream().anyMatch(r -> r.getRealRecipient().equals("one")));
		Assert.assertTrue(recipients.stream().anyMatch(r -> r.getRealRecipient().equals("two")));
		//
		config.setRecipients("  one , one   ");
		recipients = service.getRecipients(config);
		Assert.assertEquals(1, recipients.size());
		Assert.assertTrue(recipients.stream().anyMatch(r -> r.getRealRecipient().equals("one")));
	}
	
	@Test(expected = ResultCodeException.class)
	public void testSaveEmptyRecipientsWithRedirect() {
		NotificationConfigurationDto config = new NotificationConfigurationDto();
		config.setTopic(getHelper().createName());
		config.setNotificationType(IdmEmailLog.NOTIFICATION_TYPE);
		config.setRedirect(true);
		//
		service.save(config);
	}
}
