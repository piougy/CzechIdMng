package eu.bcvsolutions.idm.core.security.api.domain;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;

public interface AuthorizationPolicy {
	
	static final String PERMISSION_SEPARATOR = ",";
	
	/**
	 * Policy for group (agenda)
	 * 
	 * @return
	 */
	String getGroupPermission();

	/**
	 * Policy for identifiable type
	 * 
	 * @return
	 */
	String getAuthorizableType();
	
	/**
	 * Policy evaluator
	 * 
	 * @return
	 */
	String getEvaluatorType();
	
	/**
	 * Policy properties
	 * 
	 * @return
	 */
	ConfigurationMap getEvaluatorProperties();
	
	/**
	 * Returns base permissions granted by this policy
	 *  
	 * @return
	 */
	String getBasePermissions();
	
	/**
	 * Returns base permissions granted by this policy as set
	 * 
	 * @return
	 */
	default Set<String> getPermissions() {
		Set<String> permissions = new HashSet<>();
		if (StringUtils.isNotEmpty(getBasePermissions())) {
			for (String basePermission : getBasePermissions().split(AuthorizationPolicy.PERMISSION_SEPARATOR)) {
				if(StringUtils.isNotBlank(basePermission)) {
					permissions.add(basePermission.toUpperCase().trim());
				}
			}
		}
		return permissions;
	}
}
