package eu.bcvsolutions.idm.ic.api;

import java.io.Serializable;
import java.util.UUID;

/**
 * Interface for object class. Object class defined type or category of
 * connector object.
 * 
 * @author Vít Švanda
 *
 */
public interface IcObjectClass extends Serializable {

	/**
	 * Return type or category of connector object
	 * 
	 * @return
	 */
	String getType();

	/**
	 * Return display name for this type of object class
	 * 
	 * @return
	 */
	String getDisplayName();

	void setDisplayName(String displayName);

	/**
	 * Get relation on the IdmRoleRequest, can be null and request doesn't have to
	 * exist (used in virtual systems).
	 */
	UUID getRoleRequestId();

	/**
	 * Set relation on the IdmRoleRequest, can be null and request doesn't have to
	 * exist (used in virtual systems).
	 */
	void setRoleRequestId(UUID roleRequestId);

}