package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleAuthority;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultGroovyScriptService;
import eu.bcvsolutions.idm.core.security.exception.IdmSecurityException;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;
import groovy.lang.MissingPropertyException;

public class DefaultGroovyScriptServiceTest extends AbstractUnitTest {

	private static final String TEST_ONE = "testOne";
	private GroovyScriptService groovyScriptService;

	@Before
	public void init() {
		groovyScriptService = new DefaultGroovyScriptService();
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

	@Test(expected = MissingPropertyException.class)
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
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername(TEST_ONE);
		String result = (String) groovyScriptService.evaluate(script, ImmutableMap.of("entity", identity));
		assertEquals(TEST_ONE, result);
	}

	@Test(expected = IdmSecurityException.class)
	public void testSecurityScriptListDeepUnvalid() {
		String script = "return entity.authorities;";
		groovyScriptService.validateScript(script);
		IdmRole role = new IdmRole();
		List<IdmRoleAuthority> authorities = new ArrayList<>();
		authorities.add(new IdmRoleAuthority());
		role.setAuthorities(authorities);
		role.setName(TEST_ONE);
		groovyScriptService.evaluate(script, ImmutableMap.of("entity", role));
	}

	@Test
	public void testSecurityScriptListValid() {
		String script = "return list;";
		groovyScriptService.validateScript(script);
		IdmRole role = new IdmRole();
		List<IdmRoleAuthority> authorities = new ArrayList<>();
		authorities.add(new IdmRoleAuthority());
		role.setAuthorities(authorities);
		role.setName(TEST_ONE);
		Object result = groovyScriptService.evaluate(script, ImmutableMap.of("entity", role, "list", authorities));
		assertEquals(role.getAuthorities(), result);
	}

	@Test(expected = IdmSecurityException.class)
	public void testSecurityScriptFile() {
		String script = "return new File();";
		groovyScriptService.validateScript(script);
		groovyScriptService.evaluate(script, null);
	}

}
