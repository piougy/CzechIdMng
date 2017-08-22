package eu.bcvsolutions.idm.core.scheduler.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Jaxb type for init default scheduled tasks. Basic type (main root).
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@XmlRootElement(name = "tasks") // root element
public class IdmScheduledTasksType {

	private List<IdmScheduledTaskType> tasks;

	@XmlElement(name = "task", type = IdmScheduledTaskType.class)
	public List<IdmScheduledTaskType> getTasks() {
		return tasks;
	}

	public void setTasks(List<IdmScheduledTaskType> tasks) {
		this.tasks = tasks;
	}

}
