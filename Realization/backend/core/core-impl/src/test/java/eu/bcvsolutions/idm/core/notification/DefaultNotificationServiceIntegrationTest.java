package eu.bcvsolutions.idm.core.notification;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationState;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.EmailNotificationSender;
import eu.bcvsolutions.idm.core.notification.api.service.IdmEmailLogService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.notification.entity.IdmConsoleLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.notification.repository.IdmEmailLogRepository;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationLogRepository;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Notification service tests
 * 
 * @author Radek Tomiška
 * @author Marek Klement
 *
 */
public class DefaultNotificationServiceIntegrationTest extends AbstractIntegrationTest {

	private static final String TOPIC = "idm:test";

	@Autowired private TestHelper helper;
	@Autowired private NotificationManager notificationManager;
	@Autowired private EmailNotificationSender emailService;
	@Autowired private IdmNotificationLogRepository idmNotificationRepository;
	@Autowired private IdmNotificationLogService notificationLogService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmEmailLogRepository emailLogRepository;
	@Autowired private IdmEmailLogService emailLogService;
	@Autowired private IdmNotificationTemplateService notificationTemplateService;
	@Autowired private IdmNotificationConfigurationService notificationConfigurationService;
	//
	IdmNotificationConfigurationDto config = null;

	@Before
	public void clear() {
		loginAsAdmin("admin");
		// TODO: make test stateless!
		emailLogRepository.deleteAll();
		idmNotificationRepository.deleteAll();
		//
		config = new IdmNotificationConfigurationDto();
		config.setTopic(TOPIC);
		config.setNotificationType(IdmEmailLog.NOTIFICATION_TYPE);
		config = notificationConfigurationService.save(config);
	}

	@After
	@Override
	public void logout() {
		notificationConfigurationService.delete(config);
		super.logout();
	}

	@Test
	@Transactional
	public void testSendSimple() {
		assertEquals(0, idmNotificationRepository.count());
		IdmNotificationTemplateDto template = createTestTemplate("Idm notification", "subject");

		IdmIdentityDto identity = identityService.getByUsername(InitTestData.TEST_USER_1);

		notificationManager.send(TOPIC, new IdmMessageDto.Builder().setTemplate(template).build(), identity);

		assertEquals(1, idmNotificationRepository.count());
		assertEquals(1, emailLogRepository.count());
	}

	@Test
	@Transactional
	public void testFilterByDate() {
		assertEquals(0, idmNotificationRepository.count());
		IdmNotificationTemplateDto template = createTestTemplate("Idm notification", "subject");
		IdmIdentityDto identity = identityService.getByUsername(InitTestData.TEST_USER_1);

		DateTime from = new DateTime().minusDays(1);
		DateTime till = new DateTime().minusDays(1);
		notificationManager.send(TOPIC, new IdmMessageDto.Builder().setTemplate(template).build(), identity);
		notificationManager.send(TOPIC, new IdmMessageDto.Builder().setTemplate(template).build(), identity);

		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setNotificationType(IdmNotificationLog.class);
		assertEquals(2, notificationLogService.find(filter, null).getTotalElements());

		filter.setFrom(from);
		assertEquals(2, notificationLogService.find(filter, null).getTotalElements());

		filter.setFrom(null);
		filter.setTill(till);
		assertEquals(0, notificationLogService.find(filter, null).getTotalElements());
	}

	@Test
	@Transactional
	public void testEmailFilterBySender() {
		// create templates
		IdmNotificationTemplateDto template = createTestTemplate("Idm notification", "subject");

		IdmNotificationFilter filter = new IdmNotificationFilter();

		filter.setSender(InitTestData.TEST_USER_2);
		assertEquals(0, emailLogService.find(filter, null).getTotalElements());
		filter.setSender(InitTestData.TEST_USER_1);
		assertEquals(0, emailLogService.find(filter, null).getTotalElements());

		// send some email
		IdmIdentityDto identity = identityService.getByUsername(InitTestData.TEST_USER_1);
		IdmIdentityDto identity2 = identityService.getByUsername(InitTestData.TEST_USER_2);

		emailService.send(TOPIC, new IdmMessageDto.Builder().setTemplate(template).build(), identity);

		filter.setSender(null);
		assertEquals(1, emailLogService.find(filter, null).getTotalElements());
		filter.setSender(identity2.getUsername());
		assertEquals(0, emailLogService.find(filter, null).getTotalElements());
		filter.setSender(null);
		filter.setRecipient(identity.getUsername());
		assertEquals(1, emailLogService.find(filter, null).getTotalElements());
	}

