package eu.bcvsolutions.idm.core.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationState;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.notification.repository.IdmEmailLogRepository;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationLogRepository;
import eu.bcvsolutions.idm.core.notification.service.api.EmailNotificationSender;
import eu.bcvsolutions.idm.core.notification.service.api.IdmEmailLogService;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationManager;
import eu.bcvsolutions.idm.core.notification.service.impl.DefaultIdmNotificationTemplateService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Notification service tests
 * 
 * @author Radek Tomiška
 * @author Marek Klement
 *
 */
public class DefaultNotificationServiceTest extends AbstractIntegrationTest {

	private static final String TOPIC = "idm:test";
	private static final String TEST_TEMPLATE = "testTemplate";

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
	@Autowired private ConfigurationService configurationService;
	//
	NotificationConfigurationDto config = null;

	@Before
	public void clear() {
		loginAsAdmin("admin");
		// TODO: make test stateless!
		emailLogRepository.deleteAll();
		idmNotificationRepository.deleteAll();
		//
		config = new NotificationConfigurationDto();
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

		NotificationFilter filter = new NotificationFilter();
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

		NotificationFilter filter = new NotificationFilter();

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

		NotificationFilter filter = new NotificationFilter();
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
	public void redeployExistTemplate() {
		String backupFolder = "/tmp/idm_test_backup/";
		//
		configurationService.setValue(DefaultIdmNotificationTemplateService.BACKUP_FOLDER_CONFIG, backupFolder);
		//
		NotificationTemplateFilter filter = new NotificationTemplateFilter();
		filter.setText(TEST_TEMPLATE);
		//
		List<IdmNotificationTemplateDto> testTemplates = notificationTemplateService.find(filter, null).getContent();
		//
		assertEquals(1, testTemplates.size());
		IdmNotificationTemplateDto testTemplate = testTemplates.get(0);
		assertEquals(TEST_TEMPLATE, testTemplate.getCode());
		//
		// redeploy
		IdmNotificationTemplateDto testTemplateNew = notificationTemplateService.redeploy(testTemplate);
		//
		// after redeploy must be id same
		assertEquals(testTemplateNew.getId(), testTemplate.getId());
		configurationService.setValue(DefaultIdmNotificationTemplateService.BACKUP_FOLDER_CONFIG, null);
	}

	@Test
	public void redeployWithoutBackupFolder() {
		String backupPath = configurationService.getValue(DefaultIdmNotificationTemplateService.BACKUP_FOLDER_CONFIG);
		if (backupPath != null) {
			configurationService.setValue(DefaultIdmNotificationTemplateService.BACKUP_FOLDER_CONFIG, null);
		}
		//
		NotificationTemplateFilter filter = new NotificationTemplateFilter();
		filter.setText(TEST_TEMPLATE);
		//
		List<IdmNotificationTemplateDto> testTemplates = notificationTemplateService.find(filter, null).getContent();
		//
		assertEquals(1, testTemplates.size());
		IdmNotificationTemplateDto testTemplate = testTemplates.get(0);
		assertEquals(TEST_TEMPLATE, testTemplate.getCode());
		//
		try {
			notificationTemplateService.redeploy(testTemplate);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof ResultCodeException);
			ResultCodeException resultCode = (ResultCodeException) e;
			assertEquals(resultCode.getError().getError().getStatusEnum(),
					CoreResultCode.BACKUP_FOLDER_NOT_FOUND.name());
		}
	}

