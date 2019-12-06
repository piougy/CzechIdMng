package eu.bcvsolutions.idm.core.model.service.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.LoggerManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Logback logger manager tests - change and restore logger levels.
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class LogbackLoggerManagerIntegrationTest extends AbstractIntegrationTest {
	
	public final static String TEST_PACKAGE_FROM_PROPERTIES = "eu.bcvsolutions.test.mock.package";
	//
	@Autowired private ApplicationContext context;
	@Autowired private ConfigurationService configurationService;
	//
	private LogbackLoggerManager manager;

	@Before
	public void init() {
		manager = context.getAutowireCapableBeanFactory().createBean(LogbackLoggerManager.class);
	}
	
	@Test
	public void testChangeLevel() {
		IdmConfigurationDto configuration = new IdmConfigurationDto();
		configuration.setName(String.format("%s%s", LoggerManager.PROPERTY_PREFIX, "mock"));
		configuration.setValue("debug");
		//
		Level level = manager.setLevel(configuration);
		//
		Assert.assertEquals(Level.DEBUG, level);
		//
		configuration.setValue(null);
		level = manager.setLevel(configuration);
		//
		Assert.assertNull(level);
	}
	
	@Test
	public void testRestoreLevelWithoutConfiguration() {
		IdmConfigurationDto configuration = new IdmConfigurationDto();
		configuration.setName(String.format("%s%s", LoggerManager.PROPERTY_PREFIX, "mock"));
		configuration.setValue(null);
		//
		Level level = manager.setLevel(configuration);
		//
		Assert.assertNull(level);
	}
	
	@Test
	public void testPackageName() {
		Assert.assertNull(manager.getPackageName("mock"));
		Assert.assertEquals("mock", manager.getPackageName(String.format("%s%s", LoggerManager.PROPERTY_PREFIX, "mock")));
	}
	
	@Test
	public void testChangeLevelByConfigurationEvent() {
		IdmConfigurationDto configuration = new IdmConfigurationDto();
		configuration.setName(String.format("%s%s", LoggerManager.PROPERTY_PREFIX, "mock"));
		configuration.setValue("debug");
		//
		Assert.assertNull(manager.getLevel(manager.getPackageName(configuration.getName())));
		//
		configurationService.saveConfiguration(configuration);
		//
		Assert.assertEquals(Level.DEBUG, manager.getLevel(manager.getPackageName(configuration.getName())));
		//
		configurationService.deleteValue(configuration.getCode());
		//
		Assert.assertNull(manager.getLevel(manager.getPackageName(configuration.getName())));
	}
	
	@Test
	public void testInitConfigurationFromPropertyFile() {
		Assert.assertEquals(Level.ERROR, manager.getLevel(TEST_PACKAGE_FROM_PROPERTIES));
		//
		Assert.assertEquals(Level.DEBUG, manager.setLevel(TEST_PACKAGE_FROM_PROPERTIES, "deBug"));
		//
		Assert.assertEquals(Level.DEBUG, manager.getLevel(TEST_PACKAGE_FROM_PROPERTIES));
		//
		Assert.assertEquals(Level.ERROR, manager.setLevel(TEST_PACKAGE_FROM_PROPERTIES, (String) null));
		//
		Assert.assertEquals(Level.ERROR, manager.getLevel(TEST_PACKAGE_FROM_PROPERTIES));
	}
}
