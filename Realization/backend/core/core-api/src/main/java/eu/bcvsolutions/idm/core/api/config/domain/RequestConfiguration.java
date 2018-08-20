package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Request configuration properties
 * 
 * @author svandav
 *
 */
public interface RequestConfiguration extends Configurable {
	
	/**
	 * Prefix of WF definition key
	 */
	String PROPERTY_WF_PREFIX = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.request";
	/**
	 * Enable requesting of role
	 */
	String PROPERTY_ROLE_ENABLE = 
			ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.request.idm-role.enabled";
	String CAMEL_SPLIT_REGEX = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";
	
	
	
	@Override
	default String getConfigurableType() {
		return "request";
	}
	
	@Override
	default boolean isDisableable() {
		return false;
	}
	
	@Override
	default public boolean isSecured() {
		return false;
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>();
		properties.add(getPropertyName(PROPERTY_ROLE_ENABLE));
		return properties;
	}
	
	/**
	 * Are requests of role enabled?
	 * @return
	 */
	boolean isRoleRequestEnabled();

	/**
	 * Get approval process key
	 * 
	 * @param entityType
	 * @return
	 */
	String getRequestApprovalProcessKey(Class<Requestable> entityType);
}