	@Test
	public void redeployWithBackupFolder() {
		String backupFolder = "/tmp/idm_test_backup/";
		//
		File directory = new File(backupFolder);
		if (directory.exists() && directory.isDirectory()) {
			try {
				FileUtils.deleteDirectory(directory);
			} catch (IOException e) {
				fail();
			}
		}
		//
		configurationService.setValue(DefaultIdmNotificationTemplateService.BACKUP_FOLDER_CONFIG, backupFolder);
		//
		NotificationTemplateFilter filter = new NotificationTemplateFilter();
		filter.setText(TEST_TEMPLATE);
		//
		List<IdmNotificationTemplateDto> testTemplates = notificationTemplateService.find(filter, null).getContent();
		//
		assertEquals(1, testTemplates.size());
		IdmNotificationTemplateDto testTemplate = testTemplates.get(0);
		assertEquals(TEST_TEMPLATE, testTemplate.getCode());
		//
		try {
			IdmNotificationTemplateDto newDto = notificationTemplateService.redeploy(testTemplate);
			assertEquals(testTemplate.getCode(), newDto.getCode());
			//
			DateTime date = new DateTime();
			DecimalFormat decimalFormat = new DecimalFormat("00");
			directory = new File(backupFolder + "templates/" + date.getYear()
					+ decimalFormat.format(date.getMonthOfYear()) + decimalFormat.format(date.getDayOfMonth()) + "/");
			File[] files = directory.listFiles();
			assertEquals(1, files.length);
			File backup = files[0];
			assertTrue(backup.exists());
			assertTrue(backup.getName().contains("admin"));
			assertTrue(backup.getName().contains(testTemplate.getCode()));
		} catch (Exception e) {
			fail();
		}
		configurationService.setValue(DefaultIdmNotificationTemplateService.BACKUP_FOLDER_CONFIG, null);
	}

	@Test
	public void redeployExistTemplateAndCheckBackup() {
		// check if exist directory and remove it with all files in
		String backupFolder = "/tmp/idm_test_backup/";
		//
		File directory = new File(backupFolder);
		if (directory.exists() && directory.isDirectory()) {
			try {
				FileUtils.deleteDirectory(directory);
			} catch (IOException e) {
				fail();
			}
		}
		//
		configurationService.setValue(DefaultIdmNotificationTemplateService.BACKUP_FOLDER_CONFIG, backupFolder);
		//
		NotificationTemplateFilter filter = new NotificationTemplateFilter();
		filter.setText(TEST_TEMPLATE);
		//
		List<IdmNotificationTemplateDto> testTemplates = notificationTemplateService.find(filter, null).getContent();
		//
		assertEquals(1, testTemplates.size());
		IdmNotificationTemplateDto testTemplate = testTemplates.get(0);
		assertEquals(TEST_TEMPLATE, testTemplate.getCode());
		//
		IdmNotificationTemplateDto testTemplateNew = notificationTemplateService.redeploy(testTemplate);
		//
		assertEquals(testTemplateNew.getId(), testTemplate.getId());
		//
		// after redeploy check directory
		DateTime date = new DateTime();
		DecimalFormat decimalFormat = new DecimalFormat("00");
		directory = new File(backupFolder + "templates/" + date.getYear()
				+ decimalFormat.format(date.getMonthOfYear()) + decimalFormat.format(date.getDayOfMonth()) + "/");
		assertTrue(directory.exists());
		assertTrue(directory.isDirectory());
		//
		File[] files = directory.listFiles();
		assertEquals(1, files.length);
		File backup = files[0];
		assertTrue(backup.exists());
		assertTrue(backup.getName().contains("admin"));
		assertTrue(backup.getName().contains(testTemplateNew.getCode()));
	}

	@Test
	public void redeployNewTemplate() {
		IdmNotificationTemplateDto template = new IdmNotificationTemplateDto();
		String name = "temp_" + System.currentTimeMillis();
		template.setCode(name);
		template.setName(name);
		template.setBodyText(name);
		template.setSubject(name);
		template = notificationTemplateService.save(template);
		//
		// check exception
		try {
			notificationTemplateService.redeploy(template);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof ResultCodeException);
			ResultCodeException resultCode = (ResultCodeException) e;
			assertEquals(resultCode.getError().getError().getStatusEnum(),
					CoreResultCode.NOTIFICATION_TEMPLATE_XML_FILE_NOT_FOUND.name());
		}
	}

	@Test
	public void textFilterTest(){
		IdmIdentityDto identity = helper.createIdentity();
		NotificationFilter filter = new NotificationFilter();
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
		NotificationFilter filter = new NotificationFilter();
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
		NotificationFilter filter = new NotificationFilter();
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

		NotificationFilter filter = new NotificationFilter();
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
