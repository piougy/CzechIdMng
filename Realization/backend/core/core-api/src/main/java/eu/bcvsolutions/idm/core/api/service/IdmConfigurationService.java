package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Provides configuration through application
 * 
 * @author Radek Tomiška 
 * 
 * @see ConfigurationService
 *
 */
public interface IdmConfigurationService extends 
		ReadWriteDtoService<IdmConfigurationDto, DataFilter>, 
		AuthorizableService<IdmConfigurationDto>,
		CodeableService<IdmConfigurationDto>, 
		ConfigurationService {
	
	String CONFIDENTIAL_PROPERTY_VALUE = "config:value";	
}
