package eu.bcvsolutions.idm.core.notification.api.dto;

import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;

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
	private String description;
	private String notificationTemplateCode;
	
	public NotificationConfigurationDto() {
	}
	
	public NotificationConfigurationDto(String topic, NotificationLevel level, String notificationType, String description, String notificationTemplateCode) {
		this.topic = topic;
		this.level = level;
		this.notificationType = notificationType;
		this.description = description;
		this.notificationTemplateCode  = notificationTemplateCode;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNotificationTemplateCode() {
		return notificationTemplateCode;
	}

	public void setNotificationTemplateCode(String notificationTemplateCode) {
		this.notificationTemplateCode = notificationTemplateCode;
	}
}
