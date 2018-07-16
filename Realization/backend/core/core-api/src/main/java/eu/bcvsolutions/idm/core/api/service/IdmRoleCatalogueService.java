package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Role could be in one catalogue (simply roles folder)
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
public interface IdmRoleCatalogueService extends 
		EventableDtoService<IdmRoleCatalogueDto, IdmRoleCatalogueFilter>, 
		CodeableService<IdmRoleCatalogueDto>,
		AuthorizableService<IdmRoleCatalogueDto>,
		ScriptEnabled {
	
	/**
	 * Prefix to configuration
	 */
	String CONFIGURATION_PREFIX = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.roleCatalogue.";
	String CONFIGURATION_PROPERTY_VALID = "valid";
	String CONFIGURATION_PROPERTY_REBUILD = "rebuild";	
	
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
	 Page<IdmRoleCatalogueDto> findRoots(Pageable pageable);
	 
	 /**
	 * Method returns direct children by parent id
	 * 
	 * @param parent
	 * @return Page of children
	 */
	Page<IdmRoleCatalogueDto> findChildrenByParent(UUID parentId, Pageable pageable);
	
	/**
	 * Returns configuration property name for role catalogue.
	 * 
	 * @param propertyName
	 * @return
	 */
	String getConfigurationPropertyName(String propertyName);
	
	/**
	 * Get list of {@link IdmRoleCatalogueDto} for given role.
	 * 
	 * @param role
	 * @return
	 */
	List<IdmRoleCatalogueDto> findAllByRole(UUID role);

	/**
	 * Get list of {@link IdmRoleCatalogueDto} parents for given catalogue.
	 * @param catalogueId
	 * @return
	 */
	List<IdmRoleCatalogueDto> findAllParents(UUID catalogueId);
}
