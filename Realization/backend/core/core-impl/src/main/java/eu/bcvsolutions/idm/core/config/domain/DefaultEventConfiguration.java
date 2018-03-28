package eu.bcvsolutions.idm.core.config.domain;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;

/**
 * Configuration for features with event processing.
 * 
 * @author Radek Tomiška
 *
 */
@Component("eventConfiguration")
public class DefaultEventConfiguration extends AbstractConfiguration implements EventConfiguration {	
	
	@Autowired 
	@Qualifier(SchedulerConfiguration.EVENT_EXECUTOR_NAME)
	private Executor executor;
	
	@Override
	public Executor getExecutor() {
		return executor;
	}
	
	@Override
	public boolean isAsynchronous() {
		return getConfigurationService().getBooleanValue(PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, DEFAULT_EVENT_ASYNCHRONOUS_ENABLED);
	}
	
	@Override
	public String getAsynchronousInstanceId() {
		return getConfigurationService().getValue(PROPERTY_EVENT_ASYNCHRONOUS_INSTANCE_ID, getConfigurationService().getInstanceId());
	}
}
