package eu.bcvsolutions.idm.icf.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	String getIcfType();

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
	@JsonIgnore
	default String getFullName() {
		return getIcfType() + ":" + getConnectorName() + ":" + getBundleName() + ":" + getBundleVersion();
	}

}