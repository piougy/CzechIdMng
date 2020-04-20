package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Context (~filter) for load permission together with loaded dto.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
public interface PermissionContext extends BaseDataFilter {

	/**
	 * Load permissions into DTO.
	 */
	String PARAMETER_ADD_PERMISSIONS = "addPermissions";
	/**
	 * Evaluate permission, when DTO is loaded.
	 * Parameter name can be given in url parameters together with filter parameters.
	 * 
	 * @since 10.3.0
	 */
	String PARAMETER_EVALUATE_PERMISSION = "_permission";
	
	/**
	 * Load permission together with loaded dto.
	 * 
	 * @return true - permissions will be loaded
	 */
    default boolean getAddPermissions() {
    	return getParameterConverter().toBoolean(getData(), PARAMETER_ADD_PERMISSIONS, false);
    }

    /**
     * Load permission together with loaded dto.
     * 
     * @param value true - permissions will be loaded
     */
    default void setAddPermissions(boolean value) {
    	set(PARAMETER_ADD_PERMISSIONS, value);
    }
    
    /**
     * Evaluate permission, when DTO is loaded.
	 * Parameter name can be given in url parameters together with filter parameters.
	 * 
     * @return base permission to evaluate
     * @since 10.3.0
     */
    default BasePermission getEvaluatePermission() {
    	String rawPermission = getParameterConverter().toString(getData(), PARAMETER_EVALUATE_PERMISSION);
    	//
    	return PermissionUtils.toPermission(rawPermission);
    }

    /**
     * Evaluate permission, when DTO is loaded.
	 * Parameter name can be given in url parameters together with filter parameters.
	 * 
     * @param base permission to evaluate
     * @since 10.3.0
     */
    default void setEvaluatePermission(BasePermission permission) {
    	set(PARAMETER_EVALUATE_PERMISSION, permission);
    }
}