package eu.bcvsolutions.idm.core.notification.entity;

import java.text.MessageFormat;
import java.util.HashMap;
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

/**
 * Notification content
 * 
 * @author Radek Tomiška
 *
 */
@Embeddable
public class IdmMessage {
	
	public static final NotificationLevel DEFAULT_LEVEL = NotificationLevel.INFO;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "level", nullable = false, length = 45)
	private NotificationLevel level = DEFAULT_LEVEL;

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

	private IdmMessage(Builder builder) {
		if (builder.model != null) {
			model = builder.model;
			this.htmlMessage = builder.model.getMessage();
			this.textMessage = builder.model.getMessage();
			this.subject = builder.model.getStatusEnum();
			this.parameters = builder.model.getParameters();
			//
			// set level from model, override level
			if (model.getStatus().is5xxServerError()) {
				level = NotificationLevel.ERROR;
			} else if(model.getStatus().is2xxSuccessful()) {
				level = NotificationLevel.SUCCESS;
			} else {
				level = NotificationLevel.WARNING;
			}
		} else {
			if (builder.subject != null) {
				subject = builder.subject;
			} else if (builder.template != null && builder.template.getSubject() != null) {
				subject = builder.template.getSubject();
			}
			//
			if (builder.textMessage != null) {
				textMessage = builder.textMessage;
			} else if (builder.template != null && builder.template.getBodyText() != null) {
				textMessage = builder.template.getBodyText();
			}
			//
			if (builder.htmlMessage != null) {
				htmlMessage = builder.htmlMessage;
			} else if (builder.template != null && builder.template.getBodyHtml() != null) {
				htmlMessage = builder.template.getBodyHtml();
			}
			//
			template = builder.template;
			parameters = builder.parameters;
			model = builder.model;
			level = builder.level == null ? DEFAULT_LEVEL : builder.level;
		}
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

	/**
	 * {@link IdmMessage} builder
	 * 
	 * @author Radek Tomiška
	 *
	 */
	public static class Builder {

		private NotificationLevel level;
		private String subject;
		private String textMessage;
		private String htmlMessage;
		private ResultModel model;
		private IdmNotificationTemplate template;
		private Map<String, Object> parameters;
		
		public Builder() {
		}
		
		public Builder(NotificationLevel level) {
			this.level = level;
		}

		public Builder setLevel(NotificationLevel level) {
			this.level = level;
			return this;
		}

		public Builder setSubject(String subject) {
			this.subject = subject;
			return this;
		}

		public Builder setTextMessage(String textMessage) {
			this.textMessage = textMessage;
			return this;
		}

		public Builder setHtmlMessage(String htmlMessage) {
			this.htmlMessage = htmlMessage;
			return this;
		}
		
		public Builder setParameters(Map<String, Object> parameters) {
			this.parameters = parameters;
			return this;
		}
		
		public Builder addParameter(String key, Object value) {
			if (this.parameters == null) {
				this.parameters = new HashMap<>();
			}
			this.parameters.put(key, value);
			return this;
		}

		public Builder setTemplate(IdmNotificationTemplate template) {
			this.template = template;
			return this;
		}

		/**
		 * Sets all messages (text, html ...)
		 * @param message
		 * @return
		 */
		public Builder setMessage(String message) {
			this.textMessage = message;
			this.htmlMessage = message;
			return this;
		}

		public Builder setModel(ResultModel model) {
			this.model = model;
			if (model != null) {
				// set default subject and message
				if (StringUtils.isEmpty(subject)) {
					subject = model.getStatusEnum();
				}
				if (StringUtils.isEmpty(textMessage)) {
					textMessage = model.getMessage();
				}
				if (level == null) {
					if (model.getStatus().is5xxServerError()) {
						level = NotificationLevel.ERROR;
					} else if(model.getStatus().is2xxSuccessful()) {
						level = NotificationLevel.SUCCESS;
					} else {
						level = NotificationLevel.WARNING;
					}
				}
			}
			return this;
		}
	
		public IdmMessage build() {
			return new IdmMessage(this);
		}
	}
}
