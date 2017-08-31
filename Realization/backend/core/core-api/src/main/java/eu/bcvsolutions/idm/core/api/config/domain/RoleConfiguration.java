package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Configuration for roles
 * 
 * @author Radek Tomiška
 *
 */
public interface RoleConfiguration  extends Configurable {
	
	static final String PROPERTY_DEFAULT_ROLE = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.role.default";
	static final String DEFAULT_DEFAULT_ROLE = "userRole";
	
	static final String PROPERTY_ADMIN_ROLE = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.role.admin";
	static final String DEFAULT_ADMIN_ROLE = "superAdminRole";
	
	@Override
	default String getConfigurableType() {
		return "role";
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
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sence here
		properties.add(getPropertyName(PROPERTY_DEFAULT_ROLE));
		properties.add(getPropertyName(PROPERTY_ADMIN_ROLE));
		return properties;
	}
	
	/**
	 * Returns default role identifier
	 * 
	 * @return
	 */
	UUID getDefaultRoleId();
	
	/**
	 * Returns default role
	 * 
	 * @return
	 */
	IdmRoleDto getDefaultRole();
	
	/**
	 * Returns admin role identifier
	 * 
	 * @return
	 */
	UUID getAdminRoleId();
	
	/**
	 * Returns admin role
	 * 
	 * @return
	 */
	IdmRoleDto getAdminRole();
}
