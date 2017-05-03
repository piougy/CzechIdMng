package eu.bcvsolutions.idm.core.notification.api.dto;

import org.springframework.hateoas.core.Relation;

/**
 * Sms message dto
 *
 * @author Peter Sourek
 */
@Relation(collectionRelation = "smsLogs")
public class IdmSmsLogDto extends IdmNotificationLogDto {

    private static final long serialVersionUID = 1L;

    public IdmSmsLogDto(IdmNotificationDto notification) {
        super(notification);
    }

    public IdmSmsLogDto() {
        super();
    }
}
