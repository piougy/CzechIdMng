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
	
	static final String IDM_PROPERTY_PREFIX = "idm.";
	static final String IDM_PUBLIC_PROPERTY_PREFIX = IDM_PROPERTY_PREFIX + "pub.";
	static final String IDM_PRIVATE_PROPERTY_PREFIX = IDM_PROPERTY_PREFIX + "sec.";

	/**
	 * Returns configured value for given key
	 * 
	 * @param key
	 * @return
	 */
	String getValue(String key);
	
	/**
	 * Returns configured value as boolean for given key
	 * 
	 * @param key
	 * @return
	 */
	boolean getBoolean(String key);
	
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
