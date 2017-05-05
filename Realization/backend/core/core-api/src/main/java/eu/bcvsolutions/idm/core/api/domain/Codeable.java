package eu.bcvsolutions.idm.core.api.domain;

/**
 * Interface to mark objects that are identifiable by an string CODE.
 * Create attribute code that is unique for all object from type.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */

public interface Codeable extends Identifiable {

	/**
	 * Returns the code identifying the object.
	 * 
	 * @return code
	 */
	String getCode();
}
