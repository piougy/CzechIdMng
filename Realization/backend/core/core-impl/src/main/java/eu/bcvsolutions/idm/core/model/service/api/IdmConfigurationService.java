package eu.bcvsolutions.idm.core.model.service.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
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
public interface IdmConfigurationService extends ReadWriteEntityService<IdmConfiguration, QuickFilter>, IdentifiableByNameEntityService<IdmConfiguration> {
	
	static final String CONFIDENTIAL_PROPERTY_VALUE = "config:value";
	
	/**
	 * Returns configurations by given keyPrefix.
	 * 
	 * @param keyPrefix
	 * @param pageable
	 * @return
	 */
	public Page<IdmConfiguration> findByPrefix(String keyPrefix, Pageable pageable);
	
}
