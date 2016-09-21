package eu.bcvsolutions.idm.core.model.domain;

import java.util.Collections;
import java.util.List;

import eu.bcvsolutions.idm.security.domain.GroupPermission;

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

	@Override
	public String getName() {
		return getClass().getName();
	}

	@Override
	public String getDescription() {
		return null;
	}

	/**
	 * Returns module version from pom project
	 */
	@Override
	public String getVersion() {
		return getClass().getPackage().getImplementationVersion();
	}

	/**
	 * Return empty permissions
	 */
	@Override
	public List<GroupPermission> getPermissions() {
		return Collections.emptyList();
	}
	
	@Override
	public boolean isDisableable() {
		return true;
	}
	
}
