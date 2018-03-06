package eu.bcvsolutions.idm.core.scheduler.api.service;

import java.util.UUID;

import org.quartz.Job;

/**
 * Interface for schedulable task services (this services will be automatically registered to sheduler tasks)
 * 
 * @author Radek Tomiška
 */
public interface SchedulableTaskExecutor<V> extends LongRunningTaskExecutor<V>, Job {

	String PARAMETER_INSTANCE_ID = LongRunningTaskExecutor.PARAMETER_INSTANCE_ID; // server instance id
	String PARAMETER_DRY_RUN = "core:dryRun"; // dry run mode

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
