package eu.bcvsolutions.idm.core.notification.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fasterxml.jackson.annotation.JsonBackReference;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.ecm.entity.IdmAttachment;

/**
 * Notification attachment.
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
@Entity
@Table(name = "idm_notification_attachment", indexes = {
		@Index(name = "idx_idm_notification_att_not", columnList = "notification_id"),
		@Index(name = "idx_idm_notification_att_att", columnList = "attachment_id")
		})
public class IdmNotificationAttachment extends AbstractEntity {

	private static final long serialVersionUID = 6041589660726734115L;

	@NotNull
	@JsonBackReference
	@ManyToOne(optional = false)
	@JoinColumn(name = "notification_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmNotification notification;
	
	@ManyToOne(optional = true)
	@NotFound(action = NotFoundAction.IGNORE)
	@JoinColumn(name = "attachment_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmAttachment attachment;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Basic(optional = false)
	@Column(name = "name", nullable = false, length = DefaultFieldLengths.NAME)
	private String name; // = attachment name - in case attachment is deleted somehow (ecm storage cleanup etc.)

	public IdmNotification getNotification() {
		return notification;
	}

	public void setNotification(IdmNotification notification) {
		this.notification = notification;
	}

	public IdmAttachment getAttachment() {
		return attachment;
	}

	public void setAttachment(IdmAttachment attachment) {
		this.attachment = attachment;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
