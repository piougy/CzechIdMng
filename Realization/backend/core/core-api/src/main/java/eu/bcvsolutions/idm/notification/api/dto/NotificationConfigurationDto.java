package eu.bcvsolutions.idm.notification.api.dto;

import eu.bcvsolutions.idm.notification.api.domain.NotificationLevel;

/**
 * Notification configuration
 * 
 * @author Radek Tomiška
 *
 */
public class NotificationConfigurationDto {
	
	private String topic;
	private NotificationLevel level;
	private String notificationType;
	
	public NotificationConfigurationDto() {
	}
	
	public NotificationConfigurationDto(String topic, NotificationLevel level, String notificationType) {
		this.topic = topic;
		this.level = level;
		this.notificationType = notificationType;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public NotificationLevel getLevel() {
		return level;
	}

	public void setLevel(NotificationLevel level) {
		this.level = level;
	}

	public String getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}
}
