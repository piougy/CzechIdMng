package eu.bcvsolutions.idm.core.scheduler.entity;

import java.io.Serializable;
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

/**
 * Persisted long running task
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_long_running_task", indexes = { 
		@Index(name = "idx_idm_long_r_t_inst", columnList = "instance_id"),
		@Index(name = "idx_idm_long_r_t_type", columnList = "task_type")
		})
public class IdmLongRunningTask extends AbstractEntity {
	
	private static final long serialVersionUID = -4665452018920201474L;
	
	@NotEmpty
	@Column(name = "task_type", length = DefaultFieldLengths.NAME, nullable = false)
	private String taskType;
	
	@Column(name = "task_description", length = DefaultFieldLengths.NAME)
	private String taskDescription;
	
	@Column(name = "task_properties", length = Integer.MAX_VALUE)
	private Serializable taskProperties;
	
	@Column(name = "task_count")
	private Long count;
	
	@Column(name = "task_counter")
	private Long counter;
	
	@NotNull
	@Column(name = "running", nullable = false)
	private boolean running = false;
	
	@NotNull
	@Column(name = "instance_id", length = DefaultFieldLengths.NAME, nullable = false)
	private String instanceId;
	
	@Column(name = "thread_id", nullable = false)
	private long threadId;
	
	@Column(name = "thread_name", length = DefaultFieldLengths.NAME )
	private String threadName;
	
	@Embedded
	private OperationResult result;

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
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

	public OperationResult getResult() {
		return result;
	}

	public void setResult(OperationResult result) {
		this.result = result;
	}
	
	public String getTaskDescription() {
		return taskDescription;
	}
	
	public void setTaskDescription(String taskDescription) {
		this.taskDescription = taskDescription;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public long getThreadId() {
		return threadId;
	}
	
	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}
	
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	public String getInstanceId() {
		return instanceId;
	}
	
	public String getThreadName() {
		return threadName;
	}
	
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}
	
	public void setTaskProperties(Serializable taskProperties) {
		this.taskProperties = taskProperties;
	}
	
	public Serializable getTaskProperties() {
		if (taskProperties == null) {
			taskProperties = new HashMap<>();
		}
		return taskProperties;
	}
}
