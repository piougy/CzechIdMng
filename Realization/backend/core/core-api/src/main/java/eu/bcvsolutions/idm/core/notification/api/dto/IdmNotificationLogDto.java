package eu.bcvsolutions.idm.core.notification.api.dto;

import org.springframework.hateoas.core.Relation;

/**
 * Generic log message dto
 *
 * @author Peter Sourek
 */
@Relation(collectionRelation = "notifications")
public class IdmNotificationLogDto extends IdmNotificationDto {

    private static final long serialVersionUID = 1L;

    public IdmNotificationLogDto(IdmNotificationDto notification) {
        super(notification);
    }


    public IdmNotificationLogDto() {
        super();
    }

}
