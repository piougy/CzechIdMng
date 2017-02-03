package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Schedulable task services (this services will be automatically available as scheduled tasks)
 * 
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractSchedulableTaskExecutor extends AbstractLongRunningTaskExecutor implements SchedulableTaskExecutor {

	@Autowired
	private SecurityService securityService;
	@Autowired
	private IdmLongRunningTaskService longRunningTaskService;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// run as system - called from scheduler internally
		securityService.setSystemAuthentication();
		//
		// add task to queue only
		// for running long running tasks is long running task service
		IdmLongRunningTask longRunningTask = new IdmLongRunningTask();
		longRunningTask.setTaskType(getClass().getCanonicalName());
		longRunningTask.setTaskDescription(context.getJobDetail().getDescription());
		longRunningTask.setTaskProperties(context.getMergedJobDataMap());
		longRunningTask.setResult(new OperationResult.Builder(OperationState.CREATED).build());
		longRunningTask.setInstanceId(context.getMergedJobDataMap().getString(SchedulableTaskExecutor.PARAMETER_INSTANCE_ID));
		//
		longRunningTaskService.save(longRunningTask);
	}
	
	/**
	 * Returns universal task parameters. Don't forget to override this method additively.
	 */
	@Override
	public List<String> getParameterNames() {
		// any parameter for now
		return new ArrayList<>();
	}
}
