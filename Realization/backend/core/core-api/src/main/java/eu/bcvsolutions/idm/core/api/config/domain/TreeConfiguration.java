package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Tree configuration properties
 * 
 * @author Radek Tomiška
 *
 */
public interface TreeConfiguration extends Configurable {
	
	/**
	 * Default tree type
	 */
	static final String PROPERTY_DEFAULT_TYPE = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.tree.defaultType";
	
	/**
	 * Default tree node
	 */
	static final String PROPERTY_DEFAULT_NODE = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.tree.defaultNode";
	
	
	@Override
	default String getConfigurableType() {
		return "tree";
	}
	
	@Override
	default boolean isDisableable() {
		return false;
	}
	
	@Override
	default public boolean isSecured() {
		return true;
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sense here
		properties.add(getPropertyName(PROPERTY_DEFAULT_TYPE));
		properties.add(getPropertyName(PROPERTY_DEFAULT_NODE));
		return properties;
	}
	
	/**
	 * Returns default tree type
	 * 
	 * @return
	 */
	IdmTreeTypeDto getDefaultType();
	
	/**
	 * Sets default tree type
	 * 
	 * @param treeTypeId
	 */
	void setDefaultType(UUID treeTypeId);
	
	/**
	 * Returns default tree node
	 * 
	 * @return
	 */
	IdmTreeNodeDto getDefaultNode();
	
	/**
	 * Sets default tree node
	 * 
	 * @param treeNodeId
	 */
	void setDefaultNode(UUID treeNodeId);
}
