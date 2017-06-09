package eu.bcvsolutions.idm.core.notification.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

import java.util.UUID;

public class NotificationRecipientFilter extends QuickFilter {

    private UUID notification;

    public UUID getNotification() {
        return notification;
    }

    public void setNotification(UUID notification) {
        this.notification = notification;
    }
}
