package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Provides configuration through application
 * <p>
 * Conventions:
 * - all managed properties should start with {@value #IDM_PROPERTY_PREFIX} prefix
 * - all public properties should start with {@value #IDM_PUBLIC_PROPERTY_PREFIX} prefix. This properties are visible for everyone (public endpoits etc.)
 * - all private properties should start with {@value #IDM_PRIVATE_PROPERTY_PREFIX} prefix. This properties are visible by logged identity authority
 * - after prefix moduleId should be given - e.g. idm.pub.core.
 * - Separator {@value #PROPERTY_MULTIVALUED_SEPARATOR} is supported for multi values properites.
 * - Multivalue properties are trimmed automatically - e.g. is posible to use 'key=value1, value2'
 * 
 * @author Radek Tomiška 
 */
public interface ConfigurationService {
	
	String PROPERTY_SEPARATOR = ".";
	String SPLIT_PROPERTY_SEPARATOR = "\\.";
	String IDM_PROPERTY_PREFIX = "idm" + PROPERTY_SEPARATOR;
	String IDM_PUBLIC_PROPERTY_PREFIX = IDM_PROPERTY_PREFIX + "pub" + PROPERTY_SEPARATOR;
	String IDM_PRIVATE_PROPERTY_PREFIX = IDM_PROPERTY_PREFIX + "sec" + PROPERTY_SEPARATOR;
	//
	// instance id - backend server identifier
	String PROPERTY_APP_INSTANCE_ID = IDM_PUBLIC_PROPERTY_PREFIX + "app.instanceId";
	String DEFAULT_APP_INSTANCE_ID = "main";
	//
	// date format
	String PROPERTY_APP_DATE_FORMAT = IDM_PUBLIC_PROPERTY_PREFIX + "app.format.date";
	String DEFAULT_APP_DATE_FORMAT = "dd.MM.yyyy";
	//
	// datetime format
	String PROPERTY_APP_DATETIME_FORMAT = IDM_PUBLIC_PROPERTY_PREFIX + "app.format.datetime";
	String DEFAULT_APP_DATETIME_FORMAT = "dd.MM.yyyy HH:mm";
	// datetime with seconds format
	String PROPERTY_APP_DATETIME_WITH_SECONDS_FORMAT = IDM_PUBLIC_PROPERTY_PREFIX + "app.format.datetimeseconds";
	String DEFAULT_APP_DATETIME_WITH_SECONDS_FORMAT = "dd.MM.yyyy HH:mm:ss";
	//
	// common properties
	String PROPERTY_ORDER = "order";
	String PROPERTY_ENABLED = "enabled";
	String PROPERTY_IMPLEMENTATION = "impl";
	//
	// common properties default
	int DEFAULT_ORDER = 0;
	boolean DEFAULT_ENABLED = true;
	String PROPERTY_MULTIVALUED_SEPARATOR = ","; // multi value default separator

	/**
	 * Returns configured value for given key. If no value for given key is configured, then returns {@code null}.
	 * 
	 * @param key
	 * @return
	 */
	String getValue(String key);
	
	/**
	 * Return multi values property. 
	 * Default separator is supported {@link #PROPERTY_MULTIVALUED_SEPARATOR} only.
	 * Values are trimmed automatically - e.g. key=value1, value2
	 *  
	 * @since 7.6.0
	 * @param key
	 * @return
	 */
	List<String> getValues(String key);
	
	/**
	 * Set given configuration value. Creates new, if configuration with given key does not exist. 
	 * 
	 * @param key
	 * @param value
	 */
	void setValue(String key, String value);
	
	/**
	 * Sets multi values property.
	 * Default separator is supported {@link ConfigurationService#PROPERTY_MULTIVALUED_SEPARATOR} only.
	 * 
	 * @since 7.6.0
	 * @param key
	 * @param values
	 */
	void setValues(String key, List<String> values);
	
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
	void saveConfiguration(IdmConfigurationDto configuration);
	
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
	List<IdmConfigurationDto> getAllPublicConfigurations();
	
	/**
	 * Returns all configuration properties from property files
	 * 
	 * @return
	 */
	List<IdmConfigurationDto> getAllConfigurationsFromFiles();
	
	/**
	 * Returns server environment properties
	 * 
	 * @return
	 */
	List<IdmConfigurationDto> getAllConfigurationsFromEnvironment();
	
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
	Map<String, IdmConfigurationDto> getConfigurations(String keyPrefix);
	
	/**
	 * Returns url to frontend (by configuration)
	 * 
	 * @param path
	 * @return
	 */
	String getFrontendUrl(String path);
	
	/**
	 * Returns global date format on BE. Used in notification templates, logs, etc.
	 * 
	 * @return
	 */
	String getDateFormat();
	
	/**
	 * Returns global datetime format on BE. Used in notification templates, logs, etc.
	 * 
	 * @return
	 */
	String getDateTimeFormat();
	
	/**
	 * Returns global datetime (with seconds) format on BE. Used in notification templates, logs, etc.
	 * 
	 * @return
	 */
	String getDateTimeSecondsFormat();
}
