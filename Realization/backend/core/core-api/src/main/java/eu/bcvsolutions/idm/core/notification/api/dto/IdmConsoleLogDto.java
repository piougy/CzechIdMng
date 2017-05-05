package eu.bcvsolutions.idm.core.notification.api.dto;

import org.springframework.hateoas.core.Relation;

/**
 * Generic log message dto
 *
 * @author Peter Sourek
 */
@Relation(collectionRelation = "consoleLogs")
public class IdmConsoleLogDto extends IdmNotificationLogDto {

    private static final long serialVersionUID = 1L;

    public IdmConsoleLogDto(IdmNotificationDto notification) {
        super(notification);
    }

    public IdmConsoleLogDto() {
        super();
    }
}
