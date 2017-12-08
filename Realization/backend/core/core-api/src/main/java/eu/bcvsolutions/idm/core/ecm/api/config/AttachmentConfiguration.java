package eu.bcvsolutions.idm.core.ecm.api.config;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Configuration for attachments
 * 
 * @author Radek Tomiška
 */	
public interface AttachmentConfiguration extends Configurable {
	
	/**
	 * Path to attachments storage
	 * - System.getProperty("user.home")/idm_data will be used if no path is given
	 */
	static final String PROPERTY_STORAGE_PATH = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.attachment.storagePath";
	
	/**
	 * Path, where will be stored temporary files
	 * - PROPERTY_STORAGE_PATH/idm_data/temp will be used if no path is given
	 */
	static final String PROPERTY_TEMP_PATH = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.attachment.tempPath";
	
	/**
	 * Temporary file time to live in milliseconds.
	 * Older temporary files will be purged
	 */
	static final String PROPERTY_TEMP_TTL = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.attachment.tempTtl";
	static final long DEFAULT_TEMP_TTL = 1209600000L; // default 14 days
	
	@Override
	default String getConfigurableType() {
		return "attachment";
	}
	
	@Override
	default boolean isDisableable() {
		return false;
	}
	
	@Override
	default public boolean isSecured() {
		return true;
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sense here
		properties.add(getPropertyName(PROPERTY_STORAGE_PATH));
		properties.add(getPropertyName(PROPERTY_TEMP_PATH));
		properties.add(getPropertyName(PROPERTY_TEMP_TTL));
		return properties;
	}
	
	/**
	 * Path to attachments storage or url path, etc.
	 * - System.getProperty("user.home")/idm_data will be used if no path is configured
	 * 
	 * @return
	 */
	String getStoragePath();
	
	/**
	 * Path, where will be created temporal files (when attachment is uploaded, transormed, etc.)
	 * - getStoragePath()/temp will be used if no path is configured
	 * 
	 * @return
	 */
	String getTempPath();
	
	/**
	 * Temporary file time to live in milliseconds.
	 * Older temporary files will be purged
	 * 
	 * @return time to live in milliseconds
	 */
	long getTempTtl();
	
	/**
	 * Sets temporary file time to live in milliseconds.
	 * 
	 * @param ttl
	 */
	void setTempTtl(long ttl);
	
}
