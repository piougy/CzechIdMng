package eu.bcvsolutions.idm.core.scheduler.api.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.joda.time.DateTime;
import org.quartz.Trigger;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;

/**
 * Base class for task triggers
 */
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "_type")
@JsonSubTypes({
	@JsonSubTypes.Type(value = SimpleTaskTrigger.class),
	@JsonSubTypes.Type(value = CronTaskTrigger.class)
})
public abstract class AbstractTaskTrigger {

	private String id;
	@NotNull
	private String taskId;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;	
	private DateTime nextFireTime;
	private DateTime previousFireTime;
	private TaskTriggerState state;
	
	public AbstractTaskTrigger() {
	}
	
	/**
	 * Creates a new instance using trigger and state
	 * 
	 * @param trigger trigger
	 * @param state state
	 */
	public AbstractTaskTrigger(String taskId, Trigger trigger, TaskTriggerState state) {
		this.taskId = taskId;
		this.id = trigger.getKey().getName();
		this.description = trigger.getDescription();
		this.nextFireTime = new DateTime(trigger.getNextFireTime());
		this.previousFireTime = new DateTime(trigger.getPreviousFireTime());
		this.state = state;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getTaskId() {
		return taskId;
	}
	
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public DateTime getNextFireTime() {
		return nextFireTime;
	}
	
	public void setNextFireTime(DateTime nextFireTime) {
		this.nextFireTime = nextFireTime;
	}
	
	public DateTime getPreviousFireTime() {
		return previousFireTime;
	}
	
	public void setPreviousFireTime(DateTime previousFireTime) {
		this.previousFireTime = previousFireTime;
	}
	
	public TaskTriggerState getState() {
		return state;
	}
	
	public void setState(TaskTriggerState state) {
		this.state = state;
	}
}