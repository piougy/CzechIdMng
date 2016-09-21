package eu.bcvsolutions.idm.core.model.domain;

import java.util.List;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.security.domain.GroupPermission;

/**
 * One module will contain one module descriptor
 * 
 * @author Radek Tomiška
 *
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
	 * Module permissions
	 * 
	 * @return
	 */
	List<GroupPermission> getPermissions();

}
