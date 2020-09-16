package eu.bcvsolutions.idm.core.notification.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonBackReference;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;

/**
 * Notification attachment.
 * Notification attachment support two modes:
 * - new attachment with input data and name can be given => attachment will be saved (owner = notification) and sent.
 * - persisted attachment (from custom owner) can be given => attachment input data will be load and sent.
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
@Relation(collectionRelation = "notificationAttachments")
public class IdmNotificationAttachmentDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	//
	@JsonBackReference
	@Embedded(dtoClass = IdmNotificationLogDto.class)
	private UUID notification;
	@Embedded(dtoClass = IdmAttachmentDto.class)
	private UUID attachment;
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String name;
	
	public UUID getNotification() {
		return notification;
	}
	
	public void setNotification(UUID notification) {
		this.notification = notification;
	}
	
	public UUID getAttachment() {
		return attachment;
	}
	
	public void setAttachment(UUID attachment) {
		this.attachment = attachment;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
