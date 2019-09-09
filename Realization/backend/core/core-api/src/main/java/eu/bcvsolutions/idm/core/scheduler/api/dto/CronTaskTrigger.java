package eu.bcvsolutions.idm.core.scheduler.api.dto;

import javax.validation.constraints.NotNull;

import org.quartz.CronTrigger;

/**
 * Cron task trigger
 * 
 * @author Radek Tomiška
 */
public class CronTaskTrigger extends AbstractTaskTrigger {

	private static final long serialVersionUID = 1L;
	
	@NotNull
	private String cron;
	private String executeDate;
	
	public CronTaskTrigger() {
	}
	
	/**
	 * Creates a new instance using trigger and state
	 * 
	 * @param trigger trigger
	 * @param state state
	 */
	public CronTaskTrigger(String taskId, CronTrigger trigger, TaskTriggerState state, String executeDate) {
		super(taskId, trigger, state);
		
		this.executeDate = executeDate;
		cron = trigger.getCronExpression();
	}
	
	public String getCron() {
		return cron;
	}
	
	public void setCron(String cron) {
		this.cron = cron;
	}

	public String getExecuteDate() {
		return executeDate;
	}

	public void setExecuteDate(String executeDate) {
		this.executeDate = executeDate;
	}
}
