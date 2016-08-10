package eu.bcvsolutions.idm.core.model.service;

/**
 * Provides configuration through application
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public interface ConfigurationService {

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
	 * Return email configuration
	 * 
	 * @return
	 */
	Object getEmailerConfiguration();

}
