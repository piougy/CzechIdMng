package eu.bcvsolutions.idm.core.bulk.action.impl.script;

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
import org.junit.Assert;
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
 * Integration tests for {@link ScriptRedeployBulkAction}
 *
 * @author Ondrej Husnik
 *
 */

public class ScriptRedeployBulkActionIntegrationTest extends AbstractBulkActionTest {
	
	private static final String TEST_SCRIPT_CODE_1 = "testScript1";
	private static final String TEST_BACKUP_FOLDER = "/tmp/idm_test_backup/";
	private static final String CHANGED_TEST_DESC = "CHANGED_TEST_DESC";

	@Autowired
	private IdmScriptService scriptService;
	
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
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		Set<UUID> scripts = new HashSet<UUID>();
		scripts.add(script1.getId());
		
		String origDesc = script1.getDescription();
		script1.setDescription(CHANGED_TEST_DESC);
		script1 = scriptService.save(script1);
		assertNotEquals(script1.getDescription(), origDesc);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmScript.class, ScriptRedeployBulkAction.NAME);
		bulkAction.setIdentifiers(scripts);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		
		script1 = scriptService.get(script1.getId());
		assertEquals(script1.getDescription(), origDesc);
	}
	
	
	@Test
	public void processBulkActionByFilter() {
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		Set<UUID> scripts = new HashSet<UUID>();
		scripts.add(script1.getId());
			
		String origDesc = script1.getDescription();
		script1.setDescription(CHANGED_TEST_DESC);
		script1 = scriptService.save(script1);
		assertNotEquals(script1.getDescription(), origDesc);
		
		IdmScriptFilter filter = new IdmScriptFilter();
		filter.setCode(TEST_SCRIPT_CODE_1);
		List<IdmScriptDto> checkScripts = scriptService.find(filter, null).getContent();
		assertEquals(1, checkScripts.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmScript.class, ScriptRedeployBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		script1 = scriptService.get(script1.getId());
		assertEquals(script1.getDescription(), origDesc);
	}
	
	@Test
	public void prevalidationBulkActionByIds() {
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmScript.class, ScriptRedeployBulkAction.NAME);
		bulkAction.getIdentifiers().add(script1.getId());
		
		// None info results
		ResultModels resultModels = bulkActionManager.prevalidate(bulkAction);
		List<ResultModel> results = resultModels.getInfos();
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(n -> n.getStatusEnum().equals(CoreResultCode.BACKUP_FOLDER_FOUND.toString())));
		Assert.assertTrue(results.stream().anyMatch(n -> n.getStatusEnum().equals(CoreResultCode.DEPLOY_SCRIPT_FOLDER_FOUND.toString())));
	}

	@Test
	public void processBulkActionWithoutPermission() {
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		Set<UUID> scripts = new HashSet<UUID>();
		scripts.add(script1.getId());
		
		String origDesc = script1.getDescription();
		script1.setDescription(CHANGED_TEST_DESC);
		script1 = scriptService.save(script1);
		assertNotEquals(script1.getDescription(), origDesc);
		
		// user hasn't permission for script update
		IdmIdentityDto readerIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(readerIdentity.getUsername());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmScript.class, ScriptRedeployBulkAction.NAME);
		bulkAction.setIdentifiers(scripts);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 0l, null, null);
		script1 = scriptService.get(script1.getId());
		assertEquals(script1.getDescription(), CHANGED_TEST_DESC);
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
