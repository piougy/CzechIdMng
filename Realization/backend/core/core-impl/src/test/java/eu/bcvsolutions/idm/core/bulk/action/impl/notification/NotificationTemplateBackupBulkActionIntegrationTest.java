package eu.bcvsolutions.idm.core.bulk.action.impl.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.service.Recoverable;
import eu.bcvsolutions.idm.core.api.utils.ZipUtils;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link RedeployBulkAction}.
 * 
 * 
 * FIXME: prevent to reuse test template.
 *
 * @author Ondrej Husnik
 */
public class NotificationTemplateBackupBulkActionIntegrationTest extends AbstractBulkActionTest {
	
	private static final String TEST_TEMPLATE = "testTemplate";
	private static final String TEST_BACKUP_FOLDER = "/tmp/idm_test_backup/";
	private static final String CHANGED_TEST_DESC = "CHANGED_TEST_DESC";
	
	private IdmIdentityDto loggedUser; 

	@Autowired
	private IdmNotificationTemplateService templateService;
	@Autowired
	private LongRunningTaskManager longRunningTaskManager;
	@Autowired
	private AttachmentManager attachmentManager;
	
	
	@Before
	public void login() {
		loggedUser = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(loggedUser.getUsername());
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, TEST_BACKUP_FOLDER);
		cleanUp();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkActionByIds() throws IOException {
		IdmNotificationTemplateDto template = templateService.getByCode(TEST_TEMPLATE);
		Set<UUID> templates = new HashSet<UUID>();
		templates.add(template.getId());
		
		template.setSubject(CHANGED_TEST_DESC);
		IdmNotificationTemplateDto templateOne = templateService.save(template);
		assertEquals(templateOne.getSubject(), CHANGED_TEST_DESC);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmNotificationTemplate.class,  NotificationTemplateBackupBulkAction.NAME);
		bulkAction.setIdentifiers(templates);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		
		testBackupFileContent(templateOne, loggedUser.getUsername());
		
		// test attachment is created for LRT
		IdmLongRunningTaskDto task = longRunningTaskManager.getLongRunningTask(processAction.getLongRunningTaskId());
		List<IdmAttachmentDto> attachments = attachmentManager.getAttachments(task, null).getContent();
		Assert.assertEquals(1, attachments.size());
		try (InputStream attachmentData = attachmentManager.getAttachmentData(attachments.get(0).getId())) {
			Assert.assertNotNull(attachmentData);
			// save
			File zipFile = attachmentManager.createTempFile();
			Files.copy(attachmentData, zipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			// and extract file
			Path zipFolder = attachmentManager.createTempDirectory(null);
			ZipUtils.extract(zipFile, zipFolder.toString());
			//
			File[] listFiles = zipFolder.toFile().listFiles();
			Assert.assertEquals(1, listFiles.length);
			Assert.assertEquals(String.format("%s.xml", templateOne.getCode()), listFiles[0].getName());
		}
	}
	
	@Test
	public void processBulkActionByFilter() {
		IdmNotificationTemplateDto template1 = templateService.getByCode(TEST_TEMPLATE);
		Set<UUID> templates = new HashSet<UUID>();
		templates.add(template1.getId());
			
		template1.setSubject(CHANGED_TEST_DESC);
		template1 = templateService.save(template1);
		assertEquals(template1.getSubject(), CHANGED_TEST_DESC);
		
		IdmNotificationTemplateFilter filter = new IdmNotificationTemplateFilter();
		filter.setText(CHANGED_TEST_DESC);
		
		List<IdmNotificationTemplateDto> checkScripts = templateService.find(filter, null).getContent();
		assertEquals(1, checkScripts.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmNotificationTemplate.class,  NotificationTemplateBackupBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		
		testBackupFileContent(template1, loggedUser.getUsername());
	}
	
	@Test
	public void prevalidationBulkActionByIds() {
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, "");
		IdmNotificationTemplateDto template1 = templateService.getByCode(TEST_TEMPLATE);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmNotificationTemplate.class, NotificationTemplateBackupBulkAction.NAME);
		bulkAction.getIdentifiers().add(template1.getId());
		
		// None info results
		ResultModels resultModels = bulkActionManager.prevalidate(bulkAction);
		List<ResultModel> results = resultModels.getInfos();
		assertEquals(1, results.size());
		assertEquals(results.get(0).getStatusEnum(), CoreResultCode.BACKUP_FOLDER_NOT_FOUND.toString());
	}

	@Test
	public void processBulkActionWithoutPermission() {
		IdmNotificationTemplateDto template1 = templateService.getByCode(TEST_TEMPLATE);
		Set<UUID> templates = new HashSet<UUID>();
		templates.add(template1.getId());
		
		template1.setSubject(CHANGED_TEST_DESC);
		template1 = templateService.save(template1);
		assertEquals(template1.getSubject(), CHANGED_TEST_DESC);
		
		// user hasn't permission for script update
		IdmIdentityDto readerIdentity = this.createUserWithAuthorities(IdmBasePermission.UPDATE);
		loginAsNoAdmin(readerIdentity.getUsername());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmNotificationTemplate.class,  NotificationTemplateBackupBulkAction.NAME);
		bulkAction.setIdentifiers(templates);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 0l, null, null);
		
		// no backup files exits
		assertNull(getBackupFiles());
	}

	/**
	 * Cleans backup directory after every test 
	 */
	private void cleanUp() {
		String bckFolderName = configurationService.getValue(Recoverable.BACKUP_FOLDER_CONFIG);
		File path = new File(bckFolderName);
		if (path.exists() && path.isDirectory()) {
			try {
				FileUtils.deleteDirectory(path);
			} catch (IOException e) {
				fail("Unable to clean up backup directory!");
			}
		}
	}
	
	/**
	 * Get backup files for logged user and specified script
	 * 
	 * @param script
	 * @param loggedUserName
	 * @return
	 */
	private File[] getBackupFiles() {
		ZonedDateTime date = ZonedDateTime.now();
		DecimalFormat decimalFormat = new DecimalFormat("00");
		File directory = new File(TEST_BACKUP_FOLDER + "templates/" + date.getYear()
				+ decimalFormat.format(date.getMonthValue()) + decimalFormat.format(date.getDayOfMonth()) + "/");
		return directory.listFiles();
	}
	
	/**
	 * Test found file contains expected description
	 * 
	 * @param script
	 * @param loggedUserName
	 * @return
	 */
	private void testBackupFileContent(IdmNotificationTemplateDto script, String loggedUserName) {
		try {
			File[] files = getBackupFiles();
			assertEquals(1, files.length);
			File backup = files[0];
			assertTrue(backup.exists());
			assertTrue(backup.getName().contains(loggedUserName));
			assertTrue(backup.getName().contains(script.getCode()));
			
			String content = new String(Files.readAllBytes(backup.toPath()), StandardCharsets.UTF_8);
			assertTrue(content.contains("<subject>" + script.getSubject() + "</subject>"));
		} catch (Exception e) {
			fail();
		}
	}
}
