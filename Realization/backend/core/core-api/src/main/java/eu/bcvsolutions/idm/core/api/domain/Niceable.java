package eu.bcvsolutions.idm.core.api.domain;

/**
 * Interface to mark objects that have some nice label (nice label may not be unique).
 * 
 * @author Vít Švanda
 *
 */

public interface Niceable {

	/**
	 * Returns the nice label for that object (not unique).
	 * 
	 * @return nice label
	 */
	String getNiceLabel();
}
