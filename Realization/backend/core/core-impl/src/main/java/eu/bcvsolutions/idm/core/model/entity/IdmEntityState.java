package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

/**
 * Persists entity states, when entity is changed.
 * One entity change can have more states - depends on registered task, which changes and persist this state and result.
 * Entity can have their own state (without event change).
 * State can be persisted by event processor - each processor can save more results.
 * 
 * @since 8.0.0
 * @author Radek Tomiška
 * 
 */
@Entity
@Table(name = "idm_entity_state", indexes = {
		@Index(name = "idx_idm_entity_state_o_id", columnList = "owner_id"),
		@Index(name = "idx_idm_entity_state_o_type", columnList = "owner_type"),
		@Index(name = "idx_idm_entity_state_event", columnList = "event_id")})
public class IdmEntityState extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "owner_type", length = DefaultFieldLengths.NAME, nullable = false)
	private String ownerType;
	
	@NotNull
	@Column(name = "owner_id", length = 16, nullable = false)
	private UUID ownerId;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "event_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmEntityEvent event;
	
	@Column(name = "processed_order")
	private Integer processedOrder;
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "processor_id", length = DefaultFieldLengths.NAME)
	private String processorId; // bean name / identifier (spring bean name or other identifier)
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "processor_module", length = DefaultFieldLengths.NAME)
	private String processorModule;
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "processor_name", length = DefaultFieldLengths.NAME)
	private String processorName; // component name - given name e.g. save-identity-processor
	
	@NotNull
	@Column(name = "closed", nullable = false)
	private boolean closed;
	
	@NotNull
	@Column(name = "suspended", nullable = false)
	private boolean suspended;
	
	@NotNull
	@Column(name = "instance_id", length = DefaultFieldLengths.NAME, nullable = false)
	private String instanceId;
	
	@Embedded
	private OperationResult result;

	public IdmEntityEvent getEvent() {
		return event;
	}
	
	public void setEvent(IdmEntityEvent event) {
		this.event = event;
	}

	public OperationResult getResult() {
		return result;
	}

	public void setResult(OperationResult result) {
		this.result = result;
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
