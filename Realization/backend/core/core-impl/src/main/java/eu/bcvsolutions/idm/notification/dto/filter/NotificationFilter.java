package eu.bcvsolutions.idm.notification.dto.filter;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.notification.domain.NotificationState;

/**
 * Filter for notifications (notification, email ...)
 * 
 * @author Radek Tomiška
 *
 */
public class NotificationFilter extends QuickFilter {

	private String sender;
	private String recipient;
	private NotificationState state;
	private DateTime from;
	private DateTime till;
	private Boolean sent;

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public DateTime getFrom() {
		return from;
	}

	public void setFrom(DateTime from) {
		this.from = from;
	}

	public DateTime getTill() {
		return till;
	}

	public void setTill(DateTime till) {
		this.till = till;
	}
	
	public NotificationState getState() {
		return state;
	}
	
	public void setState(NotificationState state) {
		this.state = state;
	}
	
	public Boolean getSent() {
		return sent;
	}
	
	public void setSent(Boolean sent) {
		this.sent = sent;
	}
}
