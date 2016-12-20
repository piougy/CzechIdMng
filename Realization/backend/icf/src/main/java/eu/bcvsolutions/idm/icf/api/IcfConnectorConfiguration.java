package eu.bcvsolutions.idm.icf.api;

/**
 * Configuration for ICF connector
 * @author svandav
 *
 */
public interface IcfConnectorConfiguration {

	/**
	 * @return the configurationProperties
	 */
	IcfConfigurationProperties getConfigurationProperties();

	/**
	 * @return the connectorPoolingSupported
	 */
	boolean isConnectorPoolingSupported();

	/**
	 * @return the connectorPoolConfiguration
	 */
	IcfObjectPoolConfiguration getConnectorPoolConfiguration();

	/**
	 * @return the producerBufferSize
	 */
	int getProducerBufferSize();

}