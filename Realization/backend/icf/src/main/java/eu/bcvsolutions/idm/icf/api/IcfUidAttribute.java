package eu.bcvsolutions.idm.icf.api;

import org.identityconnectors.framework.common.objects.ConnectorObject;

public interface IcfUidAttribute {

	/**
	 * Obtain a string representation of the value of this attribute, which
	 * value uniquely identifies a {@link ConnectorObject object} on the target
	 * resource.
	 *
	 * @return value that uniquely identifies an object.
	 */
	String getUidValue();

	String getRevision();

}