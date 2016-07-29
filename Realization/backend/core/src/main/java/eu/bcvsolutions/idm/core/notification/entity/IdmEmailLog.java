package eu.bcvsolutions.idm.core.notification.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "idm_email_log")
public class IdmEmailLog extends IdmNotification {
	
	private static final long serialVersionUID = -6492542811469689133L;
	
	// TODO: attachments

}
