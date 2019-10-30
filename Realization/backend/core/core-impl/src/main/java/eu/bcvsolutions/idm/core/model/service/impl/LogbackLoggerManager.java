package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.google.common.collect.Maps;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import eu.bcvsolutions.idm.core.api.domain.comparator.CodeableComparator;
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
	// from logback.xml
	private Map<String, Level> fileLevels = new HashMap<>();
	// needed when configured level is deleted (mixed application.properties and logback.xml)
	private Map<String, Level> originalLevelCache = new HashMap<>();
	//
	@Autowired private ConfigurationService configurationService;
	
	@PostConstruct
	public void init() {
		LOG.info("Initialize logger levels from configuration");
		//
		loadFileLevels();
		//
		// init from database
		Map<String, String> initPackageLevels = Maps.newHashMap();
		configurationService
			.getConfigurations(PROPERTY_PREFIX)
			.values()
			.stream()
			.filter(configuration -> {
				return getPackageName(configuration.getName()) != null;
			})
			.filter(configuration -> {
				return StringUtils.isNotEmpty(configuration.getValue());
			})
			.forEach(configuration -> {
				initPackageLevels.put(getPackageName(configuration.getName()), configuration.getValue());
			});
		//
		// init from application files
		configurationService
			.getAllConfigurationsFromFiles()
			.stream()
			.filter(configuration -> {
				return getPackageName(configuration.getName()) != null;
			})
			.filter(configuration -> {
				return StringUtils.isNotEmpty(configuration.getValue());
			})
			.filter(configuration -> {
				return !initPackageLevels.containsKey(getPackageName(configuration.getName()));
			})
			.forEach(configuration -> {
				initPackageLevels.put(getPackageName(configuration.getName()), configuration.getValue());
			});
		//
		initPackageLevels.forEach((packageName, level) -> {
	        Level originalLevel = setLoggerLevel(packageName, Level.toLevel(level));
	        if (!originalLevelCache.containsKey(packageName)) {
	        	originalLevelCache.put(packageName, originalLevel);
	        }
		});
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
	        setLoggerLevel(packageName, actualLevel);
		}
		//
		return actualLevel == null ? null : org.slf4j.event.Level.valueOf(actualLevel.levelStr);
	}
	
	@Override
	public org.slf4j.event.Level getLevel(String packageName) {
		Level actualLevel = getLogger(packageName).getLevel();
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
	
	@Override
	public List<IdmConfigurationDto> getAllConfigurationsFromFiles() {
		return fileLevels
			.entrySet()
			.stream()
			.filter(entry -> {
				return entry.getValue() != null;
			})
			.map(entry -> {
				return new IdmConfigurationDto(String.format("%s%s", PROPERTY_PREFIX, entry.getKey()), entry.getValue().levelStr);
			})
			.sorted(new CodeableComparator())
			.collect(Collectors.toList());
	}
	
	protected void loadFileLevels() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.getLoggerList().forEach(logger -> {
			fileLevels.put(logger.getName(), logger.getLevel());
		});
	}
	
	/**
	 * Restore logger level from application configuration.
	 * 
	 * @param packageName required
	 * @return original level, which is restored. {@code null} is returned, when logger is not configured by file (property or logback.xml).
	 */
	protected Level restoreLevel(String packageName) {
		Assert.hasLength(packageName, "Package name is required.");
		//
		Level orignalLevel = originalLevelCache.get(packageName);
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
        Logger logger = getLogger(packageName);
        Level previousLevel = logger.getLevel();
        LOG.info("Setting logger level for package [{}] to [{}] (previous level [{}])", packageName, level, previousLevel);
        //
        logger.setLevel(level);
        // if lower level is set, then this message will be logged (=> is needed, but it looks redundantly :))
        LOG.info("Logger level for package [{}] is set to [{}] (previous level [{}])", packageName, level, previousLevel);
        //
        return previousLevel;
	}
	
	/**
	 * Returns logger (cannot be null).
	 * 
	 * @param packageName
	 * @return
	 */
	private Logger getLogger(String packageName) {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		//
		return loggerContext.getLogger(packageName);
	}
}
