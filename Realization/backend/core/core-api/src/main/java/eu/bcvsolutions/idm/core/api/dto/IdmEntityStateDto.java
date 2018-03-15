package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;

/**
 * Persists entity states, when entity is changed (e.g. by event processing). One entity change can have
 * more states - depends on registered task, which changes and persist this state and result.
 * 
 * @author Radek Tomiška
 * @since 8.0.0
 */
@Relation(collectionRelation = "entityStates")
public class IdmEntityStateDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	//
	@NotEmpty
	private String ownerType;
	@NotNull
	private UUID ownerId;
	@Embedded(dtoClass = IdmEntityEventDto.class)
	private UUID event;
	private Integer processedOrder;
	@Size(max = DefaultFieldLengths.NAME)
	private String processorId; // bean name / identifier (spring bean name or other identifier)
	@Size(max = DefaultFieldLengths.NAME)
	private String processorModule;
	@Size(max = DefaultFieldLengths.NAME)
	private String processorName; // component name - given name e.g. save-identity-processor
	private boolean closed;
	private boolean suspended;
	@NotNull
	private String instanceId;
	private OperationResultDto result;
	
	public IdmEntityStateDto() {
	}
	
	public IdmEntityStateDto(IdmEntityEventDto event) {
		if (event == null) {
			this.event = null;
			this.ownerType = null;
			this.ownerId = null;
			this.instanceId = null;
		} else {
			this.event = event.getId();
			this.ownerType = event.getOwnerType();
			this.ownerId = event.getOwnerId();
			this.instanceId = event.getInstanceId();
		}
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
	
	public UUID getEvent() {
		return event;
	}
	
	public void setEvent(UUID event) {
		this.event = event;
	}

	public OperationResultDto getResult() {
		return result;
	}

	public void setResult(OperationResultDto result) {
		this.result = result;
	}

	public Integer getProcessedOrder() {
		return processedOrder;
	}

	public void setProcessedOrder(Integer processedOrder) {
		this.processedOrder = processedOrder;
	}

	public String getProcessorId() {
		return processorId;
	}

	public void setProcessorId(String processorId) {
		this.processorId = processorId;
	}

	public String getProcessorModule() {
		return processorModule;
	}

	public void setProcessorModule(String processorModule) {
		this.processorModule = processorModule;
	}

	public String getProcessorName() {
		return processorName;
	}

	public void setProcessorName(String processorName) {
		this.processorName = processorName;
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
}
