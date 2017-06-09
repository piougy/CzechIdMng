package eu.bcvsolutions.idm.ic.api;

/**
 * Configuration for IC connector
 * @author svandav
 *
 */
public interface IcConnectorConfiguration {

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

}