	@Test
	@Transactional
	public void testEmailFilterBySent() {
		IdmIdentityDto identity = identityService.getByUsername(InitTestData.TEST_USER_1);

		IdmNotificationFilter filter = new IdmNotificationFilter();
		//
		// create templates
		IdmNotificationTemplateDto template = createTestTemplate("Idm notification", "subject");
		//
		emailService.send(new IdmMessageDto.Builder().setTemplate(template).build(), identity);
		filter.setSent(true);
		assertEquals(0, emailLogService.find(filter, null).getTotalElements());

		emailService.send(new IdmMessageDto.Builder().setTemplate(template).build(), identity);
		filter.setSent(false);
		assertEquals(2, emailLogService.find(filter, null).getTotalElements());
	}

	@Test
	public void textFilterTest(){
		IdmIdentityDto identity = helper.createIdentity();
		IdmNotificationFilter filter = new IdmNotificationFilter();
		//
		// create templates
		IdmNotificationTemplateDto template = createTestTemplate("TestTemplate1", "testSubject");
		IdmNotificationTemplateDto template2 = createTestTemplate("TestTemplate2","testSubject2");
		//
		emailService.send(new IdmMessageDto.Builder().setTemplate(template).build(), identity);
		emailService.send(new IdmMessageDto.Builder().setTemplate(template2).build(), identity);
		// filter text BODY
		filter.setText(template.getBodyText());
		Page<IdmNotificationLogDto> result = notificationLogService.find(filter, null);
		assertEquals("Wrong text message body",1, result.getTotalElements());
		// filter text HTML
		filter.setText(template2.getBodyHtml());
		result = notificationLogService.find(filter, null);
		assertEquals("Wrong text message html",1, result.getTotalElements());
		// filter text subject
		filter.setText(template.getSubject());
		result = notificationLogService.find(filter, null);
		assertEquals("Wrong text message html",2, result.getTotalElements());
	}

	@Test
	public void senderFilterTest(){
		IdmIdentityDto sender = helper.createIdentity();
		IdmNotificationFilter filter = new IdmNotificationFilter();
		IdmNotificationDto notification = new IdmNotificationDto();
		notification.setIdentitySender(sender.getId());
		//
		// create templates
		IdmNotificationTemplateDto template = createTestTemplate("TestTemplate3", "testSubject3");
		IdmMessageDto message = new IdmMessageDto.Builder().setTemplate(template).build();
		notification.setMessage(message);
		notificationManager.send(notification);
		//
		// filter text BODY
		filter.setSender(sender.getUsername());
		Page<IdmNotificationLogDto> result = notificationLogService.find(filter, null);
		assertEquals("Wrong sender",sender.getId(), result.getContent().get(0).getIdentitySender());
	}

	@Test
	public void parentFilterText(){
		IdmNotificationFilter filter = new IdmNotificationFilter();
		IdmNotificationDto notification = new IdmNotificationDto();
		IdmNotificationDto parentNotification = new IdmNotificationDto();
		// prepare template and message
		IdmNotificationTemplateDto template2 = createTestTemplate("TestTemplate5", "testSubject5");
		IdmMessageDto message2 = new IdmMessageDto.Builder().setTemplate(template2).build();
		// set parent
		parentNotification.setMessage(message2);
		IdmNotificationLogDto logDto = notificationManager.send(parentNotification);
		notification.setParent(logDto.getMessage().getId());
		//
		// send message
		IdmNotificationTemplateDto template = createTestTemplate("TestTemplate4", "testSubject4");
		IdmMessageDto message = new IdmMessageDto.Builder().setTemplate(template).build();
		notification.setMessage(message);
		notificationManager.send(notification);
		// set filter
		filter.setParent(logDto.getId());
		Page<IdmNotificationLogDto> result = notificationLogService.find(filter, null);
		assertEquals("Wrong sender", logDto.getId(), result.getContent().get(0).getParent());
	}

