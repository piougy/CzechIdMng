package eu.bcvsolutions.idm.core.model.service.api;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.CodeableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;

/**
 * Provides configuration through application
 * 
 * @author Radek Tomiška 
 * 
 * @see ConfigurationService
 *
 */
public interface IdmConfigurationService extends ReadWriteEntityService<IdmConfiguration, QuickFilter>, 
		CodeableService<IdmConfiguration>, ConfigurationService {
	
	static final String CONFIDENTIAL_PROPERTY_VALUE = "config:value";	
}
