package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.security.exception.IdmSecurityException;
import eu.bcvsolutions.idm.test.api.AbstractVerifiableUnitTest;

public class DefaultGroovyScriptServiceTest extends AbstractVerifiableUnitTest {

	private static final String TEST_ONE = "testOne";

	@Mock
	IdmCacheManager cacheManager;

	@InjectMocks
	private DefaultGroovyScriptService groovyScriptService;

	@After
	public void resetInteractions(){
		Mockito.reset(cacheManager);
	}

	@Test
	public void testScriptOne() {
		String result = (String) groovyScriptService.evaluate("return \"testOne\";", null);
		assertEquals(TEST_ONE, result);
	}

	@Test
	public void testValidScript() {
		String script = "if(true){ return \"testOne\"}";
		groovyScriptService.validateScript(script);
		String result = (String) groovyScriptService.evaluate(script, null);
		assertEquals(TEST_ONE, result);
	}

	@Test(expected = ResultCodeException.class)
	public void testUnvalidScript() {
		String script = "if(true) return \"testOne\"}";
		groovyScriptService.validateScript(script);
	}

	@Test
	public void testInputVariableScript() {
		String script = "if(attributeValue){return String.valueOf(!attributeValue);}else{return \"true\";}";
		groovyScriptService.validateScript(script);
		String result = (String) groovyScriptService.evaluate(script, ImmutableMap.of("attributeValue", true));
		assertEquals("false", result);
	}

	@Test(expected = ResultCodeException.class)
	public void testMissingInputVariableScript() {
		String script = "if(attributeValue){return String.valueOf(!attributeValue);}else{return \"true\";}";
		groovyScriptService.validateScript(script);
		groovyScriptService.evaluate(script, null);
	}

	@Test(expected = IdmSecurityException.class)
	public void testSecurityScriptSystemExitOne() {
		String script = "System.exit(1);";
		groovyScriptService.validateScript(script);
		groovyScriptService.evaluate(script, null);
	}

	@Test(expected = IdmSecurityException.class)
	public void testSecurityScriptSystemExitTwo() {
		String script = "def c = System; c.exit(-1);";
		groovyScriptService.validateScript(script);
		groovyScriptService.evaluate(script, null);
	}

	@Test(expected = IdmSecurityException.class)
	public void testSecurityScriptSystemExitThree() {
		String script = "((Object)System).exit(-1)";
		groovyScriptService.validateScript(script);
		groovyScriptService.evaluate(script, null);
	}

	@Test(expected = IdmSecurityException.class)
	public void testSecurityScriptSystemExitFour() {
		String script = "Class.forName('java.lang.System').exit(-1)";
		groovyScriptService.validateScript(script);
		groovyScriptService.evaluate(script, null);
	}

	@Test(expected = IdmSecurityException.class)
	public void testSecurityScriptSystemExitFive() {
		String script = "('java.lang.System' as Class).exit(-1)";
		groovyScriptService.validateScript(script);
		groovyScriptService.evaluate(script, null);
	}

	@Test(expected = IdmSecurityException.class)
	public void testSecurityScriptSystemExitSix() {
		String script = "import static java.lang.System.exit; exit(-1)";
		groovyScriptService.validateScript(script);
		groovyScriptService.evaluate(script, null);
	}

	@Test
	public void testSecurityScriptInputVariableValid() {
		String script = "return entity.username;";
		groovyScriptService.validateScript(script);
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(TEST_ONE);
		String result = (String) groovyScriptService.evaluate(script, ImmutableMap.of("entity", identity));
		assertEquals(TEST_ONE, result);
	}

	@Test(expected = IdmSecurityException.class)
	public void testSecurityScriptListDeepUnvalid() {
		String script = "return entity.getEavs().get(0);";
		groovyScriptService.validateScript(script);
		IdmIdentityRoleDto role = new IdmIdentityRoleDto();
		role.getEavs().add(new IdmFormInstanceDto());
		groovyScriptService.evaluate(script, ImmutableMap.of("entity", role));
	}

	@Test
	public void testSecurityScriptAllowInheritedClass() {
		String script = "return entity.getEavs().get(0);";
		groovyScriptService.validateScript(script);
		IdmIdentityRoleDto role = new IdmIdentityRoleDto();
		role.getEavs().add(new IdmFormInstanceDto());
		Object result = groovyScriptService.evaluate(script, ImmutableMap.of("entity", role), Lists.newArrayList(BaseDto.class));

		assertEquals(role.getEavs().get(0), result);
	}

	@Test
	public void testSecurityScriptListValid() {
		String script = "return formInstance;";
		groovyScriptService.validateScript(script);
		IdmIdentityRoleDto role = new IdmIdentityRoleDto();
		role.getEavs().add(new IdmFormInstanceDto());
		//
		Object result = groovyScriptService.evaluate(script, ImmutableMap.of("entity", role, "formInstance", role.getEavs().get(0)));
		//
		assertEquals(role.getEavs().get(0), result);
	}

	@Test(expected = IdmSecurityException.class)
	public void testSecurityScriptFile() {
		String script = "return new File();";
		groovyScriptService.validateScript(script);
		groovyScriptService.evaluate(script, null);
	}

}
