package eu.bcvsolutions.idm.core.model.service.api;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.service.CodeableService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
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
	
	static final String CONFIDENTIAL_PROPERTY_VALUE = "config:value";	
}
