package eu.bcvsolutions.idm.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.configuration.entity.IdmConfiguration;
import eu.bcvsolutions.idm.configuration.repository.IdmConfigurationRepository;
import eu.bcvsolutions.idm.configuration.service.ConfigurationService;
import eu.bcvsolutions.idm.core.AbstractIntegrationTest;
import eu.bcvsolutions.idm.core.TestUtils;

public class DefaultConfigurationServiceTest extends AbstractIntegrationTest {
	
	private static final String TEST_PROPERTY_KEY = "test.property";
	private static final String TEST_PROPERTY_DB_KEY = "test.db.property";

	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	private IdmConfigurationRepository configurationRepository;
	
	@Before
	public void login() {
		super.loginAsAdmin(TestUtils.TEST_USER_1);
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testReadNotExists() {
		assertNull(configurationService.getValue("not_exists"));
	}
	
	@Test
	public void testReadPropertyFromFile() {
		assertEquals("true", configurationService.getValue(TEST_PROPERTY_KEY));
	}
	
	@Test
	public void testReadBooleanPropertyFromFile() {
		assertTrue(configurationService.getBoolean(TEST_PROPERTY_KEY));
	}
	
	@Test
	public void testReadPropertyFromDb() {
		configurationRepository.save(new IdmConfiguration(TEST_PROPERTY_DB_KEY, "true"));
		assertTrue(configurationService.getBoolean(TEST_PROPERTY_DB_KEY));
	}
	
	@Test
	public void testReadOverridenPropertyFromDb() {
		configurationRepository.save(new IdmConfiguration(TEST_PROPERTY_KEY, "false"));
		assertEquals("false", configurationService.getValue(TEST_PROPERTY_KEY));
	}
}
