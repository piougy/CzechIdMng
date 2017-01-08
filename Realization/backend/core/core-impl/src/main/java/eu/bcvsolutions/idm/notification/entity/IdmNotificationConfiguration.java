package eu.bcvsolutions.idm.notification.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Configuration for notification routing
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_notification_configuration", indexes = {
		@Index(name = "ux_idm_not_conf", columnList = "topic,notification_type", unique = true),
		@Index(name = "idx_idm_not_conf_topic", columnList = "topic"),
		@Index(name = "idx_idm_not_conf_type", columnList = "notification_type")
		})
public class IdmNotificationConfiguration extends AbstractEntity {

	private static final long serialVersionUID = 3131809487111061022L;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "topic", nullable = false, length = DefaultFieldLengths.NAME)
	private String topic; // TODO: should be defined in module descriptor as supported topics
	
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "notification_type", nullable = false, length = DefaultFieldLengths.NAME)
	private String notificationType;

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}
}
