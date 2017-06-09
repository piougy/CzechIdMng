package eu.bcvsolutions.idm.core.notification.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * Common message properties for notification system
 *
 * @author Radek Tomiška
 */
public interface BaseNotification {

    /**
     * Notification type - email, notification, websocket etc.
     *
     * @return
     */
    String getType();

    /**
     * Notification topic
     *
     * @return
     */
    String getTopic();

    /**
     * Notification topic
     *
     * @param topic
     */
    void setTopic(String topic);

    /**
     * Notification sender - could be filled, when notification is send from
     * some identity
     *
     * @param identitySender
     */
    void setIdentitySender(UUID identitySender);

    /**
     * Notification sender - could be filled, when notification is send from
     * some identity
     *
     * @return
     */
    UUID getIdentitySender();

    /**
     * Notification recipients
     *
     * @param recipients
     */
    void setRecipients(List<IdmNotificationRecipientDto> recipients);

    /**
     * Notification recipients
     *
     * @return
     */
    List<IdmNotificationRecipientDto> getRecipients();

    /**
     * Sent message
     *
     * @param message
     */
    void setMessage(IdmMessageDto message);

    /**
     * Sent message
     *
     * @return
     */
    IdmMessageDto getMessage();
}
