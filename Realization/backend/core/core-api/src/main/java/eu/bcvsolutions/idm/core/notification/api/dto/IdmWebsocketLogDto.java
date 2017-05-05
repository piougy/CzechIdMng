package eu.bcvsolutions.idm.core.notification.api.dto;

import org.springframework.hateoas.core.Relation;

/**
 * Web socket log message dto
 *
 * @author Peter Sourek
 */
@Relation(collectionRelation = "websocketLogs")
public class IdmWebsocketLogDto extends IdmNotificationLogDto {

    private static final long serialVersionUID = 1L;

    public IdmWebsocketLogDto(IdmNotificationDto notification) {
        super(notification);
    }

    public IdmWebsocketLogDto() {

    }
}
