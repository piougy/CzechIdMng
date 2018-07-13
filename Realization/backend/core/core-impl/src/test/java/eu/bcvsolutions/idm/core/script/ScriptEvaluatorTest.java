package eu.bcvsolutions.idm.core.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.hibernate.envers.internal.tools.Triple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;

import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.domain.ScriptAuthorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmScriptAuthority;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmTreeTypeService;
import eu.bcvsolutions.idm.core.script.evaluator.AbstractScriptEvaluator;
import eu.bcvsolutions.idm.core.script.evaluator.DefaultScriptEvaluator;
import eu.bcvsolutions.idm.core.security.exception.IdmSecurityException;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;


/**
 * Default test for evaluating scripts inside another script.
 * Also include some integration test with CRUD scripts.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class ScriptEvaluatorTest extends AbstractIntegrationTest {

	private String TREE_TYPE_NAME = "test_script_tree_type_name";
	private String TREE_TYPE_CODE = "test_script_tree_type_code";
	
	@Autowired
	private IdmScriptService scriptService;
	
	@Autowired
	private IdmScriptAuthorityService scriptAuthorityService;
	
	private PluginRegistry<AbstractScriptEvaluator, IdmScriptCategory> pluginExecutors; 
	
	@Autowired
	private List<AbstractScriptEvaluator> executors; 
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private GroovyScriptService groovyScriptService;
	
	@Autowired
	private IdmTreeTypeService treeTypeService;
	
	@Autowired
	private DefaultScriptEvaluator defaultScriptEvaluator;
	
	@Before
	public void init() {
		if (pluginExecutors == null) {
			pluginExecutors = OrderAwarePluginRegistry.create(executors);
		}
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testRemoveScriptWithAuthentization() {
		IdmScriptDto script = new IdmScriptDto();
		script.setCategory(IdmScriptCategory.DEFAULT);
		script.setCode("script_code_" + System.currentTimeMillis());
		script.setName("script_name_" + System.currentTimeMillis());
		//
		script = scriptService.saveInternal(script);
		
		IdmScriptAuthorityDto auth = createAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, List.class.getName(), null);
		IdmScriptAuthorityDto auth2 = createAuthority(script.getId(), ScriptAuthorityType.CLASS_NAME, ArrayList.class.getName(), null);
		//
		scriptService.deleteInternal(script);
		//
		assertNull(scriptAuthorityService.get(auth.getId()));
		assertNull(scriptAuthorityService.get(auth2.getId()));
	}
	
	@Test
	public void testCreateScript() {
		IdmScriptDto script = new IdmScriptDto();
		script.setCategory(IdmScriptCategory.DEFAULT);
		script.setCode("script_code_" + System.currentTimeMillis());
		script.setName("script_name_" + System.currentTimeMillis());
		//
		IdmScriptDto script2 = scriptService.saveInternal(script);
		//
		assertEquals(script.getCode(), script2.getCode());
		assertEquals(script.getName(), script2.getName());
		assertEquals(script.getCategory(), script2.getCategory());
	}
	
	@Test
	public void testCreateDuplScriptName() {
		String duplName = "script_name";
		//
		IdmScriptDto script = new IdmScriptDto();
		script.setCategory(IdmScriptCategory.DEFAULT);
		script.setCode("script_code_" + System.currentTimeMillis());
		script.setName(duplName);
		//
		script = scriptService.saveInternal(script);
		//
		IdmScriptDto script2 = new IdmScriptDto();
		script2.setCategory(IdmScriptCategory.DEFAULT);
		script2.setCode("script_code_" + System.currentTimeMillis());
		script2.setName(duplName);
		//
		script2 = scriptService.saveInternal(script2);
		//
		assertNotEquals(script.getCode(), script2.getCode());
		assertEquals(script.getName(), script2.getName());
		assertEquals(script.getCategory(), script2.getCategory());
	}
	
	@Test(expected = DataIntegrityViolationException.class)
	public void testCreateDuplScriptCode() {
		String duplCode = "script_code";
		//
		IdmScriptDto script = new IdmScriptDto();
		script.setCategory(IdmScriptCategory.DEFAULT);
		script.setCode(duplCode);
		script.setName("script_name_" + System.currentTimeMillis());
		//
		script = scriptService.saveInternal(script);
		//
		IdmScriptDto script2 = new IdmScriptDto();
		script2.setCategory(IdmScriptCategory.DEFAULT);
		script2.setCode(duplCode);
		script2.setName("script_name_" + System.currentTimeMillis());
		//
		script2 = scriptService.saveInternal(script2);
	}
	
	@Test(expected = IdmSecurityException.class)
	public void testEvaluateScriptWithoutAuth() {
		IdmScriptDto script = new IdmScriptDto();
		script.setCategory(IdmScriptCategory.DEFAULT);
		script.setCode("script_name_" + System.currentTimeMillis());
		script.setName("script_name_" + System.currentTimeMillis());
		//
		script.setScript(createListScript(Boolean.TRUE));
		//
		script = scriptService.saveInternal(script);
		//
		IdmScriptDto script2 = new IdmScriptDto();
		script2.setCategory(IdmScriptCategory.DEFAULT);
		script2.setCode("script_name_" + System.currentTimeMillis());
		script2.setName("script_name_" + System.currentTimeMillis());
		//
		script.setScript(createScriptThatCallAnother(script, IdmScriptCategory.DEFAULT, null, false));
		//
		script2 = scriptService.saveInternal(script2);
		//
		groovyScriptService.evaluate(script.getScript(), createParametersWithEvaluator(IdmScriptCategory.DEFAULT), createExtraAllowedClass());
	}
	
	@Test
	public void testEvaluateScriptWithAuth() {
		IdmScriptDto subScript = new IdmScriptDto();
		subScript.setCategory(IdmScriptCategory.DEFAULT);
		subScript.setCode("script_name_" + System.currentTimeMillis());
		subScript.setName("script_name_" + System.currentTimeMillis());
		//
		subScript.setScript(createListScript(Boolean.TRUE));
		//
		subScript = scriptService.saveInternal(subScript);
		//
		createAuthority(subScript.getId(), ScriptAuthorityType.CLASS_NAME, IdmRole.class.getName(), null);
		//
		IdmScriptDto parent = new IdmScriptDto();
		parent.setCategory(IdmScriptCategory.DEFAULT);
		parent.setCode("script_name_" + System.currentTimeMillis());
		parent.setName("script_name_" + System.currentTimeMillis());
		//
		parent.setScript(createScriptThatCallAnother(subScript, IdmScriptCategory.DEFAULT, null, false));
		parent = scriptService.saveInternal(parent);
		//
		groovyScriptService.evaluate(parent.getScript(), createParametersWithEvaluator(IdmScriptCategory.DEFAULT), createExtraAllowedClass());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testEvaluateScriptWithAnotherCategory() {
		IdmScriptDto subScript = new IdmScriptDto();
		subScript.setCategory(IdmScriptCategory.SYSTEM);
		subScript.setCode("script_name_" + System.currentTimeMillis());
		subScript.setName("script_name_" + System.currentTimeMillis());
		//
		subScript.setScript(createListScript(Boolean.TRUE));
		//
		subScript = scriptService.saveInternal(subScript);
		//
		createAuthority(subScript.getId(), ScriptAuthorityType.CLASS_NAME, IdmRole.class.getName(), null);
		//
		IdmScriptDto parent = new IdmScriptDto();
		parent.setCategory(IdmScriptCategory.DEFAULT);
		parent.setCode("script_name_" + System.currentTimeMillis());
		parent.setName("script_name_" + System.currentTimeMillis());
		//
		parent.setScript(createScriptThatCallAnother(subScript, IdmScriptCategory.DEFAULT, null, false));
		parent = scriptService.saveInternal(parent);
		//
		groovyScriptService.evaluate(parent.getScript(), createParametersWithEvaluator(IdmScriptCategory.DEFAULT), createExtraAllowedClass());
	}
	
	@Test
	public void testEvaluateScriptWithCreateEntity() {
		IdmScriptDto subScript = new IdmScriptDto();
		subScript.setCategory(IdmScriptCategory.DEFAULT);
		subScript.setCode("script_name_" + System.currentTimeMillis());
		subScript.setName("script_name_" + System.currentTimeMillis());
		//
		subScript.setScript(createTreeNodeScript(TREE_TYPE_CODE, TREE_TYPE_NAME));
		//
		subScript = scriptService.save(subScript);
		//
		createAuthority(subScript.getId(), ScriptAuthorityType.CLASS_NAME, IdmTreeTypeDto.class.getName(), null);
		//
		createAuthority(subScript.getId(), ScriptAuthorityType.SERVICE, DefaultIdmTreeTypeService.class.getCanonicalName(), "treeTypeService");
		//
		IdmScriptDto parent = new IdmScriptDto();
		parent.setCategory(IdmScriptCategory.DEFAULT);
		parent.setCode("script_name_" + System.currentTimeMillis());
		parent.setName("script_name_" + System.currentTimeMillis());
		//
		parent.setScript(createScriptThatCallAnother(subScript, IdmScriptCategory.DEFAULT, null, true));
		parent = scriptService.save(parent);
		//
		Object uuid = groovyScriptService.evaluate(parent.getScript(), createParametersWithEvaluator(IdmScriptCategory.DEFAULT), createExtraAllowedClass());
		//
		assertNotNull(uuid);
		//
		IdmTreeTypeDto treeType = this.treeTypeService.get(UUID.fromString(uuid.toString()));
		//
		assertNotNull(treeType);
		assertEquals(this.TREE_TYPE_CODE, treeType.getCode());
		assertEquals(this.TREE_TYPE_NAME, treeType.getName());
	}
	
	@Test(expected = IdmSecurityException.class)
	public void testEvaluateScriptWithCreateEntityAndWorkWith() {
		IdmScriptDto subScript = new IdmScriptDto();
		subScript.setCategory(IdmScriptCategory.DEFAULT);
		subScript.setCode("script_name_" + System.currentTimeMillis());
		subScript.setName("script_name_" + System.currentTimeMillis());
		//
		subScript.setScript(createTreeNodeScript(TREE_TYPE_CODE + "_2", TREE_TYPE_NAME + "_2"));
		//
		subScript = scriptService.save(subScript);
		//
		createAuthority(subScript.getId(), ScriptAuthorityType.CLASS_NAME, IdmTreeTypeDto.class.getName(), null);
		//
		createAuthority(subScript.getId(), ScriptAuthorityType.SERVICE, this.treeTypeService.getClass().getName(), "treeTypeService");
		//
		IdmScriptDto parent = new IdmScriptDto();
		parent.setCategory(IdmScriptCategory.DEFAULT);
		parent.setCode("script_name_" + System.currentTimeMillis());
		parent.setName("script_name_" + System.currentTimeMillis());
		//
		StringBuilder scriptToEnd = new StringBuilder();
		scriptToEnd.append("import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;\n");
		scriptToEnd.append("IdmTreeTypeDto entity = new IdmTreeTypeDto();\n");
		scriptToEnd.append("entity.setCode('" + TREE_TYPE_CODE + "_3');\n");
		scriptToEnd.append("entity.setName('" + TREE_TYPE_NAME + "_3');\n");
		scriptToEnd.append("return 'test';\n");
		//
		parent.setScript(createScriptThatCallAnother(subScript, IdmScriptCategory.DEFAULT, null, false, scriptToEnd.toString()));
		parent = scriptService.saveInternal(parent);
		//
		groovyScriptService.evaluate(parent.getScript(), createParametersWithEvaluator(IdmScriptCategory.DEFAULT), createExtraAllowedClass());
		//
		fail();
	}
	
	@Test
	public void testThreeScriptAuthoritySuccess() {
		IdmScriptDto third = new IdmScriptDto();
		third.setCategory(IdmScriptCategory.DEFAULT);
		third.setCode("third_script_name_" + System.currentTimeMillis());
		third.setName("third_script_name_" + System.currentTimeMillis());
		StringBuilder script = new StringBuilder();
		script.append(createRoleScript());
		script.append("return null;\n");
		third.setScript(script.toString());
		third = scriptService.save(third);
		createAuthority(third.getId(), ScriptAuthorityType.CLASS_NAME, IdmRole.class.getName(), null);
		//
		IdmScriptDto second = new IdmScriptDto();
		second.setCategory(IdmScriptCategory.DEFAULT);
		second.setCode("second_script_name_" + System.currentTimeMillis());
		second.setName("second_script_name_" + System.currentTimeMillis());
		script = new StringBuilder();
		script.append(createTreeTypeScript());
		script.append(createScriptThatCallAnother(third, IdmScriptCategory.DEFAULT, null, false, null));
		script.append("return null;\n");
		second.setScript(script.toString());
		//
		second = scriptService.save(second);
		createAuthority(second.getId(), ScriptAuthorityType.CLASS_NAME, IdmTreeTypeDto.class.getName(), null);
		createAuthority(second.getId(), ScriptAuthorityType.CLASS_NAME, IdmTreeNodeDto.class.getName(), null);
		//
		//
		IdmScriptDto first = new IdmScriptDto();
		first.setCategory(IdmScriptCategory.DEFAULT);
		first.setCode("first_script_name_" + System.currentTimeMillis());
		first.setName("first_script_name_" + System.currentTimeMillis());
		script = new StringBuilder();
		script.append(createIdenityScript());
		script.append("\n");
		script.append(createRoleScript());
		script.append(createScriptThatCallAnother(second, IdmScriptCategory.DEFAULT, null, false, null));
		script.append("return null;\n");
		first.setScript(script.toString());
		first = scriptService.save(first);
		createAuthority(first.getId(), ScriptAuthorityType.CLASS_NAME, IdmIdentity.class.getName(), null);
		createAuthority(first.getId(), ScriptAuthorityType.CLASS_NAME, IdmRole.class.getName(), null);
		//
		//
		defaultScriptEvaluator.evaluate(first.getCode());
	}
	
	@Test(expected = IdmSecurityException.class)
	public void testThreeScriptAuthorityFailSecond() {
		IdmScriptDto third = new IdmScriptDto();
		third.setCategory(IdmScriptCategory.DEFAULT);
		third.setCode("script_name_" + System.currentTimeMillis());
		third.setName("script_name_" + System.currentTimeMillis());
		third.setScript(createTreeNodeScript());
		third = scriptService.save(third);
		createAuthority(third.getId(), ScriptAuthorityType.CLASS_NAME, IdmTreeNodeDto.class.getName(), null);
		//
		IdmScriptDto second = new IdmScriptDto();
		second.setCategory(IdmScriptCategory.DEFAULT);
		second.setCode("script_name_" + System.currentTimeMillis());
		second.setName("script_name_" + System.currentTimeMillis());
		StringBuilder script = new StringBuilder(createTreeTypeScript());
		script.append(createTreeNodeScript());// security exception
		script.append(createScriptThatCallAnother(third, IdmScriptCategory.DEFAULT, null, false, null));
		second.setScript(script.toString());
		second = scriptService.save(second);
		createAuthority(second.getId(), ScriptAuthorityType.CLASS_NAME, IdmTreeTypeDto.class.getName(), null);
		//
		//
		IdmScriptDto first = new IdmScriptDto();
		first.setCategory(IdmScriptCategory.DEFAULT);
		first.setCode("script_name_" + System.currentTimeMillis());
		first.setName("script_name_" + System.currentTimeMillis());
		script = new StringBuilder(createIdenityScript());
		script.append(createScriptThatCallAnother(second, IdmScriptCategory.DEFAULT, null, false, null));
		first.setScript(script.toString());
		first = scriptService.save(first);
		createAuthority(first.getId(), ScriptAuthorityType.CLASS_NAME, IdmIdentity.class.getName(), null);
		//
		//
		defaultScriptEvaluator.evaluate(first.getCode());
		fail();
	}
	
	@Test(expected = IdmSecurityException.class)
	public void testThreeScriptAuthorityThirdScriptUseAnotherAuth() {
		IdmScriptDto third = new IdmScriptDto();
		third.setCategory(IdmScriptCategory.DEFAULT);
		third.setCode("script_name_" + System.currentTimeMillis());
		third.setName("script_name_" + System.currentTimeMillis());
		StringBuilder script = new StringBuilder(createTreeNodeScript());
		script.append(createIdenityScript()); // fail
		third.setScript(script.toString());
		third = scriptService.save(third);
		createAuthority(third.getId(), ScriptAuthorityType.CLASS_NAME, IdmTreeNodeDto.class.getName(), null);
		//
		IdmScriptDto second = new IdmScriptDto();
		second.setCategory(IdmScriptCategory.DEFAULT);
		second.setCode("script_name_" + System.currentTimeMillis());
		second.setName("script_name_" + System.currentTimeMillis());
		script = new StringBuilder(createTreeTypeScript());
		script.append(createTreeNodeScript());
		second.setScript(createScriptThatCallAnother(third, IdmScriptCategory.DEFAULT, null, false, null));
		second = scriptService.save(second);
		createAuthority(second.getId(), ScriptAuthorityType.CLASS_NAME, IdmTreeTypeDto.class.getName(), null);
		//
		//
		IdmScriptDto first = new IdmScriptDto();
		first.setCategory(IdmScriptCategory.DEFAULT);
		first.setCode("script_name_" + System.currentTimeMillis());
		first.setName("script_name_" + System.currentTimeMillis());
		script = new StringBuilder(createIdenityScript());
		script.append(createScriptThatCallAnother(second, IdmScriptCategory.DEFAULT, null, false, null));
		first.setScript(script.toString());
		first = scriptService.save(first);
		createAuthority(first.getId(), ScriptAuthorityType.CLASS_NAME, IdmIdentity.class.getName(), null);
		//
		//
		defaultScriptEvaluator.evaluate(first.getCode());
		fail();
	}
	
	@Test
	public void testEvaluateLogScript() {
		groovyScriptService.evaluate(createLogScript(), null);
	}
	
	@Test
	public void testScriptExceptionWithCatch() {
		String testString = "TEST-" + System.currentTimeMillis();
		//
		Triple<IdmScriptDto, IdmScriptDto, IdmScriptDto> scripts = createThreeScripts();
		IdmScriptDto script1 = scripts.getFirst();
		IdmScriptDto script2 = scripts.getSecond();
		IdmScriptDto script3 = scripts.getThird();
		//
		StringBuilder script1Body = new StringBuilder();
		script1Body.append("try {\n");
		script1Body.append(this.createScriptThatCallAnother(script2, IdmScriptCategory.DEFAULT, null, false));
		script1Body.append("} catch (Throwable e) {\n");
		script1Body.append("return \"" + testString + "\";\n");
		script1Body.append("}\n");
		script1Body.append("return null;\n");
		//
		script1.setScript(script1Body.toString());
		script2.setScript(this.createScriptThatCallAnother(script3, IdmScriptCategory.DEFAULT, null, false));
		script3.setScript("throw new Exception(\""+ testString +"\")");
		//
		script1 = this.scriptService.save(script1);
		script2 = this.scriptService.save(script2);
		script3 = this.scriptService.save(script3);
		try {
			Object result = defaultScriptEvaluator.evaluate(script1.getCode());
			assertNotNull(result);
			assertEquals(testString, result);
		} catch (Throwable e) {
			fail();
		}
	}
	
	@Test
	public void testScriptThrowableWithCatch() {
		String testString = "TEST-" + System.currentTimeMillis();
		//
		Triple<IdmScriptDto, IdmScriptDto, IdmScriptDto> scripts = createThreeScripts();
		IdmScriptDto script1 = scripts.getFirst();
		IdmScriptDto script2 = scripts.getSecond();
		IdmScriptDto script3 = scripts.getThird();
		//
		StringBuilder script1Body = new StringBuilder();
		script1Body.append("try {\n");
		script1Body.append(this.createScriptThatCallAnother(script2, IdmScriptCategory.DEFAULT, null, false));
		script1Body.append("} catch (Throwable e) {\n");
		script1Body.append("return \"" + testString + "\";\n");
		script1Body.append("}\n");
		script1Body.append("return null;\n");
		//
		script1.setScript(script1Body.toString());
		script2.setScript(this.createScriptThatCallAnother(script3, IdmScriptCategory.DEFAULT, null, false));
		script3.setScript("throw new Throwable(\""+ testString +"\")");
		//
		script1 = this.scriptService.save(script1);
		script2 = this.scriptService.save(script2);
		script3 = this.scriptService.save(script3);
		try {
			Object result = defaultScriptEvaluator.evaluate(script1.getCode());
			assertNotNull(result);
			assertEquals(testString, result);
		} catch (Throwable e) {
			fail();
		}
	}
	
	@Test
	public void testScriptRuntimeExceptionWithCatch() {
		String testString = "TEST-" + System.currentTimeMillis();
		//
		Triple<IdmScriptDto, IdmScriptDto, IdmScriptDto> scripts = createThreeScripts();
		IdmScriptDto script1 = scripts.getFirst();
		IdmScriptDto script2 = scripts.getSecond();
		IdmScriptDto script3 = scripts.getThird();
		//
		StringBuilder script1Body = new StringBuilder();
		script1Body.append("try {\n");
		script1Body.append(this.createScriptThatCallAnother(script2, IdmScriptCategory.DEFAULT, null, false));
		script1Body.append("} catch (Throwable e) {\n");
		script1Body.append("return \"" + testString + "\";\n");
		script1Body.append("}\n");
		script1Body.append("return null;\n");
		//
		script1.setScript(script1Body.toString());
		script2.setScript(this.createScriptThatCallAnother(script3, IdmScriptCategory.DEFAULT, null, false));
		script3.setScript("throw new RuntimeException(\""+ testString +"\")");
		//
		script1 = this.scriptService.save(script1);
		script2 = this.scriptService.save(script2);
		script3 = this.scriptService.save(script3);
		try {
			Object result = defaultScriptEvaluator.evaluate(script1.getCode());
			assertNotNull(result);
			assertEquals(testString, result);
		} catch (Throwable e) {
			fail();
		}
	}
	
	@Test(expected = IdmSecurityException.class)
	public void testScriptSecurityExceptionWithCatchSecurityException() {
		String testString = "TEST-" + System.currentTimeMillis();
		//
		Triple<IdmScriptDto, IdmScriptDto, IdmScriptDto> scripts = createThreeScripts();
		IdmScriptDto script1 = scripts.getFirst();
		IdmScriptDto script2 = scripts.getSecond();
		IdmScriptDto script3 = scripts.getThird();
		//
		StringBuilder script1Body = new StringBuilder();
		script1Body.append("try {\n");
		script1Body.append(this.createScriptThatCallAnother(script2, IdmScriptCategory.DEFAULT, null, false));
		script1Body.append("} catch (SecurityException e) {\n");
		script1Body.append("return \"" + testString + "\";\n");
		script1Body.append("}\n");
		script1Body.append("return null;\n");
		//
		script1.setScript(script1Body.toString());
		script2.setScript(this.createScriptThatCallAnother(script3, IdmScriptCategory.DEFAULT, null, false));
		script3.setScript("throw new SecurityException(\""+ testString +"\")");
		//
		script1 = this.scriptService.save(script1);
		script2 = this.scriptService.save(script2);
		script3 = this.scriptService.save(script3);
		// must throw exception
		defaultScriptEvaluator.evaluate(script1.getCode());
		fail();
	}
	
	@Test
	public void testScriptSecurityExceptionWithCatch() {
		String testString = "TEST-" + System.currentTimeMillis();
		//
		Triple<IdmScriptDto, IdmScriptDto, IdmScriptDto> scripts = createThreeScripts();
		IdmScriptDto script1 = scripts.getFirst();
		IdmScriptDto script2 = scripts.getSecond();
		IdmScriptDto script3 = scripts.getThird();
		//
		StringBuilder script1Body = new StringBuilder();
		script1Body.append("try {\n");
		script1Body.append(this.createScriptThatCallAnother(script2, IdmScriptCategory.DEFAULT, null, false));
		script1Body.append("} catch (Throwable e) {\n");
		script1Body.append("return \"" + testString + "\";\n");
		script1Body.append("}\n");
		script1Body.append("return null;\n");
		//
		script1.setScript(script1Body.toString());
		script2.setScript(this.createScriptThatCallAnother(script3, IdmScriptCategory.DEFAULT, null, false));
		script3.setScript("throw new SecurityException(\""+ testString +"\")");
		//
		script1 = this.scriptService.save(script1);
		script2 = this.scriptService.save(script2);
		script3 = this.scriptService.save(script3);
		//
		try {
			// try catch SecurityException but propage this exception as throwable, this is possible
			Object result = defaultScriptEvaluator.evaluate(script1.getCode());
			assertNotNull(result);
			assertEquals(testString, result);
		} catch (Throwable e) {
			fail();
		}
	}
	
	@Test
	public void testScriptExceptionWithoutCatch() {
		String testString = "TEST-" + System.currentTimeMillis();
		//
		Triple<IdmScriptDto, IdmScriptDto, IdmScriptDto> scripts = createThreeScripts();
		IdmScriptDto script1 = scripts.getFirst();
		IdmScriptDto script2 = scripts.getSecond();
		IdmScriptDto script3 = scripts.getThird();
		//
		script1.setScript(this.createScriptThatCallAnother(script2, IdmScriptCategory.DEFAULT, null, false));
		script2.setScript(this.createScriptThatCallAnother(script3, IdmScriptCategory.DEFAULT, null, false));
		script3.setScript("throw new Exception(\""+ testString +"\")");
		//
		script1 = this.scriptService.save(script1);
		script2 = this.scriptService.save(script2);
		script3 = this.scriptService.save(script3);
		try {
			// must throw exception
			defaultScriptEvaluator.evaluate(script1.getCode());
			fail();
		} catch (Throwable e) {
			assertTrue(e instanceof ResultCodeException);
			assertTrue(e.getMessage().contains(testString));
		}
	}
	
	@Test
	public void testScriptThrowableWithoutCatch() {
		String testString = "TEST-" + System.currentTimeMillis();
		//
		Triple<IdmScriptDto, IdmScriptDto, IdmScriptDto> scripts = createThreeScripts();
		IdmScriptDto script1 = scripts.getFirst();
		IdmScriptDto script2 = scripts.getSecond();
		IdmScriptDto script3 = scripts.getThird();
		//
		script1.setScript(this.createScriptThatCallAnother(script2, IdmScriptCategory.DEFAULT, null, false));
		script2.setScript(this.createScriptThatCallAnother(script3, IdmScriptCategory.DEFAULT, null, false));
		script3.setScript("throw new Throwable(\""+ testString +"\")");
		//
		script1 = this.scriptService.save(script1);
		script2 = this.scriptService.save(script2);
		script3 = this.scriptService.save(script3);
		try {
			// must throw exception
			defaultScriptEvaluator.evaluate(script1.getCode());
			fail();
		} catch (Throwable e) {
			assertTrue(e instanceof Throwable);
			assertTrue(e.getMessage().contains(testString));
		}
	}
	
	@Test
	public void testScriptRuntimeExceptionWithoutCatch() {
		String testString = "TEST-" + System.currentTimeMillis();
		//
		Triple<IdmScriptDto, IdmScriptDto, IdmScriptDto> scripts = createThreeScripts();
		IdmScriptDto script1 = scripts.getFirst();
		IdmScriptDto script2 = scripts.getSecond();
		IdmScriptDto script3 = scripts.getThird();
		//
		script1.setScript(this.createScriptThatCallAnother(script2, IdmScriptCategory.DEFAULT, null, false));
		script2.setScript(this.createScriptThatCallAnother(script3, IdmScriptCategory.DEFAULT, null, false));
		script3.setScript("throw new RuntimeException(\""+ testString +"\")");
		//
		script1 = this.scriptService.save(script1);
		script2 = this.scriptService.save(script2);
		script3 = this.scriptService.save(script3);
		try {
			// must throw exception
			defaultScriptEvaluator.evaluate(script1.getCode());
			fail();
		} catch (Throwable e) {
			assertTrue(e instanceof RuntimeException);
			assertTrue(e.getMessage().contains(testString));
		}
	}
	
	@Test(expected = IdmSecurityException.class)
	public void testScriptSecurityExceptionWithoutCatch() {
		String testString = "TEST-" + System.currentTimeMillis();
		//
		Triple<IdmScriptDto, IdmScriptDto, IdmScriptDto> scripts = createThreeScripts();
		IdmScriptDto script1 = scripts.getFirst();
		IdmScriptDto script2 = scripts.getSecond();
		IdmScriptDto script3 = scripts.getThird();
		//
		script1.setScript(this.createScriptThatCallAnother(script2, IdmScriptCategory.DEFAULT, null, false));
		script2.setScript(this.createScriptThatCallAnother(script3, IdmScriptCategory.DEFAULT, null, false));
		script3.setScript("throw new SecurityException(\""+ testString +"\")");
		//
		script1 = this.scriptService.save(script1);
		script2 = this.scriptService.save(script2);
		script3 = this.scriptService.save(script3);
		// must throw exception
		defaultScriptEvaluator.evaluate(script1.getCode());
		fail();
	}
	
	/**
	 * Method create simple script return as string. 
	 * Parameter returnString is used for return value in script. Return List.toString() or List
	 * @param returnString
	 * @return
	 */
	private String createListScript(Boolean returnString) {
		StringBuilder script = new StringBuilder();
		script.append("import java.util.List;\n");
		script.append("import java.util.ArrayList;\n");
		script.append("import eu.bcvsolutions.idm.core.model.entity.IdmRole;\n");
		script.append("List<IdmRole> list = new ArrayList<>();\n");
		script.append("list.add(new IdmRole());\n");
		script.append("list.add(new IdmRole());\n");
		script.append("list.add(new IdmRole());\n");
		if (returnString != null && returnString) {
			script.append("return list.toString();\n");
		} else {
			script.append("return list;\n");
		}
		return script.toString();
		
	}
	
	/**
	 * Method create three unique script with same code and name. Script body isn't filled.
	 * 
	 * @return
	 */
	private Triple<IdmScriptDto, IdmScriptDto, IdmScriptDto> createThreeScripts() {
		String codeName1 = "script_name_" + System.currentTimeMillis();
		IdmScriptDto script1 = new IdmScriptDto();
		script1.setCategory(IdmScriptCategory.DEFAULT);
		script1.setCode(codeName1);
		script1.setName(codeName1);
		script1 = this.scriptService.save(script1);
		//
		String codeName2 = "script_name_" + System.currentTimeMillis();
		IdmScriptDto script2 = new IdmScriptDto();
		script2.setCategory(IdmScriptCategory.DEFAULT);
		script2.setCode(codeName2);
		script2.setName(codeName2);
		script2 = this.scriptService.save(script2);
		//
		String codeName3 = "script_name_" + System.currentTimeMillis();
		IdmScriptDto script3 = new IdmScriptDto();
		script3.setCategory(IdmScriptCategory.DEFAULT);
		script3.setCode(codeName3);
		script3.setName(codeName3);
		script3 = this.scriptService.save(script3);
		//
		return new Triple<IdmScriptDto, IdmScriptDto, IdmScriptDto>(script1, script2, script3);
	}
	
	private String createLogScript() {
		StringBuilder script = new StringBuilder();
		script.append("org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(\"script_name\");\n");
		script.append("LOG.info(\"test\");\n");
		return script.toString();
		
	}
	
	/**
	 * Method create script that create {@link IdmTreeNodeDto} and return ID for newly created {@link IdmTreeNodeDto}
	 * Is necessary to include to this script service for {@link IdmTreeNodeService} and allow use of {@link IdmTreeNodeDto} class
	 * 
	 * When will be change {@link IdmTreeNodeDto} to DTO is necessary to fix this method and SCRIPT INSIDE!
	 * (imports and entities)
	 * 
	 * @return
	 */
	private String createTreeNodeScript(String code, String name) {
		StringBuilder script = new StringBuilder();
		// TODO: refactor IdmTreeType to dto
		script.append("import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;\n");
		script.append("IdmTreeTypeDto entity = new IdmTreeTypeDto();\n");
		script.append("entity.setCode('" + code + "');\n");
		script.append("entity.setName('" + name + "');\n");
		script.append("entity = treeTypeService.save(entity);\n");
		script.append("return entity.getId();\n");
		return script.toString();
	}
	
	/**
	 * Create script that call another script, into this script will be include scriptEvaluator for category.
	 * 
	 * @param scriptCode
	 * @return
	 */
	private String createScriptThatCallAnother(IdmScriptDto scriptDto, IdmScriptCategory category, Map<String, String> parameters, boolean returnAfterFinish) {
		return createScriptThatCallAnother(scriptDto, category, parameters, returnAfterFinish, null);
	}
	
	/**
	 * Create script that call another script, into this script will be include scriptEvaluator for category.
	 * Also is possible to add own script to end of script. But parameter returnAfterFinish must be set to false
	 * 
	 * @param scriptCode
	 * @return
	 */
	private String createScriptThatCallAnother(IdmScriptDto scriptDto, IdmScriptCategory category, Map<String, String> parameters, boolean returnAfterFinish, String scriptToEnd) {
		AbstractScriptEvaluator evaluator = getEvaluatorForCategory(category);
		//
		IdmScriptAuthority auth = new IdmScriptAuthority();
		auth.setType(ScriptAuthorityType.SERVICE);
		auth.setClassName(evaluator.getClass().getName());
		String[] servicesName = applicationContext.getBeanNamesForType(evaluator.getClass());
		auth.setService(servicesName[0]);
		//
		StringBuilder script = new StringBuilder();
		if (returnAfterFinish) {
			script.append("return " + AbstractScriptEvaluator.SCRIPT_EVALUATOR + ".evaluate(\n");
		} else {
			script.append(AbstractScriptEvaluator.SCRIPT_EVALUATOR + ".evaluate(\n");
		}
		script.append("    " + AbstractScriptEvaluator.SCRIPT_EVALUATOR + ".newBuilder()\n");
		script.append("        .setScriptCode('" + scriptDto.getCode() + "')\n");
		script.append("        .addParameter('" + AbstractScriptEvaluator.SCRIPT_EVALUATOR + "', " + AbstractScriptEvaluator.SCRIPT_EVALUATOR + ")\n");
		//
		if (parameters != null) {
			for (Entry<String, String> entry : parameters.entrySet()) {
				script.append("        .addParameter('" + entry.getKey() + "', " + entry.getValue() + ")\n");
			}
		}
		//
		script.append("	.build());\n");
		if (scriptToEnd != null) {
			script.append(scriptToEnd);
		}
		return script.toString();
	}
	
	/**
	 * Method get evaluator from plugin executors for {@link IdmScriptCategory}
	 * @param category
	 * @return
	 */
	private AbstractScriptEvaluator getEvaluatorForCategory(IdmScriptCategory category) {
		return pluginExecutors.getPluginFor(category);
	}
	
	/**
	 * Method create parameters with instance of evaluator for {@link IdmScriptCategory}
	 * @param category
	 * @return
	 */
	private Map<String, Object> createParametersWithEvaluator(IdmScriptCategory category) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(AbstractScriptEvaluator.SCRIPT_EVALUATOR, getEvaluatorForCategory(category));
		return parameters;
	}
	
	/**
	 * Method create default extra allowed class.
	 * {@link AbstractScriptEvaluator.Builder}
	 * @return
	 */
	private List<Class<?>> createExtraAllowedClass() {
		List<Class<?>> extraAllowedClass = new ArrayList<>();
		extraAllowedClass.add(AbstractScriptEvaluator.Builder.class);
		return extraAllowedClass;
	}
	
	/**
	 * Method create and save {@link IdmScriptAuthority} for script id fiven in paramete
	 * @param scriptId
	 * @param type
	 * @param className
	 * @param service
	 * @return
	 */
	private IdmScriptAuthorityDto createAuthority(UUID scriptId, ScriptAuthorityType type, String className, String service) {
		IdmScriptAuthorityDto auth = new IdmScriptAuthorityDto();
		auth.setClassName(className);
		auth.setType(type);
		auth.setScript(scriptId);
		if (type == ScriptAuthorityType.SERVICE) {
			auth.setService(service);
		}
		return scriptAuthorityService.saveInternal(auth);
	}
	
	private String createIdenityScript() {
		StringBuilder identity = new StringBuilder();
		identity.append("import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;\n");
		identity.append("IdmIdentity identity_" + System.currentTimeMillis() + " = new IdmIdentity();\n");
		return identity.toString();
	}
	
	private String createRoleScript() {
		StringBuilder role = new StringBuilder();
		role.append("import eu.bcvsolutions.idm.core.model.entity.IdmRole;\n");
		role.append("IdmRole role_" + System.currentTimeMillis() + " = new IdmRole();\n");
		return role.toString();
	}
	
	private String createTreeNodeScript() {
		StringBuilder treeNode = new StringBuilder();
		treeNode.append("import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;\n");
		treeNode.append("IdmTreeNodeDto treeNode_" + System.currentTimeMillis() + " = new IdmTreeNodeDto();\n");
		return treeNode.toString();
	}
	
	private String createTreeTypeScript() {
		StringBuilder treeType = new StringBuilder();
		treeType.append("import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;\n");
		treeType.append("IdmTreeTypeDto treeType_" + System.currentTimeMillis() + " = new IdmTreeTypeDto();\n");
		return treeType.toString();
	}
}
