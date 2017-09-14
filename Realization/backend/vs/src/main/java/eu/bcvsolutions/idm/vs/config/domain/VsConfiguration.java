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

	static final String PROPERTY_DEFAULT_ROLE = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "vs.role.default";
	static final String DEFAULT_DEFAULT_ROLE = "superAdminRole";
	
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

	IdmRoleDto getDefaultRole();
	
}
