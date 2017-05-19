package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.repository.IdmConfigurationRepository;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultConfigurationService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

public class DefaultConfigurationServiceIntegrationTest extends AbstractIntegrationTest {
	
	private static final String TEST_PROPERTY_KEY = "test.property";
	private static final String TEST_PROPERTY_DB_KEY = "test.db.property";
	public static final String TEST_GUARDED_PROPERTY_KEY = "idm.sec.core.password.test";
	private static final String TEST_GUARDED_PROPERTY_VALUE = "secret_password";

	@Autowired
	private ApplicationContext context;
	@Autowired
	private IdmConfigurationRepository configurationRepository;
	//
	private ConfigurationService configurationService;
	
	@Before
	public void login() {
		super.loginAsAdmin(InitTestData.TEST_USER_1);
		configurationService = context.getAutowireCapableBeanFactory().createBean(DefaultConfigurationService.class);
	}
	
	@After
	public void logout() {
		super.logout();
		configurationRepository.deleteAll();
	}
	
	@Test
	public void testReadNotExists() {
		assertNull(configurationService.getValue("not_exists"));
	}
	
	@Test
	public void testReadNotExistsWithDefault() {
		assertEquals("true", configurationService.getValue("not_exists", "true"));
	}
	
	@Test
	public void testReadBooleanNotExistsWithDefault() {
		assertTrue(configurationService.getBooleanValue("not_exists", true));
	}
	
	@Test
	public void testReadPropertyFromFile() {
		assertEquals("true", configurationService.getValue(TEST_PROPERTY_KEY));
	}
	
	@Test
	public void testReadBooleanPropertyFromFile() {
		assertTrue(configurationService.getBooleanValue(TEST_PROPERTY_KEY));
	}
	
	@Test
	public void testReadPropertyFromDb() {
		configurationService.saveConfiguration(new IdmConfigurationDto(TEST_PROPERTY_DB_KEY, "true"));
		assertTrue(configurationService.getBooleanValue(TEST_PROPERTY_DB_KEY));
	}
	
	@Test
	public void testReadOverridenPropertyFromDb() {
		configurationService.saveConfiguration(new IdmConfigurationDto(TEST_PROPERTY_KEY, "false"));
		assertEquals("false", configurationService.getValue(TEST_PROPERTY_KEY));
	}
	
	@Test
	public void testReadGuardedPropertyFromFile() {
		assertEquals(TEST_GUARDED_PROPERTY_VALUE, configurationService.getValue(TEST_GUARDED_PROPERTY_KEY));
	}
	
	@Test
	public void testReadConfidentialPropertyFromDB() {
		configurationService.saveConfiguration(new IdmConfigurationDto(TEST_GUARDED_PROPERTY_KEY, "secured_change"));
		assertEquals("secured_change", configurationService.getValue(TEST_GUARDED_PROPERTY_KEY));
	}
}
