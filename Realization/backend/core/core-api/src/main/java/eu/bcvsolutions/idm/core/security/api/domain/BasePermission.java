package eu.bcvsolutions.idm.core.security.api.domain;

/**
 * Base application permission
 * 
 * @author Radek Tomiška 
 */
public interface BasePermission {
	
	/**
	 * Is used for joining permission to authority
	 */
	String SEPARATOR = "_";

	/**
	 * Permission identifier
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * Permission is defined in module
	 * 
	 * @return {@code null} as default. Module is needed for localization on FE mainly.
	 */
	default String getModule() {
		return null;
	}
}
