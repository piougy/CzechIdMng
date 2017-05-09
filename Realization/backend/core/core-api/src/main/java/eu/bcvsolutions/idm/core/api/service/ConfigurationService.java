package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.ConfigurationDto;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Provides configuration through application
 * <p>
 * Conventions:
 * - all managed properties should start with {@value #IDM_PROPERTY_PREFIX} prefix
 * - all public properties should start with {@value #IDM_PUBLIC_PROPERTY_PREFIX} prefix. This properties are visible for everyone (public endpoits etc.)
 * - all private properties should start with {@value #IDM_PRIVATE_PROPERTY_PREFIX} prefix. This properties are visible by logged identity authority
 * - after prefix moduleId should be given - e.g. idm.pub.core.
 * 
 * @author Radek Tomiška 
 */
public interface ConfigurationService {
	
	static final String PROPERTY_SEPARATOR = ".";
	static final String SPLIT_PROPERTY_SEPARATOR = "\\.";
	static final String IDM_PROPERTY_PREFIX = "idm" + PROPERTY_SEPARATOR;
	static final String IDM_PUBLIC_PROPERTY_PREFIX = IDM_PROPERTY_PREFIX + "pub" + PROPERTY_SEPARATOR;
	static final String IDM_PRIVATE_PROPERTY_PREFIX = IDM_PROPERTY_PREFIX + "sec" + PROPERTY_SEPARATOR;
	//
	static final String PROPERTY_APP_INSTANCE_ID = IDM_PUBLIC_PROPERTY_PREFIX + "app.instanceId";
	static final String DEFAULT_PROPERTY_APP_INSTANCE_ID = "main";
	//
	// common properties
	static final String PROPERTY_ORDER = "order";
	static final String PROPERTY_ENABLED = "enabled";
	static final String PROPERTY_IMPLEMENTATION = "impl";
	//
	// common properties default
	static final int DEFAULT_ORDER = 0;
	static final boolean DEFAULT_ENABLED = true;
	
	/**
	 * Returns configured value for given key. If no value for given key is configured, then returns {@code null}.
	 * 
	 * @param key
	 * @return
	 */
	String getValue(String key);
	
	/**
	 * Set given configuration value. Creates new, if configuration with given key does not exist. 
	 * 
	 * @param key
	 * @param value
	 */
	void setValue(String key, String value);
	
	/**
	 * Deletes value by given key
	 * 
	 * @param key
	 * @return removed value or null, if no value was removed
	 */
	String deleteValue(String key);
	
	/**
	 * Set given configuration value. Creates new, if configuration with given key does not exist. 
	 */
	void saveConfiguration(ConfigurationDto configuration);
	
	/**
	 * Returns configured value for given key. If no value for given key is configured, then returns given defaultValue.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	String getValue(String key, String defaultValue);
	
	/**
	 * Returns configured value as {@code Boolean} for given key
	 * 
	 * @param key
	 * @return
	 */
	Boolean getBooleanValue(String key);
	
	/**
	 * Returns configured value as {@code boolean} for given key. Never throws exception - returns defaultValue.
	 * 
	 * @param key
	 * @return
	 */
	boolean getBooleanValue(String key, boolean defaultValue);
	
	/**
	 * Returns configured value as {@code Integer} for given key
	 * 
	 * @param key
	 * @return
	 * @throws NumberFormatException If the string cannot be parsed as a {@code int}.
	 */
	Integer getIntegerValue(String key);
	
	/**
	 * Returns configured value as {@code Integer} for given key. Never throws number format exception - returns defaultValue on exception.
	 * 
	 * @param key
	 * @return
	 */
	Integer getIntegerValue(String key, Integer defaultValue);
	
	/**
	 * Returns configured value as {@code Long} for given key.
	 * 
	 * @param key
	 * @return
	 * @throws NumberFormatException If the string cannot be parsed as a {@code long}.
	 */
	Long getLongValue(String key);
	
	/**
	 * Returns configured value as {@code Long} for given key. Never throws number format exception - returns defaultValue on exception.
	 * 
	 * @param key
	 * @return
	 */
	Long getLongValue(String key, Long defaultValue);
	
	/**
	 * Set given configuration value. Creates new, if configuration with given key does not exist. 
	 * 
	 * @param key
	 * @param value
	 */
	void setBooleanValue(String key, boolean value);
	
	/**
	 * Returns all public configuration properties 
	 * 
	 * @return
	 */
	List<ConfigurationDto> getAllPublicConfigurations();
	
	/**
	 * Returns all configuration properties from property files
	 * 
	 * @return
	 */
	List<ConfigurationDto> getAllConfigurationsFromFiles();
	
	/**
	 * Returns server environment properties
	 * 
	 * @return
	 */
	List<ConfigurationDto> getAllConfigurationsFromEnvironment();
	
	/**
	 * Returns configured value as {@code GuardedString} for given key.
	 * 
	 * @param key
	 * @return
	 */
	GuardedString getGuardedValue(String key);
	
	/**
	 * Returns configured value as {@code GuardedString} for given key. If no value for given key is configured, then returns given defaultValue as {@code GuardedString}.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	GuardedString getGuardedValue(String key, String defaultValue);
	
	/**
	 * Returns server instance id
	 * 
	 * @return
	 */
	String getInstanceId();
	
	/**
	 * Returns configurations by given keyPrefix. Map key is property name without prefix
	 * 
	 * @param keyPrefix
	 * @return
	 */
	Map<String, ConfigurationDto> getConfigurations(String keyPrefix);
}
