package eu.bcvsolutions.idm.core.notification.api.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

import java.util.UUID;

/**
 * Notification recipients
 * 
 * @author Petr Šourek
 */
public class IdmNotificationRecipientFilter extends QuickFilter {

    private UUID notification;

    public UUID getNotification() {
        return notification;
    }

    public void setNotification(UUID notification) {
        this.notification = notification;
    }
}
