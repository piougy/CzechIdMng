package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
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
	 * Prefix of WF definition key
	 */
	String PROPERTY_PUBLIC_PREFIX = 
			ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.request";

	String ENABLED_SUFIX = "enabled";

	String CAMEL_SPLIT_REGEX = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";
	
	String DEFAULT_APROVAL_PROCESS_KEY = "request-idm-role";
	
	
	
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
		return properties;
	}

	/**
	 * Get approval process key
	 * 
	 * @param entityType
	 * @return
	 */
	String getRequestApprovalProcessKey(Class<? extends Requestable> entityType);
	
	/**
	 * Is request mode enabled for given requestable class.
	 * !!!All requestable classes are controlled by IdmRoleDto property for now!!!
	 * 
	 * @param entityType
	 * @return
	 */
	boolean isRequestModeEnabled(Class<? extends BaseDto> entityType);
}
