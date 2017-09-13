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

import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

/**
 * Persisted archived provisioning operation 
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "sys_provisioning_archive", indexes = {
		@Index(name = "idx_sys_p_o_arch_created", columnList = "created"),
		@Index(name = "idx_sys_p_o_arch_operation_type", columnList = "operation_type"),
		@Index(name = "idx_sys_p_o_arch_system", columnList = "system_id"),
		@Index(name = "idx_sys_p_o_arch_entity_type", columnList = "entity_type"),
		@Index(name = "idx_sys_p_o_arch_entity_identifier", columnList = "entity_identifier"),
		@Index(name = "idx_sys_p_o_arch_uid", columnList = "system_entity_uid")
		})
public class SysProvisioningArchive extends AbstractEntity {

	private static final long serialVersionUID = -6191740329296942394L;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "operation_type", nullable = false)
	private ProvisioningEventType operationType;
	
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private SysSystem system;
	
	@NotNull
	@Column(name = "provisioning_context", length = Integer.MAX_VALUE, nullable = false)
	private ProvisioningContext provisioningContext; 
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "entity_type", nullable = false)
	private SystemEntityType entityType;
	
	@Column(name = "entity_identifier")
	private UUID entityIdentifier;
	
	@Column(name = "system_entity_uid")
	private String systemEntityUid; // account uid, etc.
	
	@Embedded
	private OperationResult result;
	
	/**
	 * Provisioning operation type
	 * 
	 * @return
	 */
	public ProvisioningEventType getOperationType() {
		return operationType;
	}

	public void setOperationType(ProvisioningEventType operationType) {
		this.operationType = operationType;
	}

	/**
	 * Target system
	 * 
	 * @return
	 */
	public SysSystem getSystem() {
		return system;
	}

	public void setSystem(SysSystem system) {
		this.system = system;
	}

	/**
	 * IdM entity type
	 * 
	 * @return
	 */
	public SystemEntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
	}

	/**
	 * IdM entity type identifier
	 * 
	 * @return
	 */
	public UUID getEntityIdentifier() {
		return entityIdentifier;
	}

	public void setEntityIdentifier(UUID entityIdentifier) {
		this.entityIdentifier = entityIdentifier;
	}
	
	/**
	 * Target system entity identifier
	 * 
	 * @return
	 */
	public String getSystemEntityUid() {
		return systemEntityUid;
	}

	public void setSystemEntityUid(String systemEntityUid) {
		this.systemEntityUid = systemEntityUid;
	}
	
	public OperationState getResultState() {
		if (result != null) {
			return result.getState();
		}
		return null;
	}
	
	public void setResultState(OperationState resultState) {
		if (result == null) {
			result = new OperationResult(resultState);
		} else {
			result.setState(resultState);
		}
	}
	
	public OperationResult getResult() {
		return result;
	}
	
	public void setResult(OperationResult result) {
		this.result = result;
	}
	
	public ProvisioningContext getProvisioningContext() {
		return provisioningContext;
	}
	
	public void setProvisioningContext(ProvisioningContext provisioningContext) {
		this.provisioningContext = provisioningContext;
	}
	
}
