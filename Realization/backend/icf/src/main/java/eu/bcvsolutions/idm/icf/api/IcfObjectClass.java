package eu.bcvsolutions.idm.icf.api;

/**
 * Interface for object class. Object class defined type or category of connector object.
 * @author svandav
 *
 */
public interface IcfObjectClass {

	/**
	 * Return type or category of connector object
	 * @return
	 */
	String getType();

	/**
	 * Return display name for this type of object class
	 * @return
	 */
	String getDisplayName();

	void setDisplayName(String displayName);

}