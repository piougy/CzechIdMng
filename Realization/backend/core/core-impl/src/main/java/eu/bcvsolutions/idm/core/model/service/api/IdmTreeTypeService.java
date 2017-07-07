package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.CodeableService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Operations with IdmTreeType
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public interface IdmTreeTypeService extends 
		ReadWriteEntityService<IdmTreeType, QuickFilter>,
		CodeableService<IdmTreeType> {

	/**
	 * Prefix to configuration
	 */
	public static final String CONFIGURATION_PREFIX = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.treeType.";
	public static final String CONFIGURATION_PROPERTY_VALID = "valid";
	public static final String CONFIGURATION_PROPERTY_REBUILD = "rebuild";
	
	/**
	 * Returns tree type by code 
	 * 
	 * @param code
	 * @return
	 */
	@Override
	IdmTreeType getByCode(String code);
	
	/**
	 * Returns default tree type or {@code null}, if no default tree type is defined
	 * 
	 * @return
	 */
	IdmTreeType getDefaultTreeType();
	
	/**
	 * Returns all configuration properties for given tree type.
	 * 
	 * @param treeType
	 * @return
	 */
	List<IdmConfigurationDto> getConfigurations(IdmTreeType treeType);
	
	/**
	 * Returns configuration property name for given tree type.
	 * 
	 * @param treeTypeCode
	 * @param propertyName
	 * @return
	 */
	String getConfigurationPropertyName(String treeTypeCode, String propertyName);
	
	/**
	 * Clear default tree node, when node is deleted etc.
	 * 
	 * @param defaultTreeNode
	 * @return
	 */
	int clearDefaultTreeNode(IdmTreeNode defaultTreeNode);
}
