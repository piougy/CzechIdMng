package eu.bcvsolutions.idm.notification.entity;

import java.text.MessageFormat;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;

@Embeddable
public class IdmMessage {
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "subject", length = DefaultFieldLengths.NAME)
	private String subject;

	@Column(name = "text_message")
	@Type(type = "org.hibernate.type.StringClobType") // TODO: test on oracle/ mysql
	private String textMessage;

	@Column(name = "html_message")
	@Type(type = "org.hibernate.type.StringClobType") // TODO: test on oracle/ mysql
	private String htmlMessage;
	
	public IdmMessage(String subject, String message) {
		this.subject = subject;
		this.textMessage = message;
		this.htmlMessage = message;
	}
	
	public IdmMessage(String subject, String textMessage, String htmlMessage) {
		this.subject = subject;
		this.textMessage = textMessage;
		this.htmlMessage = htmlMessage;
	}
	
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
	
	@Override
	public String toString() {
		if(StringUtils.equals(textMessage, htmlMessage)) {
			return MessageFormat.format("subject [{0}], message [{1}]", subject, textMessage);
		}
		return MessageFormat.format("subject [{0}], text [{1}], html [{2}]", subject, textMessage, htmlMessage);
	}
}
