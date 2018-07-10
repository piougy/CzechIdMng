package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Application configuration tests
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultConfigurationServiceIntegrationTest extends AbstractIntegrationTest {
	
	private static final String TEST_PROPERTY_KEY = "test.property";
	private static final String TEST_PROPERTY_DB_KEY = "test.db.property";
	public static final String TEST_GUARDED_PROPERTY_KEY = "idm.sec.core.password.test";
	private static final String TEST_GUARDED_PROPERTY_VALUE = "secret_password";
	//
	@Autowired private ApplicationContext context;
	//
	private ConfigurationService configurationService;
	
	@Before
	public void login() {
		super.loginAsAdmin();
		configurationService = context.getAutowireCapableBeanFactory().createBean(DefaultConfigurationService.class);
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	@Transactional
	public void testReadNotExists() {
		assertNull(configurationService.getValue("not_exists"));
	}
	
	@Test
	@Transactional
	public void testReadNotExistsWithDefault() {
		assertEquals("true", configurationService.getValue("not_exists", "true"));
	}
	
	@Test
	@Transactional
	public void testReadBooleanNotExistsWithDefault() {
		assertTrue(configurationService.getBooleanValue("not_exists", true));
	}
	
	@Test
	@Transactional
	public void testReadPropertyFromFile() {
		assertEquals("true", configurationService.getValue(TEST_PROPERTY_KEY));
	}
	
	@Test
	@Transactional
	public void testReadBooleanPropertyFromFile() {
		assertTrue(configurationService.getBooleanValue(TEST_PROPERTY_KEY));
	}
	
	@Test
	@Transactional
	public void testReadPropertyFromDb() {
		configurationService.saveConfiguration(new IdmConfigurationDto(TEST_PROPERTY_DB_KEY, "true"));
		assertTrue(configurationService.getBooleanValue(TEST_PROPERTY_DB_KEY));
	}
	
	@Test
	@Transactional
	public void testReadOverridenPropertyFromDb() {
		configurationService.saveConfiguration(new IdmConfigurationDto(TEST_PROPERTY_KEY, "false"));
		assertEquals("false", configurationService.getValue(TEST_PROPERTY_KEY));
	}
	
	@Test
	@Transactional
	public void testReadGuardedPropertyFromFile() {
		assertEquals(TEST_GUARDED_PROPERTY_VALUE, configurationService.getValue(TEST_GUARDED_PROPERTY_KEY));
	}
	
	@Test
	@Transactional
	public void testReadConfidentialPropertyFromDB() {
		configurationService.saveConfiguration(new IdmConfigurationDto(TEST_GUARDED_PROPERTY_KEY, "secured_change"));
		assertEquals("secured_change", configurationService.getValue(TEST_GUARDED_PROPERTY_KEY));
	}
	
	@Test
	@Transactional
	public void testGlobalDateFormatChange() {
		final String format = "dd.MM";
		configurationService.setValue(ConfigurationService.PROPERTY_APP_DATE_FORMAT, format);
		assertEquals(format, configurationService.getDateFormat());
		configurationService.setValue(ConfigurationService.PROPERTY_APP_DATE_FORMAT, ConfigurationService.DEFAULT_APP_DATE_FORMAT);
		assertEquals(ConfigurationService.DEFAULT_APP_DATE_FORMAT, configurationService.getDateFormat());
	}
	
	@Test
	@Transactional
	public void testDefaultDateTimeFormat() {
		assertEquals(ConfigurationService.DEFAULT_APP_DATETIME_FORMAT, configurationService.getDateTimeFormat());
	}
}
