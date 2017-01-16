package eu.bcvsolutions.idm.core.api.domain;

import java.util.Collections;
import java.util.List;

import eu.bcvsolutions.idm.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.security.api.domain.GroupPermission;

/**
 * Add default methods implementaton for {@link ModuleDescriptor}.
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractModuleDescriptor implements ModuleDescriptor {

	@Override
	public boolean supports(String delimiter) {
		return getId().equals(delimiter);
	}

	/**
	 * Returns module version from pom project
	 */
	@Override
	public String getName() {
		return getClass().getPackage().getImplementationTitle();
	}

	/**
	 * Returns module version from pom project
	 */
	@Override
	public String getVersion() {
		return getClass().getPackage().getImplementationVersion();
	}
	
	/**
	 * Returns module vendor from pom project
	 */
	@Override
	public String getVendor() {
		return getClass().getPackage().getImplementationVendor();
	}
	
	/**
	 * Returns description from pom project
	 */
	@Override
	public String getDescription() {
		return getClass().getPackage().getSpecificationTitle();
	}

	/**
	 * Returns empty permissions
	 */
	@Override
	public List<GroupPermission> getPermissions() {
		return Collections.emptyList();
	}
	
	@Override
	public boolean isDisableable() {
		return true;
	}
	
	/**
	 * Returns empty notifications list
	 */
	public List<NotificationConfigurationDto> getDefaultNotificationConfigurations() {
		return Collections.emptyList();
	}
	
}
