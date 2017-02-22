package eu.bcvsolutions.idm.core.notification.entity;

import java.text.MessageFormat;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;

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

	public IdmMessage() {
	}

	private IdmMessage(Builder builder) {
		level = builder.level == null ? DEFAULT_LEVEL : builder.level;
		subject = builder.subject;
		textMessage = builder.textMessage;
		htmlMessage = builder.htmlMessage;
		model = builder.model;
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
