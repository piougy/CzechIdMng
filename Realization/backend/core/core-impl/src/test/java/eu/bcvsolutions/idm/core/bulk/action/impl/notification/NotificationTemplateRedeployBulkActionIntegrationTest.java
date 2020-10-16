package eu.bcvsolutions.idm.core.bulk.action.impl.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.service.Recoverable;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link RedeployBulkAction}
 *
 * @author Ondrej Husnik
 *
 */

public class NotificationTemplateRedeployBulkActionIntegrationTest extends AbstractBulkActionTest {
	
	private static final String TEST_TEMPLATE = "testTemplate";
	private static final String TEST_BACKUP_FOLDER = "/tmp/idm_test_backup/";
	private static final String CHANGED_TEST_DESC = "CHANGED_TEST_DESC";

	@Autowired
	private IdmNotificationTemplateService templateService;
	
	
	@Before
	public void login() {
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.UPDATE);
		loginAsNoAdmin(adminIdentity.getUsername());
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, TEST_BACKUP_FOLDER);
		cleanUp();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkActionByIds() {
		IdmNotificationTemplateDto template1 = templateService.getByCode(TEST_TEMPLATE);
		Set<UUID> templates = new HashSet<UUID>();
		templates.add(template1.getId());
		
		String origSubj = template1.getSubject();
		template1.setSubject(CHANGED_TEST_DESC);
		template1 = templateService.save(template1);
		assertNotEquals(template1.getSubject(), origSubj);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmNotificationTemplate.class,  NotificationTemplateRedeployBulkAction.NAME);
		bulkAction.setIdentifiers(templates);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		
		template1 = templateService.get(template1.getId());
		assertEquals(template1.getSubject(), origSubj);
	}
	
	@Test
	public void processBulkActionByFilter() {
		IdmNotificationTemplateDto template1 = templateService.getByCode(TEST_TEMPLATE);
		Set<UUID> templates = new HashSet<UUID>();
		templates.add(template1.getId());
			
		String origSubj = template1.getSubject();
		template1.setSubject(CHANGED_TEST_DESC);
		template1 = templateService.save(template1);
		assertNotEquals(template1.getSubject(), origSubj);
		
		IdmNotificationTemplateFilter filter = new IdmNotificationTemplateFilter();
		filter.setText(CHANGED_TEST_DESC);
		
		List<IdmNotificationTemplateDto> checkScripts = templateService.find(filter, null).getContent();
		assertEquals(1, checkScripts.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmNotificationTemplate.class, NotificationTemplateRedeployBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		template1 = templateService.get(template1.getId());
		assertEquals(template1.getSubject(), origSubj);
	}
	
	@Test
	public void prevalidationBulkActionByIds() {
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, "");
		IdmNotificationTemplateDto template1 = templateService.getByCode(TEST_TEMPLATE);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmNotificationTemplate.class, NotificationTemplateRedeployBulkAction.NAME);
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
		
		String origSubj = template1.getSubject();
		template1.setSubject(CHANGED_TEST_DESC);
		template1 = templateService.save(template1);
		assertNotEquals(template1.getSubject(), origSubj);
		
		// user hasn't permission for script update
		IdmIdentityDto readerIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(readerIdentity.getUsername());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmNotificationTemplate.class,  NotificationTemplateRedeployBulkAction.NAME);
		bulkAction.setIdentifiers(templates);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 0l, null, null);
		template1 = templateService.get(template1.getId());
		assertEquals(template1.getSubject(), CHANGED_TEST_DESC);
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
}
