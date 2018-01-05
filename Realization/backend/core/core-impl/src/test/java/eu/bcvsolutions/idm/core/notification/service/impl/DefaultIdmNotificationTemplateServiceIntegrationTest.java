package eu.bcvsolutions.idm.core.notification.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Nofification template tests:
 * - init, init from multiple locations
 * - template backup and redeploy
 * 
 * 
 * @author Ondřej Kopr
 * @author Radek Tomiška
 *
 */
public class DefaultIdmNotificationTemplateServiceIntegrationTest extends AbstractIntegrationTest {

	private static final String TEST_TEMPLATE = "testTemplate";
	private static final String TEST_TEMPLATE_TWO = "testTemplateTwo";
	private static final String TEST_TEMPLATE_OVERRIDE = "testTemplateOverride";
	//
	@Autowired private ApplicationContext context;
	@Autowired private ConfigurationService configurationService;
	//
	private DefaultIdmNotificationTemplateService notificationTemplateService;
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_USER_1);
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
	public void redeployWithoutBackupFolder() {
		String backupPath = configurationService.getValue(DefaultIdmNotificationTemplateService.BACKUP_FOLDER_CONFIG);
		if (backupPath != null) {
			configurationService.setValue(DefaultIdmNotificationTemplateService.BACKUP_FOLDER_CONFIG, null);
		}
		//
		IdmNotificationTemplateDto testTemplate = notificationTemplateService.getByCode(TEST_TEMPLATE);
		Assert.assertNotNull(testTemplate);
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
		IdmNotificationTemplateDto testTemplate = notificationTemplateService.getByCode(TEST_TEMPLATE);
		Assert.assertNotNull(testTemplate);
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
			assertTrue(backup.getName().contains(InitTestData.TEST_USER_1));
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
		assertTrue(backup.getName().contains(InitTestData.TEST_USER_1));
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
}