	@Test
	@Ignore
	public void stateFilterTest(){
		IdmIdentityDto identity1 = helper.createIdentity();
		IdmIdentityDto identity2 = helper.createIdentity();
		IdmIdentityDto identity3 = helper.createIdentity();
		IdmIdentityDto identity4 = helper.createIdentity();
		List<IdmIdentityDto> identities = Arrays.asList(identity1, identity2, identity3, identity4);
		IdmNotificationTemplateDto template = createTestTemplate("TestTemplate6", "testSubject6");
		IdmMessageDto message = new IdmMessageDto.Builder().setTemplate(template).build();
		notificationManager.send(message, identities);

		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setState(NotificationState.ALL);
		Page<IdmNotificationLogDto> result = notificationLogService.find(filter, null);
		assertEquals("Wrong state ALL", 1, result.getTotalElements());
		filter.setState(NotificationState.NOT);
		Page<IdmNotificationLogDto> result2 = notificationLogService.find(filter, null);
		assertEquals("Wrong state NOT", 1, result2.getTotalElements());
		filter.setState(NotificationState.PARTLY);
		result = notificationLogService.find(filter, null);
		assertEquals("Wrong state PARTLY", 0, result.getTotalElements());
	}
	
	@Test
	public void sendWildCardsWithoutTemplate() {
		String topic = "testTopic-" + System.currentTimeMillis();
		String text = "testMessageText-" + System.currentTimeMillis();
		//
		IdmIdentityDto identity = helper.createIdentity();
		// create config, for email, topic and without level = wildcard
		IdmNotificationConfigurationDto config = new IdmNotificationConfigurationDto();
		config.setTopic(topic); // topic
		config.setNotificationType(IdmEmailLog.NOTIFICATION_TYPE); // email
		config = notificationConfigurationService.save(config);
		//
		// set all text into message
		List<IdmNotificationLogDto> notifications = notificationManager.send(topic, 
				new IdmMessageDto.Builder()
				.setLevel(NotificationLevel.SUCCESS) // set level
				.setHtmlMessage(text)
				.setTextMessage(text)
				.setSubject(text)
				.build(),
				identity);
		//
		assertEquals(1, notifications.size());
		//
		IdmNotificationLogDto notification = notifications.get(0);
		assertEquals(text, notification.getMessage().getHtmlMessage());
		assertEquals(text, notification.getMessage().getSubject());
		assertEquals(text, notification.getMessage().getTextMessage());
	}

	@Test
	public void sendWildCardsWithTemplateAndOwnText() {
		String topic = "testTopic-" + System.currentTimeMillis();
		String textMessage = "testMessageText-" + System.currentTimeMillis();
		String textTemplate = "testMessageTemplate-" + System.currentTimeMillis();
		//
		IdmNotificationTemplateDto template = new IdmNotificationTemplateDto();
		template.setName(textTemplate);
		template.setCode(textTemplate);
		template.setBodyHtml(textTemplate);
		template.setBodyText(textTemplate);
		template.setSubject(textTemplate);
		template = notificationTemplateService.save(template);
		//
		IdmIdentityDto identity = helper.createIdentity();
		// create config, for email, topic, template and without level = wildcard
		IdmNotificationConfigurationDto config = new IdmNotificationConfigurationDto();
		config.setTopic(topic); // topic
		config.setTemplate(template.getId()); // template
		config.setNotificationType(IdmEmailLog.NOTIFICATION_TYPE); // email
		config = notificationConfigurationService.save(config);
		//
		// set all text into message
		List<IdmNotificationLogDto> notifications = notificationManager.send(topic, 
				new IdmMessageDto.Builder()
				.setLevel(NotificationLevel.SUCCESS) // set level
				.setHtmlMessage(textMessage)
				.setTextMessage(textMessage)
				.setSubject(textMessage)
				.build(),
				identity);
		//
		assertEquals(1, notifications.size());
		//
		IdmNotificationLogDto notification = notifications.get(0);
		// topic has own template, but in message is set text
		assertEquals(textMessage, notification.getMessage().getHtmlMessage());
		assertEquals(textMessage, notification.getMessage().getSubject());
		assertEquals(textMessage, notification.getMessage().getTextMessage());
	}

