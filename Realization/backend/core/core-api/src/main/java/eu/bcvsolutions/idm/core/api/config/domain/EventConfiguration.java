package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Configuration for event processing
 * 
 * @author Radek Tomiška
 *
 */
public interface EventConfiguration extends Configurable {
	
	/**
	 * Asynchronous event processing is enabled. Asynchronous event processing can be disabled for testing or debugging purposes.
	 */
	String PROPERTY_EVENT_ASYNCHRONOUS_ENABLED = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.event.asynchronous.enabled";
	boolean DEFAULT_EVENT_ASYNCHRONOUS_ENABLED = true;
	
	/**
	 * Asynchronous events will be executed on server instance with id. Default is the same as {@link ConfigurationService#getInstanceId()} (current server instance).
	 */
	String PROPERTY_EVENT_ASYNCHRONOUS_INSTANCE_ID = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.event.asynchronous.instanceId";
	
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
	 * Asynchronous events will be executed on server instance with returned id.
	 * 
	 * @return
	 */
	String getAsynchronousInstanceId();

}
