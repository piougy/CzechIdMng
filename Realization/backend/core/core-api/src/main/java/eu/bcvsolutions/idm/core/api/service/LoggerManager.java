package eu.bcvsolutions.idm.core.api.service;

import java.util.List;

import org.slf4j.event.Level;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;

/**
 * Configure logger programmatically.
 * 
 * @author Radek Tomiška
 * @since 10.0.0
 */
public interface LoggerManager {
	
	/**
	 * Configured logger levels are persisted into IdM configuration (all configuration in one place).
     * All properties for logger are persisted under prefix {@link #PROPERTY_PREFIX}. Package name is defined after prefix and value is level.
	 */
	String PROPERTY_PREFIX = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.logger.";
	
	/**
	 * Set logger level for given package.
	 * 
	 * @param packageName
	 * @param level set logger level if configuration value is filled. Restore logger level, when configuration value is empty.
	 * @return actual set level
	 * @throws IllegalArgumentException if configuration property name doesn't start with {@link #PROPERTY_PREFIX}.
	 */
	Level setLevel(IdmConfigurationDto configuration);
	
	/**
	 * Set logger level for given package.
	 * 
	 * @param packageName required
	 * @param level set logger level if value is given. Restore logger level, when value is empty (or {@code null}).
	 * @return actual set level
	 */
	Level setLevel(String packageName, String level);
	
	
	/**
	 * Set logger level for given package.
	 * 
	 * @param packageName required
	 * @param level set logger level if value is given. Restore logger level, when value is {@code null}.
	 * @return actual set level
	 */
	Level setLevel(String packageName, Level level);
	
	/**
	 * Returns current logger level for given packagen name.
	 * 
	 * @param packageName
	 * @return
	 */
	Level getLevel(String packageName);
	
	/**
	 * Returns package name from configuration property name with logger prefix ({@link #PROPERTY_PREFIX}).
	 * If given configuration property name doesn't start with logger prefix ({@link #PROPERTY_PREFIX}), then {@code null} is returned.
	 * 
	 * @param configurationPropertyName
	 * @return
	 */
	String getPackageName(String configurationPropertyName);
	
	/**
	 * Configured levels in logger configuration on file system.
	 * 
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met.
	 */
	List<IdmConfigurationDto> getAllConfigurationsFromFiles();
}
