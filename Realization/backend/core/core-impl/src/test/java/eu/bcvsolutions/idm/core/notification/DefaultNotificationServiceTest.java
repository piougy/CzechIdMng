package eu.bcvsolutions.idm.core.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.notification.repository.IdmEmailLogRepository;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationLogRepository;
import eu.bcvsolutions.idm.core.notification.service.api.EmailNotificationSender;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationManager;
import eu.bcvsolutions.idm.core.notification.service.impl.DefaultIdmNotificationTemplateService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Notification service tests
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultNotificationServiceTest extends AbstractIntegrationTest {

	private static final String TOPIC = "idm:test";
	private static final String TEST_TEMPLATE = "testTemplate";

	@Autowired
	private NotificationManager notificationManager;
	@Autowired
	private EmailNotificationSender emailService;
	@Autowired
	private IdmNotificationLogRepository idmNotificationRepository;
	@Autowired
	private IdmNotificationLogService notificationLogService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmEmailLogRepository emailLogRepository;
	@Autowired
	private IdmNotificationTemplateService notificationTemplateService;
	@Autowired
	private IdmNotificationConfigurationService notificationConfigurationService;
	@Autowired
	private ConfigurationService configurationService;
	//
	NotificationConfigurationDto config = null;

	@Before
	public void clear() {
		loginAsAdmin("admin");
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
		assertEquals(0, emailLogRepository.find(filter, null).getTotalElements());
		filter.setSender(InitTestData.TEST_USER_1);
		assertEquals(0, emailLogRepository.find(filter, null).getTotalElements());

		// send some email
		IdmIdentityDto identity = identityService.getByUsername(InitTestData.TEST_USER_1);
		IdmIdentityDto identity2 = identityService.getByUsername(InitTestData.TEST_USER_2);

		emailService.send(TOPIC, new IdmMessageDto.Builder().setTemplate(template).build(), identity);

		filter.setSender(null);
		assertEquals(1, emailLogRepository.find(filter, null).getTotalElements());
		filter.setSender(identity2.getUsername());
		assertEquals(0, emailLogRepository.find(filter, null).getTotalElements());
		filter.setSender(null);
		filter.setRecipient(identity.getUsername());
		assertEquals(1, emailLogRepository.find(filter, null).getTotalElements());
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
		assertEquals(0, emailLogRepository.find(filter, null).getTotalElements());

		emailService.send(new IdmMessageDto.Builder().setTemplate(template).build(), identity);
		filter.setSent(false);
		assertEquals(2, emailLogRepository.find(filter, null).getTotalElements());
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
		IdmNotificationTemplateDto testTemplateNew = notificationTemplateService.redeployDto(testTemplate);
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
			notificationTemplateService.redeployDto(testTemplate);
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
			IdmNotificationTemplateDto newDto = notificationTemplateService.redeployDto(testTemplate);
			assertEquals(testTemplate.getCode(), newDto.getCode());
			//
			DateTime date = new DateTime();
			directory = new File(backupFolder + "templates/" + date.getYear() + "_" + date.getMonthOfYear() + "_" + date.getDayOfMonth() + "/");
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
		IdmNotificationTemplateDto testTemplateNew = notificationTemplateService.redeployDto(testTemplate);
		//
		assertEquals(testTemplateNew.getId(), testTemplate.getId());
		//
		// after redeploy check directory
		DateTime date = new DateTime();
		directory = new File(backupFolder + "templates/" + date.getYear() + "_" + date.getMonthOfYear() + "_" + date.getDayOfMonth() + "/");
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
			notificationTemplateService.redeployDto(template);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof ResultCodeException);
			ResultCodeException resultCode = (ResultCodeException) e;
			assertEquals(resultCode.getError().getError().getStatusEnum(),
					CoreResultCode.NOTIFICATION_TEMPLATE_XML_FILE_NOT_FOUND.name());
		}
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
