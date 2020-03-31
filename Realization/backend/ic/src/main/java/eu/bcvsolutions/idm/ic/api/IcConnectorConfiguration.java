package eu.bcvsolutions.idm.ic.api;

import java.io.Serializable;
import java.util.Map;

/**
 * Configuration for IC connector
 * @author svandav
 *
 */
public interface IcConnectorConfiguration extends Serializable {

	/**
	 * @return the configurationProperties
	 */
	IcConfigurationProperties getConfigurationProperties();

	/**
	 * @return the connectorPoolingSupported
	 */
	boolean isConnectorPoolingSupported();

	/**
	 * @return the connectorPoolConfiguration
	 */
	IcObjectPoolConfiguration getConnectorPoolConfiguration();

	/**
	 * @return the producerBufferSize
	 */
	int getProducerBufferSize();

	/**
	 * @return Map of operation options for this system
	 */
	Map<String, Object> getSystemOperationOptions();

}