package eu.bcvsolutions.idm.core.scheduler.api.service;

import java.util.UUID;

import org.quartz.Job;

/**
 * Interface for schedulable task services (this services will be automatically registered to sheduler tasks)
 * 
 * @author Radek Tomiška
 */
public interface SchedulableTaskExecutor<V> extends LongRunningTaskExecutor<V>, Job {

	static final String PARAMETER_INSTANCE_ID = LongRunningTaskExecutor.PARAMETER_INSTANCE_ID; // server instance id
	static final String PARAMETER_DRY_RUN = "core:dryRun"; // dry run mode

	/**
	 * Returns entity ID of currently processed scheduled task. 
	 * @return
	 */
	UUID getScheduledTaskId();
}
