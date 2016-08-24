package eu.bcvsolutions.idm.core.model.domain;

/**
 * Interface to mark objects that are identifiable by an NAME of any type.
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
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