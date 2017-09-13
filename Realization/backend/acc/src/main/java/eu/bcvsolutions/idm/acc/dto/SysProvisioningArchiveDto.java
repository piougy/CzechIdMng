package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningArchive;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * DTO for {@link SysProvisioningArchive}
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Relation(collectionRelation = "provisioningArchives")
public class SysProvisioningArchiveDto extends AbstractDto implements ProvisioningOperation {

	private static final long serialVersionUID = 9129849089102546294L;

	private ProvisioningEventType operationType;
	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;
	private ProvisioningContextDto provisioningContext;
	private SystemEntityType entityType;
	private UUID entityIdentifier;
	private String systemEntityUid; // account uid, etc.
	private OperationResultDto result;

	public ProvisioningEventType getOperationType() {
		return operationType;
	}

	public void setOperationType(ProvisioningEventType operationType) {
		this.operationType = operationType;
	}

	public UUID getSystem() {
		return system;
	}

	public void setSystem(UUID system) {
		this.system = system;
	}

	public ProvisioningContextDto getProvisioningContext() {
		return provisioningContext;
	}

	public void setProvisioningContext(ProvisioningContextDto provisioningContext) {
		this.provisioningContext = provisioningContext;
	}

	public SystemEntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
	}

	public UUID getEntityIdentifier() {
		return entityIdentifier;
	}

	public void setEntityIdentifier(UUID entityIdentifier) {
		this.entityIdentifier = entityIdentifier;
	}

	public String getSystemEntityUid() {
		return systemEntityUid;
	}

	public void setSystemEntityUid(String systemEntityUid) {
		this.systemEntityUid = systemEntityUid;
	}

	public OperationResultDto getResult() {
		return result;
	}

	public void setResult(OperationResultDto result) {
		this.result = result;
	}

	/**
	 * New {@link SysProvisioningArchiveDto} builder.
	 * 
	 * @author Radek Tomiška
	 *
	 */
	public static class Builder {
		private ProvisioningEventType operationType;
		private UUID system;
		private ProvisioningContextDto provisioningContext;
		private SystemEntityType entityType;
		private UUID entityIdentifier;
		private String systemEntityUid;
		private OperationResultDto result;
		
		public Builder() {
		}
		
		public Builder(ProvisioningOperation provisioningOperation) {
			this.operationType = provisioningOperation.getOperationType();
			this.system = provisioningOperation.getSystem();
			this.provisioningContext = provisioningOperation.getProvisioningContext();
			this.entityType = provisioningOperation.getEntityType();
			this.entityIdentifier = provisioningOperation.getEntityIdentifier();
			this.systemEntityUid = provisioningOperation.getSystemEntityUid();
			this.result = provisioningOperation.getResult();
		}
		
		public Builder setOperationType(ProvisioningEventType operationType) {
			this.operationType = operationType;
			return this;
		}
		
		public Builder setSystem(SysSystemDto system) {
			if (system != null) {
				this.system = system.getId();				
			}
			return this;
		}
		
		public Builder setProvisioningContext(ProvisioningContextDto provisioningContext) {
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
		
		public Builder setResult(OperationResultDto result) {
			this.result = result;
			return this;
		}
		
		/**
		 * Returns newly constructed SysProvisioningArchive object.
		 * 
		 * @return
		 */
		public SysProvisioningArchiveDto build() {
			SysProvisioningArchiveDto provisioningArchive = new SysProvisioningArchiveDto();
			provisioningArchive.setOperationType(operationType);
			provisioningArchive.setSystem(system);
			provisioningArchive.setSystemEntityUid(systemEntityUid);
			provisioningArchive.setEntityType(entityType);
			provisioningArchive.setEntityIdentifier(entityIdentifier);
			provisioningArchive.setProvisioningContext(provisioningContext);
			provisioningArchive.setResult(result);
			return provisioningArchive;
		}
	}

	@Override
	public OperationState getResultState() {
		if (result != null) {
			return result.getState();
		}
		return null;
	}
}
