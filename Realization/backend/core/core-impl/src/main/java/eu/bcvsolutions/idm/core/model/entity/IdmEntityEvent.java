package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

/**
 * Persisted entity event
 * 
 * @since 8.0.0
 * @author Radek Tomiška
 * 
 */
@Entity
@Table(name = "idm_entity_event", indexes = {
		@Index(name = "idx_idm_entity_event_o_id", columnList = "owner_id"),
		@Index(name = "idx_idm_entity_event_so_id", columnList = "super_owner_id"),
		@Index(name = "idx_idm_entity_event_o_type", columnList = "owner_type"),
		@Index(name = "idx_idm_entity_event_created", columnList = "created"),
		@Index(name = "idx_idm_entity_event_exe", columnList = "execute_date"),
		@Index(name = "idx_idm_entity_event_inst", columnList = "instance_id"),
		@Index(name = "idx_idm_entity_event_root", columnList = "root_id")})
public class IdmEntityEvent extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "owner_type", length = DefaultFieldLengths.NAME, nullable = false)
	private String ownerType;
	
	@NotNull
	@Column(name = "owner_id", length = 16, nullable = false)
	private UUID ownerId;
	
	@Column(name = "super_owner_id", length = 16)
	private UUID superOwnerId;
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "event_type", length = DefaultFieldLengths.NAME)
	private String eventType; // event type
	
	@Column(name = "properties", length = Integer.MAX_VALUE)
	private ConfigurationMap properties;
	
	@Column(name = "content", length = Integer.MAX_VALUE)
	private Identifiable content; // content - in current processing
	
	@Column(name = "original_source", length = Integer.MAX_VALUE)
	private Identifiable originalSource; // persisted content - before event starts
	
	@Column(name = "processed_order")
	private Integer processedOrder;
	
	@NotNull
	@Column(name = "closed", nullable = false)
	private boolean closed;
	
	@NotNull
	@Column(name = "suspended", nullable = false)
	private boolean suspended;
	
	@Column(name = "execute_date")
	private DateTime executeDate;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "priority", nullable = false, length = DefaultFieldLengths.ENUMARATION)
	private PriorityType priority;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "parent_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmEntityEvent parent; // parent event
	
	@Column(name = "root_id")
	private UUID rootId; // root event
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "parent_event_type", length = DefaultFieldLengths.NAME)
	private String parentEventType; // parent event type
	
	@NotNull
	@Column(name = "instance_id", length = DefaultFieldLengths.NAME, nullable = false)
	private String instanceId;
	
	@Embedded
	private OperationResult result;

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
	
	public UUID getSuperOwnerId() {
		return superOwnerId;
	}
	
	public void setSuperOwnerId(UUID superOwnerId) {
		this.superOwnerId = superOwnerId;
	}

	public DateTime getExecuteDate() {
		return executeDate;
	}

	public void setExecuteDate(DateTime executeDate) {
		this.executeDate = executeDate;
	}

	public IdmEntityEvent getParent() {
		return parent;
	}

	public void setParent(IdmEntityEvent parent) {
		this.parent = parent;
	}
	
	public OperationResult getResult() {
		return result;
	}
	
	public void setResult(OperationResult result) {
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
	
	public void setRootId(UUID rootId) {
		this.rootId = rootId;
	}
	
	public UUID getRootId() {
		return rootId;
	}
}
