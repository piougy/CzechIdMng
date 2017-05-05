package eu.bcvsolutions.idm.core.notification.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 *
 * @author Peter Sourek
 */
@Entity
@Table(name = "idm_notification_sms")
public class IdmSmsLog extends IdmNotificationLog {

    private static final Long serialVersionUID = -2038485392205141212L;
    public static final String NOTIFICATION_TYPE = "sms";

    @Override
    public String getType() {
        return NOTIFICATION_TYPE;
    }
}
