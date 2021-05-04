package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;

/**
 * Configuration for event processing.
 * 
 * @see SchedulerConfiguration
 * @author Radek Tomiška
 */
public interface EventConfiguration extends Configurable {
	
	/**
	 * Asynchronous event processing is enabled. Asynchronous event processing can be disabled for testing or debugging purposes.
	 * Events are processed synchronously.
	 */
	String PROPERTY_EVENT_ASYNCHRONOUS_ENABLED = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.event.asynchronous.enabled";
	boolean DEFAULT_EVENT_ASYNCHRONOUS_ENABLED = true;
	
	/**
	 * Asynchronous event processing is stopped.
	 * Asynchronous event processing is stopped, when instance for processing is switched => prevent to process asynchronous events in the meantime.
	 * Asynchronous event processing can be stopped for testing or debugging purposes.
	 * Asynchronous events are still created in queue, but they are not processed.
	 * 
	 * @since 11.1.0
	 */
	String PROPERTY_EVENT_ASYNCHRONOUS_STOP_PROCESSING = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.event.asynchronous.stopProcessing";
	boolean DEFAULT_EVENT_ASYNCHRONOUS_STOP_PROCESSING = false;
	
	/**
	 * Asynchronous events will be executed on server instance with id. Default is the same as {@link ConfigurationService#getInstanceId()} (current server instance).
	 */
	String PROPERTY_EVENT_ASYNCHRONOUS_INSTANCE_ID = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.event.asynchronous.instanceId";
	
	/**
	 * Asynchronous events will be executed in batch - batch will be split for event with HIGH / NORMAL priority in 70% HIGH / 30% NORMAL.
	 */
	String PROPERTY_EVENT_ASYNCHRONOUS_BATCH_SIZE = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.event.asynchronous.batchSize";
	int DEFAULT_EVENT_BATCH_SIZE = 15;
	
	
	@Override
	default String getConfigurableType() {
		return "event";
	}
	
	@Override
	default boolean isDisableable() {
		return false;
	}
	
	@Override
	default public boolean isSecured() {
		return true;
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sense here
		properties.add(getPropertyName(PROPERTY_EVENT_ASYNCHRONOUS_ENABLED));
		properties.add(getPropertyName(PROPERTY_EVENT_ASYNCHRONOUS_INSTANCE_ID));
		return properties;
	}
	
	/**
	 * Asynchronous event executor 
	 * 
	 * @return
	 */
	Executor getExecutor();
	
	/**
	 * Returns true, if asynchronous event processing is enabled
	 * 
	 * @return
	 */
	boolean isAsynchronous();
	
	/**
	 * Asynchronous event processing is stopped.
	 * Asynchronous event processing is stopped, when instance for processing is switched => prevent to process asynchronous events in the meantime.
	 * Asynchronous event processing can be stopped for testing or debugging purposes.
	 * Asynchronous events are still created in queue, but they are not processed.
	 * 
	 * @since 11.1.0
	 * @return true - asynchronous events are not processed 
	 */
	boolean isStopProcessing();
	
	/**
	 * Asynchronous events will be executed on server instance with returned id.
	 * 
	 * @return
	 */
	String getAsynchronousInstanceId();
	
	/**
	 * Asynchronous events will be executed in batch - batch will be split for event with HIGH / NORMAL priority in 70% HIGH / 30% NORMAL.
	 * 
	 * @return
	 */
	int getBatchSize();

}
