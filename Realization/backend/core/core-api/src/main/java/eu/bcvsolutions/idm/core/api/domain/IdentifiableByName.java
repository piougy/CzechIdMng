package eu.bcvsolutions.idm.core.api.domain;

/**
 * Interface to mark objects that are identifiable by an NAME of any type.
 * 
 * @author Radek Tomiška 
 *
 * @param <NAME>
 */
public interface IdentifiableByName {

	/**
	 * Returns the name identifying the object.
	 * 
	 * @return the identifier or {@literal null} if not available.
	 */
	String getName();
}