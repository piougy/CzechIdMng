package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;

/**
 * Persisted entity event
 * 
 * @author Radek Tomiška
 * @since 8.0.0
 */
@Relation(collectionRelation = "entityEvents")
public class IdmEntityEventDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	//
	@NotEmpty
	private String ownerType;
	@NotNull
	private UUID ownerId;
	@Size(max = DefaultFieldLengths.NAME)
	private String eventType; // persisted event type
	private ConfigurationMap properties;
	private Identifiable content; // content - in current processing
	private Identifiable originalSource; // persisted content - before event starts
	private Integer processedOrder;
	private boolean closed;
	private boolean suspended;
	private DateTime executeDate;
	private PriorityType priority;
	@Embedded(dtoClass = IdmEntityEventDto.class)
	private UUID parent;
	private String parentEventType; // parent event type
	@NotNull
	private String instanceId;
	private OperationResultDto result;
	
	public IdmEntityEventDto() {
	}
	
	public IdmEntityEventDto(UUID id) {
		super(id);
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}

	public DateTime getExecuteDate() {
		return executeDate;
	}

	public void setExecuteDate(DateTime executeDate) {
		this.executeDate = executeDate;
	}

	public UUID getParent() {
		return parent;
	}

	public void setParent(UUID parent) {
		this.parent = parent;
	}
	
	public OperationResultDto getResult() {
		return result;
	}
	
	public void setResult(OperationResultDto result) {
		this.result = result;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getParentEventType() {
		return parentEventType;
	}
	
	public void setParentEventType(String parentEventType) {
		this.parentEventType = parentEventType;
	}

	public Identifiable getContent() {
		return content;
	}

	public void setContent(Identifiable content) {
		this.content = content;
	}

	public Identifiable getOriginalSource() {
		return originalSource;
	}

	public void setOriginalSource(Identifiable originalSource) {
		this.originalSource = originalSource;
	}

	public Integer getProcessedOrder() {
		return processedOrder;
	}

	public void setProcessedOrder(Integer processedOrder) {
		this.processedOrder = processedOrder;
	}
	
	public ConfigurationMap getProperties() {
		if (properties == null) {
			properties = new ConfigurationMap();
		}
		return properties;
	}

	public void setProperties(ConfigurationMap properties) {
		this.properties = properties;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}
	
	public String getInstanceId() {
		return instanceId;
	}
	
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	public PriorityType getPriority() {
		return priority;
	}
	
	public void setPriority(PriorityType priority) {
		this.priority = priority;
	}
}
