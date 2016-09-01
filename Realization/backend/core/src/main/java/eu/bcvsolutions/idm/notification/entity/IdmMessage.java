package eu.bcvsolutions.idm.notification.entity;

import java.text.MessageFormat;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;

import eu.bcvsolutions.idm.core.model.domain.DefaultFieldLengths;

@Embeddable
public class IdmMessage {
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "subject", length = DefaultFieldLengths.NAME)
	private String subject;
	
	@Lob
	@Column(name = "text_message")
	private String textMessage;

	@Lob
	@Column(name = "html_message")
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
