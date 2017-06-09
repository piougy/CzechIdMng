package eu.bcvsolutions.idm.core.notification.api.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import org.springframework.hateoas.core.Relation;

import java.util.UUID;

/**
 * Message recipient DTO
 *
 * @author Peter Sourek
 */
@Relation(collectionRelation = "recipients")
public class IdmNotificationRecipientDto extends AbstractDto {

	private static final long serialVersionUID = -752269549571124786L;

	@JsonBackReference
	@Embedded(dtoClass = IdmNotificationLogDto.class)
	private UUID notification;
	@JsonProperty("identityRecipient")
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID identityRecipient;
	private String realRecipient;

	public IdmNotificationRecipientDto(UUID identity) {
		this(null, identity, null);
	}

	public IdmNotificationRecipientDto(String recipient) {
		this(null, null, recipient);
	}

	public IdmNotificationRecipientDto(UUID notification, UUID identity) {
		this(notification, identity, null);
	}

	public IdmNotificationRecipientDto(UUID notification, String recipient) {
		this(notification, null, recipient);
	}

	public IdmNotificationRecipientDto(UUID notification, UUID identity, String realRecipient) {
		this.notification = notification;
		this.identityRecipient = identity;
		this.realRecipient = realRecipient;
	}

	public IdmNotificationRecipientDto() {
	}

	public UUID getNotification() {
		return notification;
	}

	public void setNotification(UUID notification) {
		this.notification = notification;
	}

	public UUID getIdentityRecipient() {
		return identityRecipient;
	}

	public void setIdentityRecipient(UUID identityRecipient) {
		this.identityRecipient = identityRecipient;
	}

	public String getRealRecipient() {
		return realRecipient;
	}

	public void setRealRecipient(String realRecipient) {
        this.realRecipient = realRecipient;
    }
}
