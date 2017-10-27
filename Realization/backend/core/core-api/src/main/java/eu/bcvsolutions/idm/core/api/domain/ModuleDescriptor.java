package eu.bcvsolutions.idm.core.api.domain;

import java.util.List;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationConfigurationDto;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;

/**
 * One module will contain one module descriptor
 * 
 * @author Radek Tomiška
 */
public interface ModuleDescriptor extends Plugin<String> {
	
	/**
	 * Unique module id - short name 
	 * 
	 * @return
	 */
	String getId();
	
	/**
	 * User friendly module name
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * Textual module description
	 * 
	 * @return
	 */
	String getDescription();
	
	/**
	 * Module version
	 * 
	 * @return
	 */
	String getVersion();
	
	/**
	 * Module vendor
	 * 
	 * @return
	 */
	String getVendor();
	
	/**
	 * Module vendor's url
	 * 
	 * @return
	 */
	String getVendorUrl();
	
	/**
	 * Module vendor's url
	 * 
	 * @return
	 */
	String getVendorEmail();
	
	/**
	 * Revision from scm
	 * 
	 * @return
	 */
	String getBuildNumber();

	/**
	 * Build timestamp
	 * 
	 * @return
	 */
	String getBuildTimestamp();
	
	/**
	 * Module permissions
	 * 
	 * @return
	 */
	List<GroupPermission> getPermissions();
	
	/**
	 * Returns true, if module can be disabled if is installed
	 * 
	 * @return
	 */
	boolean isDisableable();
	
	/**
	 * Returns default module notification configuration
	 * 
	 * @return
	 */
	List<IdmNotificationConfigurationDto> getDefaultNotificationConfigurations();
	
	/**
	 * Returns true, when html documentation is available as webjar directly in module .jar package
	 * Url is constructed by convention /webjars/<module>/<version>/doc/index.html.	
	 * 
	 * @return
	 */
	boolean isDocumentationAvailable();

}
