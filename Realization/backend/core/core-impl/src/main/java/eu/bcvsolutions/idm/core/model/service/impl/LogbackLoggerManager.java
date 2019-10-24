package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.LoggerManager;
import eu.bcvsolutions.idm.core.model.event.processor.configuration.ConfigurationDeleteLoggerProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.configuration.ConfigurationSaveLoggerProcessor;

/**
 * Default implementation for logger manager. 
 * Logback library is used as default slf4j implementation.
 * Configured logger levels are persisted into IdM configuration (all configuration in one place) by registered entity event processors.
 * All properties for logger are persisted under prefix {@link #PROPERTY_PREFIX}. Package name is defined after prefix and value is level.
 * Persisted configured logger levels are loaded, after application starts and override configuration from logback.xml file.
 * 
 * @see ConfigurationDeleteLoggerProcessor
 * @see ConfigurationSaveLoggerProcessor
 * 
 * @author Radek Tomiška
 * @since 10.0.0
 */
public class LogbackLoggerManager implements LoggerManager {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LogbackLoggerManager.class);
	private Map<String, Level> originalLevelCache = new HashMap<>(); // needed when configured level is deleted
	//
	@Autowired private ConfigurationService configurationService;
	
	@PostConstruct
	public void init() {
		LOG.info("Initialize logger levels from configuration");
		//
		configurationService
			.getConfigurations(PROPERTY_PREFIX)
			.values()
			.stream()
			.filter(configuration -> {
				return StringUtils.isNotEmpty(configuration.getValue());
			})
			.forEach(this::setLevel);
	}
	
	@Override
	public org.slf4j.event.Level setLevel(IdmConfigurationDto configuration) {
		Assert.notNull(configuration, "Configuration is required");
		//
		String packageName = getPackageName(configuration.getName());
		Assert.hasLength(packageName, "Logger configuration is required. Has to start with logger prefix.");
		//
		String level = configuration.getValue();
		return setLevel(packageName, level);
	}
	
	@Override
	public org.slf4j.event.Level setLevel(String packageName, String level) {
		Assert.hasLength(packageName, "Package name is required.");
		//
		return setLevel(packageName, StringUtils.isEmpty(level) ? null : org.slf4j.event.Level.valueOf(level.toUpperCase()));
	}

	@Override
	public org.slf4j.event.Level setLevel(String packageName, org.slf4j.event.Level level) {
		Assert.hasLength(packageName, "Package name is required.");
		LOG.trace("Setting logger level [{}] for package [{}].", level, packageName);
		//
		Level actualLevel = null;
		if (level == null) {
			// configuration is empty => restore
			actualLevel = restoreLevel(packageName);
		} else {
	        //
			actualLevel = Level.toLevel(level.name());
	        Level originalLevel = setLoggerLevel(packageName, actualLevel);
	        if (!originalLevelCache.containsKey(packageName)) {
	        	originalLevelCache.put(packageName, originalLevel);
	        }
		}
		//
		return actualLevel == null ? null : org.slf4j.event.Level.valueOf(actualLevel.levelStr);
	}
	
	@Override
	public String getPackageName(String configurationProperty) {
		Assert.notNull(configurationProperty, "Configuration property s required.");
		//
		if (!configurationProperty.startsWith(PROPERTY_PREFIX)) {
			return null;
		}
		return configurationProperty.replaceFirst(PROPERTY_PREFIX, "");
	}
	
	/**
	 * Restore logger level from application configuration.
	 * 
	 * @param packageName required
	 * @return original level, which is restored. {@code null} is returned, when nothing is changed (no persisted configuration exists).
	 */
	protected Level restoreLevel(String packageName) {
		Assert.hasLength(packageName, "Package name is required.");
		//
		if (!originalLevelCache.containsKey(packageName)) {
			LOG.warn("Logger level for package [{}] was not configured, nothing to restore.", packageName);
			//
        	return null;
        }
		Level orignalLevel = originalLevelCache.remove(packageName);
		setLoggerLevel(packageName, orignalLevel);
		//
		return orignalLevel;
	}
	
	/**
	 * Set level into logger context.
	 * 
	 * @param packageName
	 * @param level
	 * @return previous level configuration
	 */
	protected Level setLoggerLevel(String packageName, Level level) {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger(packageName);
        Level previousLevel = logger.getLevel();
        LOG.info("Setting logger level for package [{}] to [{}] (previous level [{}])", packageName, level, previousLevel);
        //
        logger.setLevel(level);
        // if lower level is set, then this message will be logged (=> is needed, but it looks redundantly :))
        LOG.info("Logger level for package [{}] is set to [{}] (previous level [{}])", packageName, level, previousLevel);
        //
        return previousLevel;
	}
}
