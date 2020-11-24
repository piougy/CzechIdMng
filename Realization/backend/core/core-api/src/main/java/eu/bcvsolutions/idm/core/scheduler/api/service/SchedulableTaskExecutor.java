package eu.bcvsolutions.idm.core.scheduler.api.service;

import java.util.UUID;

import org.quartz.Job;

import eu.bcvsolutions.idm.core.api.CoreModule;
import eu.bcvsolutions.idm.core.api.domain.Auditable;

/**
 * Interface for schedulable task services (this services will be automatically registered to sheduler tasks).
 * 
 * @author Radek Tomiška
 */
public interface SchedulableTaskExecutor<V> extends LongRunningTaskExecutor<V>, Job {

	String PARAMETER_INSTANCE_ID = LongRunningTaskExecutor.PARAMETER_INSTANCE_ID; // server instance id
	String PARAMETER_MODIFIED = String.format("%s:%s", CoreModule.MODULE_ID, Auditable.PROPERTY_MODIFIED); // task modified date
	String PARAMETER_DRY_RUN = String.format("%s:dryRun", CoreModule.MODULE_ID); // dry run mode

	/**
	 * Returns entity ID of currently processed scheduled task. 
	 * @return
	 */
	UUID getScheduledTaskId();
	
	/**
	 * Returns true, if given task supports dry run mode. Returns {@code false} as default.
	 * 
	 * @return
	 * @since 7.8.3
	 */
	default boolean supportsDryRun() {
		return false;
	}
}
