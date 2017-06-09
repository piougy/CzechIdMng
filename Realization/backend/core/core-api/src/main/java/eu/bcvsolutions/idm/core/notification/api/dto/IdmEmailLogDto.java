package eu.bcvsolutions.idm.core.notification.api.dto;

import org.springframework.hateoas.core.Relation;

/**
 * Email log message dto
 *
 * @author Peter Sourek
 */
@Relation(collectionRelation = "emails")
public class IdmEmailLogDto extends IdmNotificationLogDto {

    private static final long serialVersionUID = 1L;

    public IdmEmailLogDto(IdmNotificationDto notification) {
        super(notification);
    }

    public IdmEmailLogDto() {
        super();
    }
}
