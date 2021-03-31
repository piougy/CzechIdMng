package eu.bcvsolutions.idm.core.bulk.action.impl.script;


import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.ScriptAuthorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptFilter;
import eu.bcvsolutions.idm.core.api.service.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.model.entity.IdmScript;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link ScriptBackupBulkAction}.
 * 
 *
 * @author Ondrej Husnik
 * @since 11.0.0
 * 
 */
public class ScriptDuplicateBulkActionIntegrationTest extends AbstractBulkActionTest {

	private static String SCRIPT_CODE_PREFIX = "JustPrefixForDistinguishingAmongOther";
	private IdmIdentityDto loggedUser; 

	@Autowired
	private IdmScriptService scriptService;
	@Autowired
	private IdmScriptAuthorityService scriptAuthorityService;
	
	@Before
	public void login() {
		loggedUser = this.createUserWithAuthorities(IdmBasePermission.CREATE, IdmBasePermission.READ);
		loginAsNoAdmin(loggedUser.getUsername());
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkActionByIds() {
		IdmScriptDto original = createIdmScript();
		
		IdmScriptFilter filt = new IdmScriptFilter();
		filt.setText(original.getCode());
		List<IdmScriptDto> dtos = scriptService.find(filt, null).getContent();
		Assert.assertEquals(1,  dtos.size());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmScript.class, ScriptDuplicateBulkAction.NAME);
		bulkAction.getIdentifiers().add(original.getId());
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		
		dtos = scriptService.find(filt, null).getContent();
		Assert.assertEquals(2,  dtos.size());
		Assert.assertTrue(compareScripts(dtos.get(0), dtos.get(1)));
		cleanScript(dtos);
	}
	
	

	@Test
	public void processBulkActionWithoutPermission() {
		IdmScriptDto original = createIdmScript();
		
		IdmScriptFilter filt = new IdmScriptFilter();
		filt.setText(original.getCode());
		List<IdmScriptDto> dtos = scriptService.find(filt, null).getContent();
		Assert.assertEquals(1,  dtos.size());
		
		IdmIdentityDto readerIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(readerIdentity.getUsername());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmScript.class, ScriptDuplicateBulkAction.NAME);
		bulkAction.getIdentifiers().add(original.getId());
		
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, null, null, 1l);
		
		dtos = scriptService.find(filt, null).getContent();
		Assert.assertEquals(1,  dtos.size());
		Assert.assertEquals(original.getId(), dtos.get(0).getId());
		cleanScript(dtos);
	}
	
	/**
	 * Creates test script
	 * @return
	 */
	private IdmScriptDto createIdmScript() {
		IdmScriptDto script = new IdmScriptDto();
		script.setCode(SCRIPT_CODE_PREFIX+getHelper().createName());
		script.setName(getHelper().createName());
		script.setDescription(getHelper().createName());
		script.setTemplate(getHelper().createName());
		script.setScript("import org.apache.commons.lang3.StringUtils;\nreturn StringUtils.trim(\" XXXXXX \");");
		script = scriptService.save(script);
		
		IdmScriptAuthorityDto authorityDto = new IdmScriptAuthorityDto();
		authorityDto.setScript(script.getId());
		authorityDto.setType(ScriptAuthorityType.CLASS_NAME);
		authorityDto.setClassName("org.apache.commons.lang3.StringUtils");
		scriptAuthorityService.save(authorityDto);
		
		return script;
	}
	
	/**
	 * Temporary function for comparison of scripts 
	 * @param script1
	 * @param script2
	 * @return
	 */
	private boolean compareScripts(IdmScriptDto script1, IdmScriptDto script2) {
		return StringUtils.equals(script1.getName(), script2.getName())
				&& StringUtils.equals(script1.getScript(), script2.getScript())
				&& StringUtils.equals(script1.getDescription(), script2.getDescription())
				&& StringUtils.equals(script1.getTemplate(), script2.getTemplate())
				&& Objects.equals(script1.getCategory(), script2.getCategory())
				&& compareScriptAuthority(script1, script2);
	}
	
	/**
	 * Temporary function for script authority comparison
	 * 
	 * @param script1
	 * @param script2
	 * @return
	 */
	private boolean compareScriptAuthority(IdmScriptDto script1, IdmScriptDto script2) {
		IdmScriptAuthorityFilter filt = new IdmScriptAuthorityFilter();
		filt.setScriptId(script1.getId());
		List<IdmScriptAuthorityDto> script1Auth = scriptAuthorityService.find(filt, null).getContent();
		filt.setScriptId(script2.getId());
		List<IdmScriptAuthorityDto> script2Auth = scriptAuthorityService.find(filt, null).getContent();
		
		Assert.assertEquals(script1Auth.size(), script2Auth.size());
		
		for (IdmScriptAuthorityDto orig : script2Auth) {
			boolean exists = script1Auth.stream().filter(tested -> {
				return StringUtils.equals(orig.getService(), tested.getService()) &&
						StringUtils.equals(orig.getClassName(), tested.getClassName()) &&
						Objects.equals(orig.getType(), tested.getType());
			}).count() > 0;
			if (!exists) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Cleans test scripts
	 * @param dtos
	 */
	private void cleanScript(List<IdmScriptDto> dtos) {
		for (IdmScriptDto dto : dtos) {
			scriptService.delete(dto);
		}
	}
	
}
