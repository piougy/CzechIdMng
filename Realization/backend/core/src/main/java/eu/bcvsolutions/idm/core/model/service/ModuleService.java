package eu.bcvsolutions.idm.core.model.service;

import java.util.List;

import org.springframework.plugin.core.Plugin;
import org.springframework.plugin.core.PluginRegistry;

import eu.bcvsolutions.idm.core.model.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.security.domain.GroupPermission;

/**
 * Application modules administrative. Plugin can be register by {@link ModuleDescriptor}.
 * 
 * @author Radek Tomiška
 *
 * @see ModuleDescriptor
 * @see Plugin
 * @see PluginRegistry
 */
public interface ModuleService {
	
	/**
	 * Returns all registered modules in this application (enabled and disabled too).
	 * 
	 * @return
	 */
	List<ModuleDescriptor> getInstalledModules();
	
	/**
	 * Returns module descriptor by given moduleId.
	 * 
	 * @param moduleId
	 * @return
	 */
	ModuleDescriptor getModule(String moduleId);

	/**
	 * Returns all enabled modules in this application.
	 * 
	 * @return
	 */
	List<ModuleDescriptor> getEnabledModules();

	/**
	 * Returns {@code true}, if module is enabled. If module is disabled or is not installed, then returns {@code false};
	 * 
	 * @param moduleId
	 * @return
	 */
	boolean isEnabled(String moduleId);
	
	/**
	 * Returns {@code true}, if module is enabled. If module is disabled or is not installed, then returns {@code false};
	 * 
	 * @param moduleDescriptor
	 * @return
	 */
	boolean isEnabled(ModuleDescriptor moduleDescriptor);

	/**
	 * Enable given module. Throws {@link IllegalArgumentException} when moduleId is not installed.
	 * 
	 * @param moduleId
	 */
	void enable(String moduleId);

	/**
	 * Disable given module. Throws {@link IllegalArgumentException} when moduleId is not installed.
	 * 
	 * @param moduleId
	 */
	void disable(String moduleId);

	/**
	 * Enable / disable given module. Throws {@link IllegalArgumentException} when moduleId is not installed.
	 * 
	 * @param moduleId
	 * @param enabled
	 */
	void setEnabled(String moduleId, boolean enabled);
	
	/**
	 * Returns all permissions from all enabled modules.  
	 * 
	 * @return
	 */
	List<GroupPermission> getAvailablePermissions();

}
