package eu.bcvsolutions.idm.security.domain;

/**
 * Base application permission
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public interface BasePermission {
	
	public static final String SEPARATOR = "_";

	/**
	 * Permission identifier
	 * 
	 * @return
	 */
	String getName();
}
