package eu.bcvsolutions.idm.acc.entity;

import java.text.MessageFormat;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.sun.istack.NotNull;

import eu.bcvsolutions.idm.acc.domain.AccountOperationType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.ResultState;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Persisted "active" provisioning operation. Any operation has request and batch.
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
		@Index(name = "idx_sys_p_o_entity_identifier", columnList = "entity_identifier"),
		@Index(name = "idx_sys_p_o_uid", columnList = "system_entity_uid")
		})
public class SysProvisioningOperation extends AbstractEntity implements ProvisioningOperation {

	private static final long serialVersionUID = -6191740329296942394L;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "operation_type", nullable = false)
	private ProvisioningOperationType operationType;
	
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
	
	@NotNull
	@OneToOne(mappedBy = "operation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private SysProvisioningRequest request;
	
	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.acc.entity.ProvisioningOperation#getOperationType()
	 */
	@Override
	public ProvisioningOperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(ProvisioningOperationType operationType) {
		this.operationType = operationType;
	}

	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.acc.entity.ProvisioningOperation#getSystem()
	 */
	@Override
	public SysSystem getSystem() {
		return system;
	}

	public void setSystem(SysSystem system) {
		this.system = system;
	}

	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.acc.entity.ProvisioningOperation#getEntityType()
	 */
	@Override
	public SystemEntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
	}

	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.acc.entity.ProvisioningOperation#getEntityIdentifier()
	 */
	@Override
	public UUID getEntityIdentifier() {
		return entityIdentifier;
	}

	public void setEntityIdentifier(UUID entityIdentifier) {
		this.entityIdentifier = entityIdentifier;
	}
	
	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.acc.entity.ProvisioningOperation#getSystemEntityUid()
	 */
	@Override
	public String getSystemEntityUid() {
		return systemEntityUid;
	}

	public void setSystemEntityUid(String systemEntityUid) {
		this.systemEntityUid = systemEntityUid;
	}
	
	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.acc.entity.ProvisioningOperation#getResultState()
	 */
	@Override
	public ResultState getResultState() {
		if (request != null && request.getResult() != null) {
			return request.getResult().getState();
		}
		return null;
	}
	
	public void setResultState(ResultState resultState) {
		if (request != null) {
			if (request.getResult() == null) {
				request.setResult(new SysProvisioningResult(resultState));
			} else {
				request.getResult().setState(resultState);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.acc.entity.ProvisioningOperation#getResult()
	 */
	@Override
	public SysProvisioningResult getResult() {
		if (request != null) {
			return request.getResult();
		}
		return null;
	}
	
	public void setRequest(SysProvisioningRequest request) {
		this.request = request;
	}
	
	public SysProvisioningRequest getRequest() {
		return request;
	}
	
	/* (non-Javadoc)
	 * @see eu.bcvsolutions.idm.acc.entity.ProvisioningOperation#getProvisioningContext()
	 */
	@Override
	public ProvisioningContext getProvisioningContext() {
		return provisioningContext;
	}
	
	public void setProvisioningContext(ProvisioningContext provisioningContext) {
		this.provisioningContext = provisioningContext;
	}
	
	/**
	 * New {@link SysProvisioningOperation} builder.
	 * 
	 * @author Radek Tomiška
	 *
	 */
	public static class Builder {
		private ProvisioningOperationType operationType;
		private SysSystem system;
		private ProvisioningContext provisioningContext;
		private SystemEntityType entityType;
		private UUID entityIdentifier;
		private String systemEntityUid;
		
		public Builder setOperationType(ProvisioningOperationType operationType) {
			this.operationType = operationType;
			return this;
		}
		
		/**
		 * Maps {@linkAccountOperationType} to {@link ProvisioningOperationType}.
		 * @param operationType
		 * @return
		 */
		public Builder setOperationType(AccountOperationType operationType) {
			switch (operationType) {
				case CREATE: {
					this.operationType = ProvisioningOperationType.CREATE;
					break;
				}
				case UPDATE: {
					this.operationType = ProvisioningOperationType.UPDATE;
					break;
				}
				case DELETE: {
					this.operationType = ProvisioningOperationType.DELETE;
					break;
				}
				default: {
					throw new UnsupportedOperationException(MessageFormat.format("Account operation type [{}] is not supported for provisioning", operationType));
				}
			}
			
			return this;
		}
		
		public Builder setSystem(SysSystem system) {
			this.system = system;
			return this;
		}
		
		public Builder setProvisioningContext(ProvisioningContext provisioningContext) {
			this.provisioningContext = provisioningContext;
			return this;
		}
		
		public Builder setEntityType(SystemEntityType entityType) {
			this.entityType = entityType;
			return this;
		}
		
		public Builder setEntityIdentifier(UUID entityIdentifier) {
			this.entityIdentifier = entityIdentifier;
			return this;
		}
		
		public Builder setSystemEntityUid(String systemEntityUid) {
			this.systemEntityUid = systemEntityUid;
			return this;
		}
		
		/**
		 * Returns newly constructed SysProvisioningOperation object.
		 * 
		 * @return
		 */
		public SysProvisioningOperation build() {
			SysProvisioningOperation provisioningOperation = new SysProvisioningOperation();
			provisioningOperation.setOperationType(operationType);
			provisioningOperation.setSystem(system);
			provisioningOperation.setSystemEntityUid(systemEntityUid);
			provisioningOperation.setEntityType(entityType);
			provisioningOperation.setEntityIdentifier(entityIdentifier);
			provisioningOperation.setProvisioningContext(provisioningContext);
			return provisioningOperation;
		}
	}
	
}
