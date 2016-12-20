package eu.bcvsolutions.idm.icf.api;

/**
 * Attribute for uniquely identification object on target resource
 * 
 * @author svandav
 *
 */
public interface IcfUidAttribute extends IcfAttribute {

	/**
	 * Obtain a string representation of the value of this attribute, which
	 * value uniquely identifies a connector object on the target resource.
	 *
	 * @return value that uniquely identifies an object.
	 */
	String getUidValue();

	String getRevision();

}