package eu.bcvsolutions.idm.core.api.dto.filter;

/**
 * Context (~filter) for load permission together with loaded dto.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
public interface PermissionContext extends BaseDataFilter {

	String PARAMETER_ADD_PERMISSIONS = "addPermissions";
	
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

}