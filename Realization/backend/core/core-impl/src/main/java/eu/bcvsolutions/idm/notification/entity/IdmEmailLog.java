package eu.bcvsolutions.idm.notification.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import eu.bcvsolutions.idm.notification.service.api.EmailService;

@Entity
@Table(name = "idm_email_log")
public class IdmEmailLog extends IdmNotification {
	
	private static final long serialVersionUID = -6492542811469689133L;
	
	@Override
	public String getType() {
		return EmailService.NOTIFICATION_TYPE;
	}
	
	// TODO: attachments

}
