package eu.bcvsolutions.idm.core.model.service.api;

import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
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
public interface IdmConfigurationService extends ConfigurationService, ReadWriteEntityService<IdmConfiguration, QuickFilter>, IdentifiableByNameEntityService<IdmConfiguration> {
	
}
