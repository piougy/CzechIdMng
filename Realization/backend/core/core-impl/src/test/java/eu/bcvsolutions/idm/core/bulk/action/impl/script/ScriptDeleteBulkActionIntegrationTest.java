package eu.bcvsolutions.idm.core.bulk.action.impl.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptFilter;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.model.entity.IdmScript;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link ScriptDeleteBulkAction}
 *
 * @author Ondrej Husnik
 *
 */

public class ScriptDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {
	
	private static final String TEST_SCRIPT_CODE_1 = "testScript1";
	private static final String TEST_SCRIPT_CODE_2 = "testScript2";
	private static final String TEST_SCRIPT_CODE_3 = "testScript3";

	@Autowired
	private IdmScriptService scriptService;
	
	@Before
	public void login() {
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.DELETE, IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkActionByIds() {
		scriptService.init();
		
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		IdmScriptDto script2 = scriptService.getByCode(TEST_SCRIPT_CODE_2);
		IdmScriptDto script3 = scriptService.getByCode(TEST_SCRIPT_CODE_3);
		
		Set<UUID> scripts = new HashSet<UUID>();
		scripts.add(script1.getId());
		scripts.add(script2.getId());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmScript.class, ScriptDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(scripts);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 2l, null, null);
		
		for (UUID id : scripts) {
			IdmScriptDto scriptDto = scriptService.get(id);
			assertNull(scriptDto);
		}
		assertNotNull(scriptService.get(script3.getId()));
	}
	

	@Test
	public void processBulkActionByFilter() {
		scriptService.init();
		
		String desc = "script description" + getHelper().createName();
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		IdmScriptDto script2 = scriptService.getByCode(TEST_SCRIPT_CODE_2);
		IdmScriptDto script3 = scriptService.getByCode(TEST_SCRIPT_CODE_3);
		Set<UUID> scripts = new HashSet<UUID>();
		scripts.add(script1.getId());
		scripts.add(script2.getId());
			
		script1.setDescription(desc);
		script1 = scriptService.save(script1);
		script2.setDescription(desc);
		script2 = scriptService.save(script2);
		
		IdmScriptFilter filter = new IdmScriptFilter();
		filter.setDescription(desc);

		List<IdmScriptDto> checkScripts = scriptService.find(filter, null).getContent();
		assertEquals(2, checkScripts.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmScript.class, ScriptDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 2l, null, null);
		
		for (UUID id : scripts) {
			IdmScriptDto scriptDto = scriptService.get(id);
			assertNull(scriptDto);
		}
		assertNotNull(scriptService.get(script3.getId()));
	}

	@Test
	public void processBulkActionWithoutPermission() {
		scriptService.init();
		// user hasn't permission for update role
		IdmIdentityDto readerIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(readerIdentity.getUsername());
		
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		IdmScriptDto script2 = scriptService.getByCode(TEST_SCRIPT_CODE_2);
		IdmScriptDto script3 = scriptService.getByCode(TEST_SCRIPT_CODE_3);
		
		Set<UUID> scripts = new HashSet<UUID>();
		scripts.add(script1.getId());
		scripts.add(script2.getId());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmScript.class, ScriptDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(scripts);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 0l, null, null);
		
		assertNotNull(scriptService.get(script1.getId()));
		assertNotNull(scriptService.get(script2.getId()));
		assertNotNull(scriptService.get(script3.getId()));
	}
}
