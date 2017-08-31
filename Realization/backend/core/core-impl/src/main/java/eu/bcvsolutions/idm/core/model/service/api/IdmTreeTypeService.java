package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeTypeFilter;
import eu.bcvsolutions.idm.core.api.service.CodeableService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with tree types
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@Service
public interface IdmTreeTypeService extends 
		EventableDtoService<IdmTreeTypeDto, IdmTreeTypeFilter>,
		CodeableService<IdmTreeTypeDto>,
		AuthorizableService<IdmTreeTypeDto> {

	/**
	 * Prefix to configuration
	 */
	static final String CONFIGURATION_PREFIX = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.treeType.";
	static final String CONFIGURATION_PROPERTY_VALID = "valid";
	static final String CONFIGURATION_PROPERTY_REBUILD = "rebuild";
	
	/**
	 * Returns tree type by code 
	 * 
	 * @param code
	 * @return
	 */
	@Override
	IdmTreeTypeDto getByCode(String code);
	
	/**
	 * Returns default tree type or {@code null}, if no default tree type is defined
	 * 
	 * @return
	 */
	IdmTreeTypeDto getDefaultTreeType();
	
	/**
	 * Returns all configuration properties for given tree type. {@link #getConfigurations(UUID)}
	 * 
	 * @param treeType
	 * @return
	 */
	List<IdmConfigurationDto> getConfigurations(UUID treeTypeId);
	
	/**
	 * Returns configuration property name for given tree type.
	 * 
	 * @param treeTypeCode
	 * @param propertyName
	 * @return
	 */
	String getConfigurationPropertyName(String treeTypeCode, String propertyName);
}
