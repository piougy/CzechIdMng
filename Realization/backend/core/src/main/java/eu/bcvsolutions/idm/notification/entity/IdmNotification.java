package eu.bcvsolutions.idm.notification.entity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.model.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.model.entity.AbstractEntity;
import eu.bcvsolutions.idm.notification.domain.BaseNotification;

@Entity
@Table(name = "idm_notification")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class IdmNotification extends AbstractEntity implements BaseNotification {

	private static final long serialVersionUID = -2038771692205141212L;

	@Embedded
	private IdmMessage message;
	
	@JsonManagedReference
	@JoinColumn(name = "sender_id", referencedColumnName = "id")
	@ManyToOne(optional = true, cascade = CascadeType.ALL)
	private IdmNotificationRecipient sender;
	
	@JsonManagedReference
	@OneToMany(mappedBy = "notification", cascade = CascadeType.ALL) // orphan removal is not necessary - notification can be added only
	private List<IdmNotificationRecipient> recipients;
	
	@JsonProperty(access = Access.READ_ONLY)
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "sent")
	private Date sent;
	
	@JsonProperty(access = Access.READ_ONLY)
	@Size(max = DefaultFieldLengths.LOG)
	@Column(name = "SENT_LOG", length = DefaultFieldLengths.LOG)
	private String sentLog;
	
	@JsonIgnore
	@ManyToOne(optional = true)
	@JoinColumn(name = "parent_notification_id", referencedColumnName = "id")
	private IdmNotification parent;
	
	public void setSender(IdmNotificationRecipient sender) {
		this.sender = sender;
	}
	
	public IdmNotificationRecipient getSender() {
		return sender;
	}

	public void setRecipients(List<IdmNotificationRecipient> recipients) {
		this.recipients = recipients;
	}
	
	public List<IdmNotificationRecipient> getRecipients() {
		if (recipients == null) {
			recipients = new ArrayList<>();
		}
		return recipients;
	}

	public String getSentLog() {
		return sentLog;
	}

	public void setSentLog(String sentLog) {
		this.sentLog = sentLog;
	}
	
	public void setMessage(IdmMessage message) {
		this.message = message;
	}
	
	public IdmMessage getMessage() {
		return message;
	}
	
	public void setSent(Date sent) {
		this.sent = sent;
	}
	
	public Date getSent() {
		return sent;
	}
	
	public void setParent(IdmNotification parent) {
		this.parent = parent;
	}
	
	public IdmNotification getParent() {
		return parent;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("Notification [id:{2}] [message:{0}] [first recipient:{1}]", getMessage(), getRecipients().isEmpty() ? null : getRecipients().get(0), getId());
	}
}
