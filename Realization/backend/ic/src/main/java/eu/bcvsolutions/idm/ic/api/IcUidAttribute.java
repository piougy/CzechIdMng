package eu.bcvsolutions.idm.ic.api;

/**
 * Attribute for uniquely identification object on target resource
 * 
 * @author svandav
 *
 */
public interface IcUidAttribute extends IcAttribute {

	/**
	 * Obtain a string representation of the value of this attribute, which
	 * value uniquely identifies a connector object on the target resource.
	 *
	 * @return value that uniquely identifies an object.
	 */
	String getUidValue();

	String getRevision();

}