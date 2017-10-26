package eu.bcvsolutions.idm.core.notification.entity;

import java.text.MessageFormat;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;

/**
 * Notification content
 * 
 * @author Radek Tomiška
 *
 */
@Embeddable
public class IdmMessage {
	
	@Deprecated
	public static final NotificationLevel DEFAULT_LEVEL = IdmMessageDto.DEFAULT_LEVEL;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "level", nullable = false, length = 45)
	private NotificationLevel level = IdmMessageDto.DEFAULT_LEVEL;

	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "subject", length = DefaultFieldLengths.NAME)
	private String subject;

	@Column(name = "text_message")
	@Type(type = "org.hibernate.type.StringClobType")
	private String textMessage;

	@Column(name = "html_message")
	@Type(type = "org.hibernate.type.StringClobType")
	private String htmlMessage;

	@Column(name = "result_model", length = Integer.MAX_VALUE)
	private ResultModel model;
	
	@JsonProperty(access = Access.READ_ONLY)
	@ManyToOne(optional = true)
	@JoinColumn(name = "notification_template_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmNotificationTemplate template;
	
	@JsonIgnore
	@Transient
	private transient Map<String, Object> parameters;

	public IdmMessage() {
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getTextMessage() {
		return textMessage;
	}

	public void setTextMessage(String textMessage) {
		this.textMessage = textMessage;
	}

	public String getHtmlMessage() {
		return htmlMessage;
	}

	public void setHtmlMessage(String htmlMessage) {
		this.htmlMessage = htmlMessage;
	}

	public void setModel(ResultModel model) {
		this.model = model;
	}
	
	public ResultModel getModel() {
		return model;
	}

	public NotificationLevel getLevel() {
		return level;
	}

	public void setLevel(NotificationLevel level) {
		this.level = level;
	}

	public IdmNotificationTemplate getTemplate() {
		return template;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setTemplate(IdmNotificationTemplate template) {
		this.template = template;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		if (StringUtils.equals(textMessage, htmlMessage)) {
			return MessageFormat.format("{0}: subject [{1}], message [{2}]", level, subject, textMessage);
		}
		return MessageFormat.format("{0}: subject [{1}], text [{2}], html [{3}]", level, subject, textMessage, htmlMessage);
	}
}
