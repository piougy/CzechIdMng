package eu.bcvsolutions.idm.core.scheduler.api.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulableTaskExecutor;

/**
 * Scheduled task with unique id 
 * * could be scheduled 
 * * could run manually through long running task service
 * 
 * @author Radek Tomiška
 *
 */
public class Task {

	private String id; // job name
	private String module;
	@NotEmpty
	private String instanceId;
	private Class<? extends SchedulableTaskExecutor<?>> taskType; // task executor class
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;	
	private List<AbstractTaskTrigger> triggers;
	private Map<String, String> parameters;

	public Task() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getModule() {
		return module;
	}
	
	public void setModule(String module) {
		this.module = module;
	}
	
	public Class<? extends SchedulableTaskExecutor<?>> getTaskType() {
		return taskType;
	}
	
	public void setTaskType(Class<? extends SchedulableTaskExecutor<?>> taskType) {
		this.taskType = taskType;
	}

	public List<AbstractTaskTrigger> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<AbstractTaskTrigger> triggers) {
		this.triggers = triggers;
	}

	public Map<String, String> getParameters() {
		if (parameters == null) {
			parameters = new LinkedHashMap<String, String>();
		}
		return parameters;
	}
	
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	
	/**
	 * Adds task parameter. Returs previous values associated
	 * with the provided key.
	 * @param key
	 * @param value
	 * @return previous value
	 */
	public String addParameter(String key, String value) {
		return getParameters().put(key, value);
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getInstanceId() {
		return instanceId;
	}
	
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
}
