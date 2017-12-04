package eu.bcvsolutions.idm.acc.entity;

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

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

/**
 * Persisted "active" provisioning operation. Any operation has batch and operation result.
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "sys_provisioning_operation", indexes = {
		@Index(name = "idx_sys_p_o_created", columnList = "created"),
		@Index(name = "idx_sys_p_o_operation_type", columnList = "operation_type"),
		@Index(name = "idx_sys_p_o_system", columnList = "system_id"),
		@Index(name = "idx_sys_p_o_entity_type", columnList = "entity_type"),
		@Index(name = "idx_sys_p_o_sys_entity", columnList = "system_entity_id"),
		@Index(name = "idx_sys_p_o_entity_identifier", columnList = "entity_identifier"),
		@Index(name = "idx_sys_pro_oper_batch_id", columnList = "provisioning_batch_id")
		})
public class SysProvisioningOperation extends AbstractEntity {

	private static final long serialVersionUID = -6191740329296942394L;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "operation_type", nullable = false)
	private ProvisioningEventType operationType;
	
	@NotNull
	@Column(name = "provisioning_context", length = Integer.MAX_VALUE, nullable = false)
	private ProvisioningContext provisioningContext; 
	
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private SysSystem system;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "entity_type", nullable = false)
	private SystemEntityType entityType;
	
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_entity_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private SysSystemEntity systemEntity;
	
	@Column(name = "entity_identifier")
	private UUID entityIdentifier;
	
	@Column(name = "current_attempt")
	private int currentAttempt = 0;

	@Column(name = "max_attempts")
	private int maxAttempts;

	@Embedded
	private OperationResult result;

	@ManyToOne
	@JoinColumn(name = "provisioning_batch_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private SysProvisioningBatch batch;

	public ProvisioningEventType getOperationType() {
		return operationType;
	}

	public void setOperationType(ProvisioningEventType operationType) {
		this.operationType = operationType;
	}
	
	public SysSystem getSystem() {
		return system;
	}
	
	public void setSystem(SysSystem system) {
		this.system = system;
	}

	public SystemEntityType getEntityType() {
		return entityType;
	}	
	
	public void setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
	}
	
	public SysSystemEntity getSystemEntity() {
		return systemEntity;
	}

	public void setSystemEntity(SysSystemEntity systemEntity) {
		this.systemEntity = systemEntity;
	}

	public UUID getEntityIdentifier() {
		return entityIdentifier;
	}

	public void setEntityIdentifier(UUID entityIdentifier) {
		this.entityIdentifier = entityIdentifier;
	}
	
	public ProvisioningContext getProvisioningContext() {
		return provisioningContext;
	}
	
	public void setProvisioningContext(ProvisioningContext provisioningContext) {
		this.provisioningContext = provisioningContext;
	}
	
	public int getCurrentAttempt() {
		return currentAttempt;
	}

	public void setCurrentAttempt(int attempt) {
		this.currentAttempt = attempt;
	}

	public void increaseAttempt() {
		this.currentAttempt++;
	}

	public int getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public SysProvisioningBatch getBatch() {
		return batch;
	}

	public void setBatch(SysProvisioningBatch batch) {
		this.batch = batch;
	}
	
	public OperationResult getResult() {
		return result;
	}
	
	public void setResult(OperationResult result) {
		this.result = result;
	}
	
	public String getSystemEntityUid() {
		if(this.systemEntity != null) {
			return this.systemEntity.getUid();
		}
		return null;
	}
	
	public DateTime getNextAttempt() {
		if (batch == null) {
			return null;
		}
		return batch.getNextAttempt();
	}
}
