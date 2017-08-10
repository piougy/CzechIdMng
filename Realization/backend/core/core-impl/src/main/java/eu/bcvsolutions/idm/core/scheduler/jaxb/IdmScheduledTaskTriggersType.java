package eu.bcvsolutions.idm.core.scheduler.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Triggers for scheduled task type.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@XmlRootElement(name = "triggers")
public class IdmScheduledTaskTriggersType {

	private List<IdmScheduledTaskTriggerType> triggers;

	@XmlElement(name="trigger", type = IdmScheduledTaskTriggerType.class)
	public List<IdmScheduledTaskTriggerType> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<IdmScheduledTaskTriggerType> triggers) {
		this.triggers = triggers;
	}	
}
