package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.forest.index.service.api.ForestContentService;
import eu.bcvsolutions.idm.core.api.dto.ConfigurationDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleCatalogueFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;

/**
 * Role could be in one catalogue (simply roles folder)
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmRoleCatalogueService extends 
		ReadWriteEntityService<IdmRoleCatalogue, RoleCatalogueFilter>, 
		IdentifiableByNameEntityService<IdmRoleCatalogue>,
		ForestContentService<IdmRoleCatalogue, IdmForestIndexEntity, UUID> {
	
	/**
	 * Prefix to configuration
	 */
	public static final String CONFIGURATION_PREFIX = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.roleCatalogue.";
	public static final String CONFIGURATION_PROPERTY_VALID = "valid";
	public static final String CONFIGURATION_PROPERTY_REBUILD = "rebuild";	
	
	/**
	 * Rebuild (drop and create) all indexes for role catalogue.
	 * 
	 * @return long running task id
	 */
	UUID rebuildIndexes();
	
	/**
	 * Returns all roots
	 * 
	 * @return
	 */
	Page<IdmRoleCatalogue> findRoots(Pageable pageable);
	
	/**
	 * Returns all configuration properties for role catalogue.
	 * 
	 * @return
	 */
	List<ConfigurationDto> getConfigurations();
	
	/**
	 * Returns configuration property name for role catalogue.
	 * 
	 * @param propertyName
	 * @return
	 */
	String getConfigurationPropertyName(String propertyName);
}
