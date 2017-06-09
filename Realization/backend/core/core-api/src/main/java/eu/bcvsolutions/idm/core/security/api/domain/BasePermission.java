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
	public static final String SEPARATOR = "_";

	/**
	 * Permission identifier
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * Permission is defined in module
	 * 
	 * @return
	 */
	String getModule();
}
