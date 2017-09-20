package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.api.service.Recoverable;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic test for test backup, deploy and redeploy.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class DefaultIdmSriptServiceIntegrationTest extends AbstractIntegrationTest {

	private static final String TEST_SCRIPT_CODE_1 = "testScript1";
	private static final String TEST_SCRIPT_CODE_2 = "testScript2";
	
	private static final String TEST_SCRIPT_NAME_1 = "Test script 1";
	
	private static final String TEST_BACKUP_FOLDER = "/tmp/idm_test_backup/";

	@Autowired
	private IdmScriptService scriptService;

	@Autowired
	private IdmScriptAuthorityService scriptAuthorityService;
	
	@Autowired
	private ConfigurationService configurationService;

	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void initTest() {
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		IdmScriptDto script2 = scriptService.getByCode(TEST_SCRIPT_CODE_2);

		assertNotNull(script1);
		assertNotNull(script2);

		assertEquals(TEST_SCRIPT_CODE_1, script1.getCode());
		assertEquals(TEST_SCRIPT_CODE_2, script2.getCode());

		IdmScriptAuthorityFilter filter = new IdmScriptAuthorityFilter();
		filter.setScriptId(script1.getId());
		List<IdmScriptAuthorityDto> authorities = scriptAuthorityService.find(filter, null).getContent();

		assertEquals(4, authorities.size());

		filter.setScriptId(script2.getId());
		authorities = scriptAuthorityService.find(filter, null).getContent();

		assertEquals(0, authorities.size());
	}

	@Test
	public void removeAuthRedeploy() {
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, TEST_BACKUP_FOLDER);
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		assertNotNull(script1);

		IdmScriptAuthorityFilter filter = new IdmScriptAuthorityFilter();
		filter.setScriptId(script1.getId());
		List<IdmScriptAuthorityDto> authorities = scriptAuthorityService.find(filter, null).getContent();

		assertEquals(4, authorities.size());

		scriptAuthorityService.deleteAllByScript(script1.getId());

		authorities = scriptAuthorityService.find(filter, null).getContent();

		assertEquals(0, authorities.size());

		scriptService.redeploy(script1);

		authorities = scriptAuthorityService.find(filter, null).getContent();

		assertEquals(4, authorities.size());
	}

	@Test
	public void deleteScriptRedeploy() {
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		IdmScriptDto script2 = scriptService.getByCode(TEST_SCRIPT_CODE_2);

		assertNotNull(script1);
		assertNotNull(script2);

		scriptService.delete(script1);
		scriptService.delete(script2);

		script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		script2 = scriptService.getByCode(TEST_SCRIPT_CODE_2);

		assertNull(script1);
		assertNull(script2);

		scriptService.init();

		script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		script2 = scriptService.getByCode(TEST_SCRIPT_CODE_2);

		assertNotNull(script1);
		assertNotNull(script2);
	}

	@Test
	public void tryRedeployMissingScript() {
		IdmScriptDto script = new IdmScriptDto();
		script.setCategory(IdmScriptCategory.SYSTEM);
		script.setCode("test_" + System.currentTimeMillis());
		script.setName("test_" + System.currentTimeMillis());

		script = scriptService.save(script);
		assertNotNull(script);
		assertNotNull(script.getId());

		try {
			scriptService.redeploy(script);
			fail();
		} catch (ResultCodeException e) {
			ResultCodeException resultCode = (ResultCodeException) e;
			assertEquals(resultCode.getError().getError().getStatusEnum(),
					CoreResultCode.SCRIPT_XML_FILE_NOT_FOUND.name());
		}
	}
	
	@Test
	public void tryRedepoloyScript() {
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, TEST_BACKUP_FOLDER);
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);

		assertNotNull(script1);
		
		assertEquals(TEST_SCRIPT_NAME_1, script1.getName());
		
		String changeName = "test_change_" + System.currentTimeMillis();
		script1.setName(changeName);
		script1 = scriptService.save(script1);
		
		assertEquals(changeName, script1.getName());
		
		try {
			script1 = scriptService.redeploy(script1);
			assertEquals(TEST_SCRIPT_NAME_1, script1.getName());
		} catch (ResultCodeException e) {
			fail();
		}
	}
	
	@Test
	public void backupMissingFolderExistEntity() {
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, null);
		
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);

		assertNotNull(script1);
		
		try {
			scriptService.backup(script1);
			fail();
		} catch (ResultCodeException e) {
			ResultCodeException resultCode = (ResultCodeException) e;
			assertEquals(resultCode.getError().getError().getStatusEnum(),
					CoreResultCode.BACKUP_FOLDER_NOT_FOUND.name());
		}
	}
	
	@Test
	public void backupMissingFolderNewEntity() {
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, null);
		
		IdmScriptDto script = new IdmScriptDto();
		script.setCategory(IdmScriptCategory.SYSTEM);
		script.setCode("test_" + System.currentTimeMillis());
		script.setName("test_" + System.currentTimeMillis());

		script = scriptService.save(script);
		assertNotNull(script);
		assertNotNull(script.getId());

		try {
			scriptService.backup(script);
			fail();
		} catch (ResultCodeException e) {
			ResultCodeException resultCode = (ResultCodeException) e;
			assertEquals(resultCode.getError().getError().getStatusEnum(),
					CoreResultCode.BACKUP_FOLDER_NOT_FOUND.name());
		}
	}
	
	@Test
	public void tryBackup() {
		File directory = new File(TEST_BACKUP_FOLDER);
		if (directory.exists() && directory.isDirectory()) {
			try {
				FileUtils.deleteDirectory(directory);
			} catch (IOException e) {
				fail();
			}
		}
		//
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);

		assertNotNull(script1);
		
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, TEST_BACKUP_FOLDER);
		//
		try {
			IdmScriptDto newDto = scriptService.redeploy(script1);
			assertEquals(script1.getCode(), newDto.getCode());
			//
			DateTime date = new DateTime();
			DecimalFormat decimalFormat = new DecimalFormat("00");
			directory = new File(TEST_BACKUP_FOLDER + "scripts/" + date.getYear()
					+ decimalFormat.format(date.getMonthOfYear()) + decimalFormat.format(date.getDayOfMonth()) + "/");
			File[] files = directory.listFiles();
			assertEquals(1, files.length);
			File backup = files[0];
			assertTrue(backup.exists());
			assertTrue(backup.getName().contains("admin"));
			assertTrue(backup.getName().contains(script1.getCode()));
		} catch (Exception e) {
			fail();
		}
	}
}
