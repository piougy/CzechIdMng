package eu.bcvsolutions.idm.icf.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * Uniquely identifies a connector within an installation. Consists of the
 * quadruple (icfType, bundleName, bundleVersion, connectorName)
 * @author svandav
 *
 */
public interface IcfConnectorKey {

	/**
	 * Return connector framework type
	 * @return
	 */
	String getFramework();

	/**
	 * Return bundle name for connector
	 * @return
	 */
	String getBundleName();

	/**
	 * Return version of connector
	 * @return
	 */
	String getBundleVersion();

	/**
	 * Return name of connector
	 * @return
	 */
	String getConnectorName();
	
	/**
	 * Returns full connector name
	 * 
	 * @return
	 */
	@JsonProperty(access = Access.READ_ONLY)
	default String getFullName() {
		return getFramework() + ":" + getConnectorName() + ":" + getBundleName() + ":" + getBundleVersion();
	}

}