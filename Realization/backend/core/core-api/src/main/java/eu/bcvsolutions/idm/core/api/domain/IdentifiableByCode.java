package eu.bcvsolutions.idm.core.api.domain;

/**
 * Interface to mark objects that are identifiable by an CODE of any type.
 * Create attribute code that is unique for all object from type.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdentifiableByCode {

	/**
	 * Returns the code identifying the object.
	 * 
	 * @return code
	 */
	String getCode();
}
