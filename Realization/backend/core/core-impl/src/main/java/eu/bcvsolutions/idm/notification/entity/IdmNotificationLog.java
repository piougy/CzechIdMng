package eu.bcvsolutions.idm.notification.entity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * Notification envelope - its just log - real sending is provided by related notifications.
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_notification_log")
public class IdmNotificationLog extends IdmNotification {

	private static final long serialVersionUID = -3022005218384947356L;
	public static final String NOTIFICATION_TYPE = "notification";
	
	@JsonProperty(access = Access.READ_ONLY)
	@OneToMany(mappedBy = "parent")
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private List<IdmNotification> relatedNotifications;
	
	@Override
	public String getType() {
		return NOTIFICATION_TYPE;
	}
	
	public List<IdmNotification> getRelatedNotifications() {
		if (relatedNotifications == null) {
			relatedNotifications = new ArrayList<>();
		}
		return relatedNotifications;
	}
	
	public void setRelatedNotifications(List<IdmNotification> relatedNotifications) {
		this.relatedNotifications = relatedNotifications;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("Notification message [{0}] with topic [{1}] to recipient [first:{2}]", getMessage(), getTopic(), getRecipients().isEmpty() ? null : getRecipients().get(0));
	}
}
