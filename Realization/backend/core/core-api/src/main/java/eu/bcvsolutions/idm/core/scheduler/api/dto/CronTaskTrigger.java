package eu.bcvsolutions.idm.core.scheduler.api.dto;

import javax.validation.constraints.NotNull;

import org.quartz.CronTrigger;

/**
 * Cron task trigger
 */
public class CronTaskTrigger extends AbstractTaskTrigger {

	@NotNull
	private String cron;
	
	public CronTaskTrigger() {
	}
	
	/**
	 * Creates a new instance using trigger and state
	 * 
	 * @param trigger trigger
	 * @param state state
	 */
	public CronTaskTrigger(String taskId, CronTrigger trigger, TaskTriggerState state) {
		super(taskId, trigger, state);
		
		cron = trigger.getCronExpression();
	}
	
	public String getCron() {
		return cron;
	}
	
	public void setCron(String cron) {
		this.cron = cron;
	}
}
