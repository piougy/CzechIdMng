package eu.bcvsolutions.idm.core.notification.api.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;

/**
 * Generic log message dto
 *
 * @author Peter Sourek
 */
@Relation(collectionRelation = "notifications")
public class IdmNotificationDto extends AbstractDto implements BaseNotification {

    private static final long serialVersionUID = -4130705393914057255L;

    private String topic;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private DateTime sent;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String sentLog;

    //TODO: can we use UUID with UuidToEntityConverter and embedded here?
    private List<IdmNotificationRecipientDto> recipients;
    @JsonProperty("parent")
    @Embedded(dtoClass = IdmNotificationLogDto.class)
    private UUID parent;
    @JsonProperty("message")
    private IdmMessageDto message;
    @JsonProperty("sender")
    @Embedded(dtoClass = IdmIdentityDto.class)
    private UUID identitySender;

    private String type;

    public IdmNotificationDto(IdmNotificationDto notification) {
        super(notification);
        topic = notification.getTopic();
        message = notification.getMessage();
        identitySender = notification.getIdentitySender();
        recipients = new ArrayList<>(notification.getRecipients());
        sent = notification.getSent();
        sentLog = notification.getSentLog();
        parent = notification.getParent();
        type = notification.getType();
    }

    public IdmNotificationDto() {
        super();
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public IdmMessageDto getMessage() {
        return message;
    }

    public void setMessage(IdmMessageDto message) {
        this.message = message;
    }

    @Override
    public UUID getIdentitySender() {
        return identitySender;
    }

    @Override
    public void setIdentitySender(UUID identitySender) {
        this.identitySender = identitySender;
    }

    @Override
    public List<IdmNotificationRecipientDto> getRecipients() {
        if (recipients == null) {
            recipients = new ArrayList<>();
        }
        return recipients;
    }

    @Override
    public void setRecipients(List<IdmNotificationRecipientDto> recipients) {
        this.recipients = recipients;
    }

    public DateTime getSent() {
        return sent;
    }

    public void setSent(DateTime sent) {
        this.sent = sent;
    }

    public String getSentLog() {
        return sentLog;
    }

    public void setSentLog(String sentLog) {
        this.sentLog = sentLog;
    }

    public UUID getParent() {
        return parent;
    }

    public void setParent(UUID parent) {
        this.parent = parent;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
