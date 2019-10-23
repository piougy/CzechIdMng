package eu.bcvsolutions.idm.core.model.service.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
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
	
	@Autowired private ApplicationContext context;
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
}
