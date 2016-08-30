package eu.bcvsolutions.idm.security.domain;

/**
 * Base application permission
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
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
}
