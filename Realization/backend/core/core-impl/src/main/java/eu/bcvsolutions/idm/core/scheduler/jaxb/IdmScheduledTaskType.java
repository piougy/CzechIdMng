package eu.bcvsolutions.idm.core.scheduler.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Basic type for one instance of scheduled task.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@XmlRootElement(name = "task")
public class IdmScheduledTaskType {

	private String taskType;
	private String description;
	private String instanceId;
	private IdmScheduledTaskTriggersType triggers;
	private IdmScheduledTaskParametersType parameters;

	@XmlElement(required = true, type = String.class)
	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	@XmlElement(required = false, type = String.class)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@XmlElement(required = false, type = IdmScheduledTaskTriggersType.class)
	public IdmScheduledTaskTriggersType getTriggers() {
		return triggers;
	}

	public void setTriggers(IdmScheduledTaskTriggersType triggers) {
		this.triggers = triggers;
	}

	@XmlElement(required = false, type = IdmScheduledTaskParametersType.class)
	public IdmScheduledTaskParametersType getParameters() {
		return parameters;
	}

	public void setParameters(IdmScheduledTaskParametersType parameters) {
		this.parameters = parameters;
	}

	@XmlElement(required = true, type = String.class)
	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

}