	@Test
	public void sendWildCardsWithTemplateWithoutText() {
		String topic = "testTopic-" + System.currentTimeMillis();
		String textTemplate = "testMessageTemplate-" + System.currentTimeMillis();
		//
		IdmNotificationTemplateDto template = new IdmNotificationTemplateDto();
		template.setName(textTemplate);
		template.setCode(textTemplate);
		template.setBodyHtml(textTemplate);
		template.setBodyText(textTemplate);
		template.setSubject(textTemplate);
		template = notificationTemplateService.save(template);
		//
		IdmIdentityDto identity = helper.createIdentity();
		// create config, for email, topic, template and without level = wildcard
		IdmNotificationConfigurationDto config = new IdmNotificationConfigurationDto();
		config.setTopic(topic); // topic
		config.setTemplate(template.getId()); // template
		config.setNotificationType(IdmEmailLog.NOTIFICATION_TYPE); // email
		config = notificationConfigurationService.save(config);
		//
		List<IdmNotificationLogDto> notifications = notificationManager.send(topic, 
				new IdmMessageDto.Builder()
				.setLevel(NotificationLevel.SUCCESS) // set level
				.build(),
				identity);
		//
		assertEquals(1, notifications.size());
		//
		IdmNotificationLogDto notification = notifications.get(0);
		// topic has own template and in message isnt set text
		assertEquals(textTemplate, notification.getMessage().getHtmlMessage());
		assertEquals(textTemplate, notification.getMessage().getSubject());
		assertEquals(textTemplate, notification.getMessage().getTextMessage());
	}

	@Test
	public void sendTwoWildCardsWithDifferentTemplate() {
		String topic = "testTopic-" + System.currentTimeMillis();
		String textTemplate1 = "testMessageTemplate1-" + System.currentTimeMillis();
		String textTemplate2 = "testMessageTemplate2-" + System.currentTimeMillis();
		//
		IdmNotificationTemplateDto template1 = new IdmNotificationTemplateDto();
		template1.setName(textTemplate1);
		template1.setCode(textTemplate1);
		template1.setBodyHtml(textTemplate1);
		template1.setBodyText(textTemplate1);
		template1.setSubject(textTemplate1);
		template1 = notificationTemplateService.save(template1);
		//
		IdmNotificationTemplateDto template2 = new IdmNotificationTemplateDto();
		template2.setName(textTemplate2);
		template2.setCode(textTemplate2);
		template2.setBodyHtml(textTemplate2);
		template2.setBodyText(textTemplate2);
		template2.setSubject(textTemplate2);
		template2 = notificationTemplateService.save(template2);
		//
		IdmIdentityDto identity = helper.createIdentity();
		// create config, for email, topic, template and without level = wildcard
		IdmNotificationConfigurationDto config1 = new IdmNotificationConfigurationDto();
		config1.setTopic(topic); // topic
		config1.setTemplate(template1.getId()); // template
		config1.setNotificationType(IdmEmailLog.NOTIFICATION_TYPE); // email
		config1 = notificationConfigurationService.save(config1);
		//
		// create second config, for console, topic, template and without level = wildcard
		IdmNotificationConfigurationDto config = new IdmNotificationConfigurationDto();
		config.setTopic(topic); // same topic
		config.setTemplate(template2.getId()); // different template
		config.setNotificationType(IdmConsoleLog.NOTIFICATION_TYPE); // console
		config = notificationConfigurationService.save(config);
		//
		List<IdmNotificationLogDto> notifications = notificationManager.send(topic, 
				new IdmMessageDto.Builder()
				.setLevel(NotificationLevel.SUCCESS) // set level
				.build(),
				identity);
		//
		assertEquals(2, notifications.size());
		//
		//
		int orderConsole = 0;
		int orderEmail = 1;
		// we didn't know order
		if (!notifications.get(0).getMessage().getHtmlMessage().equals(textTemplate2)) {
			// email first
			orderConsole = 1;
			orderEmail = 0;
		}
		//
		// Notification has different text
		IdmNotificationLogDto notificationConsole = notifications.get(orderConsole);
		assertEquals(textTemplate2, notificationConsole.getMessage().getHtmlMessage());
		assertEquals(textTemplate2, notificationConsole.getMessage().getSubject());
		assertEquals(textTemplate2, notificationConsole.getMessage().getTextMessage());
		//
		IdmNotificationLogDto notificationEmail= notifications.get(orderEmail);
		assertEquals(textTemplate1, notificationEmail.getMessage().getHtmlMessage());
		assertEquals(textTemplate1, notificationEmail.getMessage().getSubject());
		assertEquals(textTemplate1, notificationEmail.getMessage().getTextMessage());
	}

