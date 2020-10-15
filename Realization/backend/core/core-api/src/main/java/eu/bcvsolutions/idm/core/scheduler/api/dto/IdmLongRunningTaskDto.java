package eu.bcvsolutions.idm.core.scheduler.api.dto;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

/**
 * Long running task DTO.
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "longRunningTasks")
public class IdmLongRunningTaskDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	//
	private String taskType;
	private String taskDescription;
	private Map<String, Object> taskProperties;
	private Long count;
	private Long counter;
	private boolean running;
	private String instanceId;
	private long threadId;
	private String threadName;
	private OperationResult result;
	@Embedded(dtoClass = IdmScheduledTaskDto.class)
	private UUID scheduledTask;
	private boolean stateful;
	private boolean recoverable;
	private ZonedDateTime taskStarted;
	private ZonedDateTime taskEnded;
	private boolean dryRun;
	@JsonProperty(access = Access.READ_ONLY)
	private Long successItemCount;
	@JsonProperty(access = Access.READ_ONLY)
	private Long failedItemCount;
	@JsonProperty(access = Access.READ_ONLY)
	private Long warningItemCount;

	public IdmLongRunningTaskDto() {
	}
	
	public IdmLongRunningTaskDto(UUID id) {
		super(id);
	}
	
	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	public String getTaskDescription() {
		return taskDescription;
	}

	public void setTaskDescription(String taskDescription) {
		this.taskDescription = taskDescription;
	}

	public Map<String, Object> getTaskProperties() {
		if (taskProperties == null) {
			taskProperties = new HashMap<>();
		}
		return taskProperties;
	}

	public void setTaskProperties(Map<String, Object> taskProperties) {
		this.taskProperties = taskProperties;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Long getCounter() {
		return counter;
	}

	public void setCounter(Long counter) {
		this.counter = counter;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public long getThreadId() {
		return threadId;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public OperationResult getResult() {
		return result;
	}

	public void setResult(OperationResult result) {
		this.result = result;
	}

	public UUID getScheduledTask() {
		return scheduledTask;
	}

	public void setScheduledTask(UUID scheduledTask) {
		this.scheduledTask = scheduledTask;
	}
	
	public OperationState getResultState() {
		if (result == null) {
			return null;
		}
		return result.getState();
	}

	public boolean isStateful() {
		return stateful;
	}

	public void setStateful(boolean stateful) {
		this.stateful = stateful;
	}

	public boolean isDryRun() {return dryRun;}

	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

	public Long getSuccessItemCount() {
		return successItemCount;
	}

	public void setSuccessItemCount(Long successItemCount) {
		this.successItemCount = successItemCount;
	}

	public Long getFailedItemCount() {
		return failedItemCount;
	}

	public void setFailedItemCount(Long failedItemCount) {
		this.failedItemCount = failedItemCount;
	}

	public Long getWarningItemCount() {
		return warningItemCount;
	}

	public void setWarningItemCount(Long warningItemCount) {
		this.warningItemCount = warningItemCount;
	}
	
	/**
	 * Task can be executed repetitively without reschedule is needed.
	 * When task is canceled (e.g. by server is restarted), then task can be executed again.
	 * 
	 * @return true - LRT can be executed again.
	 * @since 10.2.0
	 */
	public boolean isRecoverable() {
		return recoverable;
	}
	
	/**
	 * Task can be executed repetitively without reschedule is needed.
	 * When task is canceled (e.g. by server is restarted), then task can be executed again.
	 * 
	 * @param recoverable  true - LRT can be executed again.
	 * @since 10.2.0
	 */
	public void setRecoverable(boolean recoverable) {
		this.recoverable = recoverable;
	}
	
	public void setTaskStarted(ZonedDateTime date){
		taskStarted = date;
	}

	public ZonedDateTime getTaskStarted(){
		return taskStarted;
	}
	
	/**
	 * Task ended datetime.
	 * 
	 * @return task end datetime
	 * @since 10.6.0
	 */
	public ZonedDateTime getTaskEnded() {
		return taskEnded;
	}
	
	/**
	 * Task ended datetime.
	 * 
	 * @param taskEnded task end datetime
	 * @since 10.6.0
	 */
	public void setTaskEnded(ZonedDateTime taskEnded) {
		this.taskEnded = taskEnded;
	}
	
	/**
	 * Clear fields persist run state.
	 * 
	 * @since 10.2.0
	 */
	public void clearState() {
		setCount(null);
		setCounter(null);
		setWarningItemCount(null);
		setSuccessItemCount(null);
		setFailedItemCount(null);
		setTaskStarted(null);
		setTaskEnded(null);
		setResult(null);
		setThreadId(0);
		setThreadName(null);
	}
}
