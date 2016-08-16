package eu.bcvsolutions.idm.configuration.service;

import java.util.List;

import eu.bcvsolutions.idm.configuration.dto.ConfigurationDto;

/**
 * Provides configuration through application
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public interface ConfigurationService {
	
	static final String PROPERTY_SEPARATOR = ".";
	static final String SPLIT_PROPERTY_SEPARATOR = "\\.";
	static final String IDM_PROPERTY_PREFIX = "idm" + PROPERTY_SEPARATOR;
	static final String IDM_PUBLIC_PROPERTY_PREFIX = IDM_PROPERTY_PREFIX + "pub" + PROPERTY_SEPARATOR;
	static final String IDM_PRIVATE_PROPERTY_PREFIX = IDM_PROPERTY_PREFIX + "sec" + PROPERTY_SEPARATOR;

	/**
	 * Returns configured value for given key. If no value for given key is configered, then returns {@code null}.
	 * 
	 * @param key
	 * @return
	 */
	String getValue(String key);
	
	/**
	 * Returns configured value for given key. If no value for given key is configered, then returns given defaultValue.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	String getValue(String key, String defaultValue);
	
	/**
	 * Returns configured value as boolean for given key
	 * 
	 * @param key
	 * @return
	 */
	Boolean getBooleanValue(String key);
	
	/**
	 * Returns configured value as boolean for given key
	 * 
	 * @param key
	 * @return
	 */
	boolean getBooleanValue(String key, boolean defaultValue);
	
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
}