	@Test
	public void sendTwoWildCardsWithOwnMessage() {
		String topic = "testTopic-" + System.currentTimeMillis();
		String textTemplate1 = "testMessageTemplate1-" + System.currentTimeMillis();
		String textTemplate2 = "testMessageTemplate2-" + System.currentTimeMillis();
		String textMessage = "testMessageText-" + System.currentTimeMillis();
		//
		IdmNotificationTemplateDto template1 = new IdmNotificationTemplateDto();
		template1.setName(textTemplate1);
		template1.setCode(textTemplate1);
		template1.setBodyHtml(textTemplate1);
		template1.setBodyText(textTemplate1);
		template1.setSubject(textTemplate1);
		template1 = notificationTemplateService.save(template1);
		//
		IdmNotificationTemplateDto template2 = new IdmNotificationTemplateDto();
		template2.setName(textTemplate2);
		template2.setCode(textTemplate2);
		template2.setBodyHtml(textTemplate2);
		template2.setBodyText(textTemplate2);
		template2.setSubject(textTemplate2);
		template2 = notificationTemplateService.save(template2);
		//
		IdmIdentityDto identity = helper.createIdentity();
		// create config, for email, topic, template and without level = wildcard
		IdmNotificationConfigurationDto config1 = new IdmNotificationConfigurationDto();
		config1.setTopic(topic); // topic
		config1.setTemplate(template1.getId()); // template
		config1.setNotificationType(IdmEmailLog.NOTIFICATION_TYPE); // email
		config1 = notificationConfigurationService.save(config1);
		//
		// create second config, for console, topic, template and without level = wildcard
		IdmNotificationConfigurationDto config = new IdmNotificationConfigurationDto();
		config.setTopic(topic); // same topic
		config.setTemplate(template2.getId()); // different template
		config.setNotificationType(IdmConsoleLog.NOTIFICATION_TYPE); // console
		config = notificationConfigurationService.save(config);
		//
		List<IdmNotificationLogDto> notifications = notificationManager.send(topic, 
				new IdmMessageDto.Builder()
				.setLevel(NotificationLevel.SUCCESS) // set level
				.setHtmlMessage(textMessage)
				.setTextMessage(textMessage)
				.setSubject(textMessage)
				.build(),
				identity);
		//
		assertEquals(2, notifications.size());
		//
		// bouth notifacations has same text
		IdmNotificationLogDto notification1 = notifications.get(0);
		IdmNotificationLogDto notification2 = notifications.get(1);
		//
		assertEquals(textMessage, notification1.getMessage().getHtmlMessage());
		assertEquals(textMessage, notification1.getMessage().getSubject());
		assertEquals(textMessage, notification1.getMessage().getTextMessage());
		//
		assertEquals(textMessage, notification2.getMessage().getHtmlMessage());
		assertEquals(textMessage, notification2.getMessage().getSubject());
		assertEquals(textMessage, notification2.getMessage().getTextMessage());
	}
	
	@Test
	public void sendNofificationToConsoleIfTopicNotFound() {
		String topic = helper.createName();
		// create config, for email, topic, template and without level = wildcard
		IdmNotificationConfigurationDto config = new IdmNotificationConfigurationDto();
		config.setTopic(topic); // topic
		config.setLevel(NotificationLevel.INFO);
		config.setNotificationType(IdmEmailLog.NOTIFICATION_TYPE); // email
		config = notificationConfigurationService.save(config);
		//
		IdmIdentityDto identity = helper.createIdentity();
		List<IdmNotificationLogDto> notifications = notificationManager.send(
				topic, 
				new IdmMessageDto
					.Builder()
					.setLevel(NotificationLevel.SUCCESS) // set level
					.setMessage("message")
					.setSubject("subject")
					.build(),
				identity);
		//
		assertEquals(1, notifications.size());
		//
		// console channel is expected, because topic configuration is wrong
		IdmNotificationLogDto notification = notifications.get(0);
		//
		Assert.assertEquals(IdmConsoleLog.NOTIFICATION_TYPE, notification.getType());
	}

	private IdmNotificationTemplateDto createTestTemplate(String body, String subject) {
		// create templates
		IdmNotificationTemplateDto template = new IdmNotificationTemplateDto();
		template.setName("test_" + System.currentTimeMillis());
		template.setBodyHtml(body);
		template.setBodyText(body);
		template.setCode(subject);
		template.setSubject(subject);
		return notificationTemplateService.save(template);
	}

}
