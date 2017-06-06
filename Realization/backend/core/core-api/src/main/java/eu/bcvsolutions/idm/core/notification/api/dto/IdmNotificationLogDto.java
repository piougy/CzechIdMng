package eu.bcvsolutions.idm.core.notification.api.dto;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.notification.api.domain.NotificationState;

/**
 * Generic log message dto
 *
 * @author Peter Sourek
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "notifications")
public class IdmNotificationLogDto extends IdmNotificationDto {

    private static final long serialVersionUID = 1L;
    @JsonProperty(access = Access.READ_ONLY)
    private NotificationState state;

    public IdmNotificationLogDto() {
    	
    }
    
    public IdmNotificationLogDto(IdmNotificationDto notification) {
        super(notification);
    }
    
    public void setState(NotificationState state) {
		this.state = state;
	}
    
    public NotificationState getState() {
		return state;
	}
}
