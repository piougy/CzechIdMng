package eu.bcvsolutions.idm.core.security.api.domain;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;

public interface AuthorizationPolicy {

	/**
	 * Policy for identifiable type
	 * 
	 * @return
	 */
	String getAuthorizableType();
	
	/**
	 * Policy properties
	 * 
	 * @return
	 */
	ConfigurationMap getEvaluatorProperties();
	
	/**
	 * Returns base permission granted by this policy
	 *  
	 * @return
	 */
	String getBasePermissions();
}
