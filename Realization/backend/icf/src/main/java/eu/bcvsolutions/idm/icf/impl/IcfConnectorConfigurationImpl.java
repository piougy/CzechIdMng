package eu.bcvsolutions.idm.icf.impl;

import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperties;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfObjectPoolConfiguration;

/**
 * Configuration for ICF connector
 * @author svandav
 *
 */
public class IcfConnectorConfigurationImpl implements IcfConnectorConfiguration {
	/**
	 * Instance of the configuration properties.
	 */
	private IcfConfigurationProperties configurationProperties;

	/**
	 * Determines if this connector uses the framework's connector pooling.
	 */
	private boolean connectorPoolingSupported;

	/**
	 * Connector pooling configuration.
	 */
	private IcfObjectPoolConfiguration connectorPoolConfiguration;

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
	public IcfConfigurationProperties getConfigurationProperties() {
		return configurationProperties;
	}

	/**
	 * @param configurationProperties
	 *            the configurationProperties to set
	 */
	public void setConfigurationProperties(IcfConfigurationProperties configurationProperties) {
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
	public IcfObjectPoolConfiguration getConnectorPoolConfiguration() {
		return connectorPoolConfiguration;
	}

	/**
	 * @param connectorPoolConfiguration
	 *            the connectorPoolConfiguration to set
	 */
	public void setConnectorPoolConfiguration(IcfObjectPoolConfiguration connectorPoolConfiguration) {
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
