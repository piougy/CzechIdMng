package eu.bcvsolutions.idm.core.scheduler.api.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

/**
 * Long running task DTO.
 * 
 * @author Jan Helbich
 */
public class IdmLongRunningTaskDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
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

}
