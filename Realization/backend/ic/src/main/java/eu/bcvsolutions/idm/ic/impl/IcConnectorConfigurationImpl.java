package eu.bcvsolutions.idm.ic.impl;

import eu.bcvsolutions.idm.ic.api.IcConfigurationProperties;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcObjectPoolConfiguration;

/**
 * Configuration for IC connector
 * @author svandav
 *
 */
public class IcConnectorConfigurationImpl implements IcConnectorConfiguration {
	/**
	 * Instance of the configuration properties.
	 */
	private IcConfigurationProperties configurationProperties;

	/**
	 * Determines if this connector uses the framework's connector pooling.
	 */
	private boolean connectorPoolingSupported;

	/**
	 * Connector pooling configuration.
	 */
	private IcObjectPoolConfiguration connectorPoolConfiguration;

	/**
	 * The size of the buffer for Connector the support. Default is 100, if size
	 * is set to zero or less will disable.
	 * 
	 */
	private int producerBufferSize = 100;

	/**
	 * @return the configurationProperties
	 */
	@Override
	public IcConfigurationProperties getConfigurationProperties() {
		return configurationProperties;
	}

	/**
	 * @param configurationProperties
	 *            the configurationProperties to set
	 */
	public void setConfigurationProperties(IcConfigurationProperties configurationProperties) {
		this.configurationProperties = configurationProperties;
	}

	/**
	 * @return the connectorPoolingSupported
	 */
	@Override
	public boolean isConnectorPoolingSupported() {
		return connectorPoolingSupported;
	}

	/**
	 * @param connectorPoolingSupported
	 *            the connectorPoolingSupported to set
	 */
	public void setConnectorPoolingSupported(boolean connectorPoolingSupported) {
		this.connectorPoolingSupported = connectorPoolingSupported;
	}

	/**
	 * @return the connectorPoolConfiguration
	 */
	@Override
	public IcObjectPoolConfiguration getConnectorPoolConfiguration() {
		return connectorPoolConfiguration;
	}

	/**
	 * @param connectorPoolConfiguration
	 *            the connectorPoolConfiguration to set
	 */
	public void setConnectorPoolConfiguration(IcObjectPoolConfiguration connectorPoolConfiguration) {
		this.connectorPoolConfiguration = connectorPoolConfiguration;
	}

	/**
	 * @return the producerBufferSize
	 */
	@Override
	public int getProducerBufferSize() {
		return producerBufferSize;
	}

	/**
	 * @param producerBufferSize
	 *            the producerBufferSize to set
	 */
	public void setProducerBufferSize(int producerBufferSize) {
		this.producerBufferSize = producerBufferSize;
	}

	// /**
	// * Get the configuration of the ResultsHandler chain of the Search
	// * operation.
	// */
	// ResultsHandlerConfiguration getResultsHandlerConfiguration();

	// /**
	// * The set of operations that this connector will support.
	// */
	// private Set<Class<? extends APIOperation>> supportedOperations;
	// /**
	// * Sets the timeout value for the operation provided.
	// *
	// * @param operation
	// * particular operation that requires a timeout.
	// * @param timeout
	// * milliseconds that the operation will wait in order to
	// * complete. Values less than or equal to zero are considered to
	// * disable the timeout property.
	// */
	// void setTimeout(Class<? extends APIOperation> operation, int timeout);
	//
	// /**
	// * Gets the timeout in milliseconds based on the operation provided.
	// *
	// * @param operation
	// * particular operation to get a timeout for.
	// * @return milliseconds to wait for an operation to complete before
	// throwing
	// * an error.
	// */
	// int getTimeout(Class<? extends APIOperation> operation);

}
