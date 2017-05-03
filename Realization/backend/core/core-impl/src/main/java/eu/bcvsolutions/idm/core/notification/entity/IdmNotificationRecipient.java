package eu.bcvsolutions.idm.core.notification.entity;

import java.text.MessageFormat;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonBackReference;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Notification recipient
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_notification_recipient", indexes = {
		@Index(name = "idx_idm_notification_rec_not", columnList = "notification_id"),
		@Index(name = "idx_idm_notification_rec_idnt", columnList = "identity_recipient_id")
		})
public class IdmNotificationRecipient extends AbstractEntity {

	private static final long serialVersionUID = 6041589660726734115L;

	@NotNull
	@JsonBackReference
	@ManyToOne(optional = false)
	@JoinColumn(name = "notification_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmNotification notification;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "identity_recipient_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentity identityRecipient;
	
	@Size(max = DefaultFieldLengths.EMAIL_ADDRESS)
	@Column(name = "real_recipient", length = DefaultFieldLengths.EMAIL_ADDRESS) // maximum is email address?
	private String realRecipient; // raw email,. sms, identity id ...

	public IdmNotificationRecipient() {
	}
	
	public IdmNotificationRecipient(IdmIdentity identity) {
		this(null, identity, null);
	}
	
	public IdmNotificationRecipient(String recipient) {
		this(null, null, recipient);
	}
	
	public IdmNotificationRecipient(IdmNotification notification, IdmIdentity identity) {
		this(notification, identity, null);
	}
	
	public IdmNotificationRecipient(IdmNotification notification, String recipient) {
		this(notification, null, recipient);
	}
	
	public IdmNotificationRecipient(IdmNotification notification, IdmIdentity identity, String realRecipient) {
		this.notification = notification;
		this.identityRecipient = identity;
		this.realRecipient = realRecipient;
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
