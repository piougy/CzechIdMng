package eu.bcvsolutions.idm.core.notification.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.IdentifiableByName;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;

/**
 * Entity that store email/text templates for apache velocity
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "idm_notification_template", indexes = { 
		@Index(name = "ux_idm_notification_template_name", columnList = "name", unique = true),
		@Index(name = "ux_idm_notification_template_code", columnList = "code", unique = true)})
public class IdmNotificationTemplate extends AbstractEntity implements IdentifiableByName {

	private static final long serialVersionUID = 4978924621333160086L;
	
	@Audited
	@NotNull
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "name")
	private String name;
	
	@Audited
	@NotNull
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "code")
	private String code;
	
	@Audited
	@NotNull
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "subject")
	private String subject;
	
	@NotNull
	@Audited
	@Type(type = "org.hibernate.type.StringClobType")
	@Column(name = "body")
	private String body;
	
	// TODO: remove?
	@Audited
	@Column(name = "timestamp") 
	private DateTime timestamp;
	
	@NotNull
	@Audited
	@Enumerated(EnumType.STRING)
	@Column(name = "level", nullable = false, length = 45)
	private NotificationLevel level = IdmMessage.DEFAULT_LEVEL;
	
	@Override
	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public String getBody() {
		return body;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}

	public String getSubject() {
		return subject;
	}

	public NotificationLevel getLevel() {
		return level;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setLevel(NotificationLevel level) {
		this.level = level;
	}
}
