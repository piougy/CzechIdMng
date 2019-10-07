package eu.bcvsolutions.idm.core.scheduler.jaxb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.time.ZonedDateTime;

/**
 * One instace of trigger for scheduled task type.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@XmlRootElement(name = "trigger")
public class IdmScheduledTaskTriggerType {

	private String type;
	private String description;
	private ZonedDateTime nextFireTime;
	private ZonedDateTime previousFireTime;
	private String state;
	private Long fireTime;
	private String cron;

	@XmlElement(required = true, type = String.class)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlElement(required = false, type = String.class)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@XmlElement(required = false, type = ZonedDateTime.class)
	public ZonedDateTime getNextFireTime() {
		return nextFireTime;
	}

	public void setNextFireTime(ZonedDateTime nextFireTime) {
		this.nextFireTime = nextFireTime;
	}

	@XmlElement(required = false, type = ZonedDateTime.class)
	public ZonedDateTime getPreviousFireTime() {
		return previousFireTime;
	}

	public void setPreviousFireTime(ZonedDateTime previousFireTime) {
		this.previousFireTime = previousFireTime;
	}

	@XmlElement(required = false, type = String.class)
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@XmlElement(required = false, type = Long.class)
	public Long getFireTime() {
		return fireTime;
	}

	public void setFireTime(Long fireTime) {
		this.fireTime = fireTime;
	}

	@XmlElement(required = false, type = String.class)
	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

}
