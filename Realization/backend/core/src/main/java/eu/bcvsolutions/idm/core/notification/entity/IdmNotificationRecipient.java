package eu.bcvsolutions.idm.core.notification.entity;

import java.text.MessageFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.model.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.model.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

@Entity
@Table(name = "idm_notification_recipient")
public class IdmNotificationRecipient extends AbstractEntity {

	private static final long serialVersionUID = 6041589660726734115L;

	@JsonIgnore
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "notification_id", referencedColumnName = "id")
	private IdmNotification notification;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "identity_recipient_id", referencedColumnName = "id")
	private IdmIdentity identityRecipient;
	
	@Size(max = DefaultFieldLengths.EMAIL_ADDRESS)
	@Column(name = "real_recipient", length = DefaultFieldLengths.EMAIL_ADDRESS) // maximum is email address?
	private String realRecipient; // raw email,. sms, identity id ...

	public IdmNotificationRecipient() {
	}
	
	public IdmNotificationRecipient(IdmNotification notification, IdmIdentity identity) {
		this(notification, identity, null);
	}
	
	public IdmNotificationRecipient(IdmNotification notification, String recipient) {
		this(notification, null, recipient);
	}
	
	public IdmNotificationRecipient(IdmNotification notification, IdmIdentity identity, String recipient) {
		this.notification = notification;
		this.identityRecipient = identity;
		this.realRecipient = recipient;
	}
	
	public IdmNotification getNotification() {
		return notification;
	}

	public void setNotification(IdmNotification notification) {
		this.notification = notification;
	}

	public IdmIdentity getIdentityRecipient() {
		return identityRecipient;
	}

	public void setIdentityRecipient(IdmIdentity identityRecipient) {
		this.identityRecipient = identityRecipient;
	}

	public String getRealRecipient() {
		return realRecipient;
	}

	public void setRealRecipient(String realRecipient) {
		this.realRecipient = realRecipient;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("identity [{0}], real [{1}]", identityRecipient == null ? null : identityRecipient.getUsername(), realRecipient);
	}
}
