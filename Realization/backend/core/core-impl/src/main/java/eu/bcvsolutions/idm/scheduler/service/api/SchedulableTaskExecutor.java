package eu.bcvsolutions.idm.scheduler.service.api;

import java.util.List;

import org.quartz.Job;

/**
 * Interface for schedulable task services (this services will be automatically registered to sheduler tasks)
 * 
 * @author Radek Tomiška
 * 
 */
public interface SchedulableTaskExecutor extends LongRunningTaskExecutor, Job {

	static final String PARAMETER_INSTANCE_ID = "core:instanceId"; // server instance id
	
	/**
	 * Returns form parameter names for this task
	 * 
	 * @return
	 */
	List<String> getParameterNames();
}
