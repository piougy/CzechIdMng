package eu.bcvsolutions.idm.core.bulk.action.impl.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
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
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptFilter;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.api.service.Recoverable;
import eu.bcvsolutions.idm.core.model.entity.IdmScript;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link ScriptBackupBulkAction}
 *
 * @author Ondrej Husnik
 *
 */

public class ScriptBackupBulkActionIntegrationTest extends AbstractBulkActionTest {
	
	private static final String TEST_SCRIPT_CODE_1 = "testScript1";
	private static final String TEST_BACKUP_FOLDER = "/tmp/idm_test_backup/";
	private static final String CHANGED_TEST_DESC = "CHANGED_TEST_DESC";
	
	private IdmIdentityDto loggedUser; 

	@Autowired
	private IdmScriptService scriptService;
	
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
	public void processBulkActionByIds() {
		scriptService.init();
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		Set<UUID> scripts = new HashSet<UUID>();
		scripts.add(script1.getId());
		
		script1.setDescription(CHANGED_TEST_DESC);
		script1 = scriptService.save(script1);
		assertEquals(script1.getDescription(), CHANGED_TEST_DESC);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmScript.class, ScriptBackupBulkAction.NAME);
		bulkAction.setIdentifiers(scripts);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		
		// test the file exits and contains set description
		testBackupFileContent(script1, loggedUser.getUsername());
	}
	
	
	@Test
	public void prevalidationBulkActionByIds() {
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, "");
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmScript.class, ScriptBackupBulkAction.NAME);
		bulkAction.getIdentifiers().add(script1.getId());
		
		// None info results
		ResultModels resultModels = bulkActionManager.prevalidate(bulkAction);
		List<ResultModel> results = resultModels.getInfos();
		assertEquals(1, results.size());
		assertEquals(results.get(0).getStatusEnum(), CoreResultCode.BACKUP_FOLDER_NOT_FOUND.toString());
	}
	
	@Test
	public void processBulkActionByFilter() {
		scriptService.init();
				
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		Set<UUID> scripts = new HashSet<UUID>();
		scripts.add(script1.getId());
			
		script1.setDescription(CHANGED_TEST_DESC);
		script1 = scriptService.save(script1);
		
		IdmScriptFilter filter = new IdmScriptFilter();
		filter.setCode(TEST_SCRIPT_CODE_1);
		List<IdmScriptDto> checkScripts = scriptService.find(filter, null).getContent();
		assertEquals(1, checkScripts.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmScript.class, ScriptBackupBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		
		// test the file exits and contains set description
		testBackupFileContent(script1, loggedUser.getUsername());
	}

	@Test
	public void processBulkActionWithoutPermission() {
		scriptService.init();
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		Set<UUID> scripts = new HashSet<UUID>();
		scripts.add(script1.getId());
		
		script1.setDescription(CHANGED_TEST_DESC);
		script1 = scriptService.save(script1);
		
		// user doeasn't have permission for script backup
		IdmIdentityDto userIdentity = this.createUserWithAuthorities(IdmBasePermission.UPDATE);
		loginAsNoAdmin(userIdentity.getUsername());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmScript.class, ScriptBackupBulkAction.NAME);
		bulkAction.setIdentifiers(scripts);
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
		File directory = new File(TEST_BACKUP_FOLDER + "scripts/" + date.getYear()
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
	private void testBackupFileContent(IdmScriptDto script, String loggedUserName) {
		try {
			File[] files = getBackupFiles();
			assertEquals(1, files.length);
			File backup = files[0];
			assertTrue(backup.exists());
			assertTrue(backup.getName().contains(loggedUserName));
			assertTrue(backup.getName().contains(script.getCode()));
			
			String content = new String(Files.readAllBytes(backup.toPath()), StandardCharsets.UTF_8);
			assertTrue(content.contains("<description><![CDATA[" + script.getDescription() + "]]></description>"));
		} catch (Exception e) {
			fail();
		}
	}
}
