package eu.bcvsolutions.idm.vs.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Virtual system configuration - interface
 * 
 * @author Svanda
 *
 */
public interface VsConfiguration extends Configurable {

	String PROPERTY_DEFAULT_ROLE = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "vs.role.default";
	String DEFAULT_ROLE = "superAdminRole";
	/**
	 * Implementer  role - product provided role for implementers (approve vs request etc.).
	 * 
	 * @since 10.5.0
	 */
	String PROPERTY_IMPLEMENTER_ROLE = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "vs.role.implementer";
	String DEFAULT_IMPLEMENTER_ROLE = "virtualSystemImplementerRole";
	
	@Override
	default String getConfigurableType() {
		return "vs";
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>();
		properties.add(getPropertyName(PROPERTY_DEFAULT_ROLE));
		return properties;
	}

	/**
	 * If you do not define none directly implementers and none role in VS configuration,
	 * then will be used implementers from default role.
	 * 
	 * @return
	 */
	IdmRoleDto getDefaultRole();
	
	
	/**
	 * Implementer role code from configuration.
	 * 
	 * @return full role code
	 * @since 10.5.0
	 */
	String getImplementerRoleCode();
	
	/**
	 * Implementer  role - product provided role for implementers (approve vs request etc.).
	 * 
	 * @return configured role
	 * @since 10.5.0
	 */
	IdmRoleDto getImplementerRole();
}
