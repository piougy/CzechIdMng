package eu.bcvsolutions.idm.core.notification.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.SpinalCase;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitTestDataProcessor;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmEmailLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.api.service.EmailNotificationSender;
import eu.bcvsolutions.idm.core.notification.api.service.IdmEmailLogService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Nofification template tests:
 * - init from multiple locations
 * - template backup and redeploy
 * 
 * 
 * @author Ondřej Kopr
 * @author Radek Tomiška
 * @author Ondrej Husnik
 *
 */
public class DefaultIdmNotificationTemplateServiceIntegrationTest extends AbstractIntegrationTest {

	private static final String TEST_TEMPLATE = "testTemplate";
	private static final String TEST_TEMPLATE_TWO = "testTemplateTwo";
	private static final String TEST_TEMPLATE_OVERRIDE = "testTemplateOverride";
	//
	@Autowired private ApplicationContext context;
	@Autowired private ConfigurationService configurationService;
	@Autowired private IdmNotificationConfigurationService notificationConfigService;
	@Autowired private EmailNotificationSender emailSenderService;
	@Autowired private IdmEmailLogService emailLogService;
	//
	private DefaultIdmNotificationTemplateService notificationTemplateService;
	
	@Before
	public void init() {
		loginAsAdmin(InitTestDataProcessor.TEST_USER_1);
		notificationTemplateService = context.getAutowireCapableBeanFactory().createBean(DefaultIdmNotificationTemplateService.class);
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void initTest() {
		IdmNotificationTemplateDto template = notificationTemplateService.getByCode(TEST_TEMPLATE);

		assertNotNull(template);

		assertEquals(TEST_TEMPLATE, template.getCode());
	}
	
	@Test
	public void initFromMultipleLocations() {
		IdmNotificationTemplateDto templateTwo = notificationTemplateService.getByCode(TEST_TEMPLATE_TWO);
		IdmNotificationTemplateDto templateOverride = notificationTemplateService.getByCode(TEST_TEMPLATE_OVERRIDE);
		
		assertNotNull(templateTwo);
		assertNotNull(templateOverride);

		assertEquals(TEST_TEMPLATE_TWO, templateTwo.getCode());
		assertEquals(TEST_TEMPLATE_OVERRIDE, templateOverride.getCode());
		//
		assertEquals("CzechIdM - test isOverriden", templateOverride.getSubject());
		assertEquals("isOverriden", templateOverride.getBodyHtml().trim());
	}
	
	@Test
	public void redeployExistTemplate() {
		String backupFolder = "/tmp/idm_test_backup/";
		//
		configurationService.setValue(DefaultIdmNotificationTemplateService.BACKUP_FOLDER_CONFIG, backupFolder);
		//
		IdmNotificationTemplateDto testTemplate = notificationTemplateService.getByCode(TEST_TEMPLATE);
		Assert.assertNotNull(testTemplate);
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
		IdmNotificationTemplateDto testTemplate = notificationTemplateService.getByCode(TEST_TEMPLATE);
		Assert.assertNotNull(testTemplate);
		assertEquals(TEST_TEMPLATE, testTemplate.getCode());
		//
		try {
			IdmNotificationTemplateDto newDto = notificationTemplateService.redeploy(testTemplate);
			assertEquals(testTemplate.getCode(), newDto.getCode());
			//
			ZonedDateTime date = ZonedDateTime.now();
			DecimalFormat decimalFormat = new DecimalFormat("00");
			directory = new File(backupFolder + "templates/" + date.getYear()
					+ decimalFormat.format(date.getMonthValue()) + decimalFormat.format(date.getDayOfMonth()) + "/");
			File[] files = directory.listFiles();
			assertEquals(1, files.length);
			File backup = files[0];
			assertTrue(backup.exists());
			assertTrue(backup.getName().contains(SpinalCase.format(InitTestDataProcessor.TEST_USER_1)));
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
		IdmNotificationTemplateDto testTemplate = notificationTemplateService.getByCode(TEST_TEMPLATE);
		Assert.assertNotNull(testTemplate);
		assertEquals(TEST_TEMPLATE, testTemplate.getCode());
		//
		IdmNotificationTemplateDto testTemplateNew = notificationTemplateService.redeploy(testTemplate);
		//
		assertEquals(testTemplateNew.getId(), testTemplate.getId());
		//
		// after redeploy check directory
		ZonedDateTime date = ZonedDateTime.now();
		DecimalFormat decimalFormat = new DecimalFormat("00");
		directory = new File(backupFolder + "templates/" + date.getYear()
				+ decimalFormat.format(date.getMonthValue()) + decimalFormat.format(date.getDayOfMonth()) + "/");
		assertTrue(directory.exists());
		assertTrue(directory.isDirectory());
		//
		File[] files = directory.listFiles();
		assertEquals(1, files.length);
		File backup = files[0];
		assertTrue(backup.exists());
		assertTrue(backup.getName().contains(SpinalCase.format(InitTestDataProcessor.TEST_USER_1)));
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
	public void evaluateEmptyHtmlText() {
		IdmNotificationTemplateDto template = new IdmNotificationTemplateDto();
		template.setCode(getHelper().createName());
		template.setName(getHelper().createName());
		template.setSubject(getHelper().createName());
		template.setBodyText(getHelper().createName());
		template = notificationTemplateService.save(template);

		IdmMessageDto message = new IdmMessageDto.Builder().setTemplate(template).build();

		notificationTemplateService.buildMessage(message);
	}

	@Test
	public void evaluateEmptyText() {
		IdmNotificationTemplateDto template = new IdmNotificationTemplateDto();
		template.setCode(getHelper().createName());
		template.setName(getHelper().createName());
		template.setSubject(getHelper().createName());
		template.setBodyHtml(getHelper().createName());
		template = notificationTemplateService.save(template);

		IdmMessageDto message = new IdmMessageDto.Builder().setTemplate(template).build();

		notificationTemplateService.buildMessage(message);
	}

	@Test(expected = ConstraintViolationException.class)
	public void evaluateEmptySubject() {
		IdmNotificationTemplateDto template = new IdmNotificationTemplateDto();
		template.setCode(getHelper().createName());
		template.setName(getHelper().createName());
		template.setBodyText(getHelper().createName());
		template.setBodyHtml(getHelper().createName());
		// throw error subject can't be null
		template = notificationTemplateService.save(template);
	}
	
	
	@Test
	public void templateReferentialIntegrityTest() {
		IdmNotificationTemplateDto template = new IdmNotificationTemplateDto();
		template.setCode(getHelper().createName());
		template.setName(getHelper().createName());
		template.setSubject(getHelper().createName());
		template = notificationTemplateService.save(template);

		// Template Dto successfully saved
		IdmNotificationTemplateFilter templateFilter = new IdmNotificationTemplateFilter();
		templateFilter.setText(template.getCode());
		assertEquals(1, notificationTemplateService.find(templateFilter, null).getContent().size());

		// Prevent from template deleting if used in notification configuration
		NotificationConfigurationDto notificationCfgDto = new NotificationConfigurationDto(getHelper().createName(),
				NotificationLevel.INFO, getHelper().createName(), getHelper().createName(), template.getId());
		notificationCfgDto = notificationConfigService.save(notificationCfgDto);
		try {
			notificationTemplateService.delete(template);
			fail("Template deleted although used in a notification configuration.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}
		notificationConfigService.delete(notificationCfgDto);

		// Found proper notification according to the used template
		IdmMessageDto message = new IdmMessageDto.Builder().setTemplate(template).build();
		List<IdmEmailLogDto> emailLogDtos = emailSenderService.send(getHelper().createName(), message);
		assertEquals(1, emailLogDtos.size());

		IdmNotificationFilter notificationFilter = new IdmNotificationFilter();
		notificationFilter.setTemplateId(template.getId());
		List<IdmEmailLogDto> foundNotificationDtos = emailLogService.find(notificationFilter, null).getContent();
		assertEquals(1, foundNotificationDtos.size());
		assertEquals(emailLogDtos.get(0).getId(), foundNotificationDtos.get(0).getId());

		// Prevent from template deleting if used in notification
		try {
			notificationTemplateService.delete(template);
			fail("Template deleted although used in a notification.");
		} catch (ResultCodeException e) {
			// Success
		} catch (Exception e) {
			fail(e.getMessage());
		}
		emailLogService.delete(emailLogDtos.get(0));

		// Template Dto successfully deleted
		notificationTemplateService.delete(template);
		assertEquals(0, notificationTemplateService.find(templateFilter, null).getContent().size());
	}

	@Test
	public void checkCdataTag() {
		String testBodyText = getHelper().createName();
		String testBodyHtml = getHelper().createName();
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
		IdmNotificationTemplateDto testTemplate = notificationTemplateService.getByCode(TEST_TEMPLATE);
		Assert.assertNotNull(testTemplate);
		assertEquals(TEST_TEMPLATE, testTemplate.getCode());
		//
		testTemplate.setBodyHtml(testBodyHtml);
		testTemplate.setBodyText(testBodyText);
		//
		notificationTemplateService.backup(testTemplate);
		//
		ZonedDateTime date = ZonedDateTime.now();
		DecimalFormat decimalFormat = new DecimalFormat("00");
		directory = new File(backupFolder + "templates/" + date.getYear()
				+ decimalFormat.format(date.getMonthValue()) + decimalFormat.format(date.getDayOfMonth()) + "/");
		assertTrue(directory.exists());
		assertTrue(directory.isDirectory());
		//
		File[] files = directory.listFiles();
		assertEquals(1, files.length);
		File backup = files[0];
		assertTrue(backup.exists());

		String content = null;
		try {
			content = new String(Files.readAllBytes(backup.toPath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}

		assertTrue(content.contains("<bodyHtml><![CDATA[" + testBodyHtml + "]]></bodyHtml>"));
		assertTrue(content.contains("<bodyText><![CDATA[" + testBodyText + "]]></bodyText>"));
	}
}
