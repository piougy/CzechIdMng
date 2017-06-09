package eu.bcvsolutions.idm.ic.api;

import java.io.Serializable;

/**
 * Interface for object class. Object class defined type or category of connector object.
 * 
 * @author svandav
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

}