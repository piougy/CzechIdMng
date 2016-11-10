package eu.bcvsolutions.idm.icf.api;

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