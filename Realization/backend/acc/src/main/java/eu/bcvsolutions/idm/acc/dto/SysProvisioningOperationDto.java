package eu.bcvsolutions.idm.acc.dto;

import java.text.MessageFormat;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation_;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * DTO for {@link SysProvisioningOperation}
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Relation(collectionRelation = "provisioningOperations")
public class SysProvisioningOperationDto extends AbstractDto implements ProvisioningOperation {

	private static final long serialVersionUID = 3928289153591673531L;
	
	private ProvisioningEventType operationType;
	private ProvisioningContext provisioningContext;
	@Embedded(dtoClass = SysSystemEntityDto.class)
	private UUID systemEntity; // account, etc.
	private UUID entityIdentifier;
	private SysProvisioningRequestDto request;

	public ProvisioningEventType getOperationType() {
		return operationType;
	}

	public void setOperationType(ProvisioningEventType operationType) {
		this.operationType = operationType;
	}

	public ProvisioningContext getProvisioningContext() {
		return provisioningContext;
	}

	public void setProvisioningContext(ProvisioningContext provisioningContext) {
		this.provisioningContext = provisioningContext;
	}

	public UUID getSystemEntity() {
		return systemEntity;
	}

	public void setSystemEntity(UUID systemEntity) {
		this.systemEntity = systemEntity;
	}

	public UUID getEntityIdentifier() {
		return entityIdentifier;
	}

	public void setEntityIdentifier(UUID entityIdentifier) {
		this.entityIdentifier = entityIdentifier;
	}

	public SysProvisioningRequestDto getRequest() {
		return request;
	}

	public void setRequest(SysProvisioningRequestDto request) {
		this.request = request;
	}

	/**
	 * systemDto is get by {@link DtoUtils#getEmbedded}
	 */
	@Override
	public UUID getSystem() {
		SysSystemEntityDto systemEntity = DtoUtils.getEmbedded(this, SysProvisioningOperation_.systemEntity, SysSystemEntityDto.class, null);
		if (systemEntity == null) {
			return null;
		}
		return systemEntity.getSystem();
	}

	@Override
	public SystemEntityType getEntityType() {
		SysSystemEntityDto systemEntity = DtoUtils.getEmbedded(this, SysProvisioningOperation_.systemEntity, SysSystemEntityDto.class, null);
		if (systemEntity == null) {
			return null;
		}
		return systemEntity.getEntityType();
	}

	@Override
	public String getSystemEntityUid() {
		SysSystemEntityDto systemEntity = DtoUtils.getEmbedded(this, SysProvisioningOperation_.systemEntity, SysSystemEntityDto.class, null);
		if (systemEntity == null) {
			return null;
		}
		return systemEntity.getUid();
	}

	@Override
	public OperationState getResultState() {
		if (this.request == null) {
			return null;
		}
		return request.getResult().getState();
	}

	@Override
	public OperationResult getResult() {
		if (this.request == null) {
			return null;
		}
		return request.getResult();
	}

	
	/**
	 * New {@link SysProvisioningOperationDto} builder.
	 * 
	 * @author Radek Tomiška
	 *
	 */
	public static class Builder {
		private ProvisioningEventType operationType;
		private ProvisioningContext provisioningContext;
		private UUID entityIdentifier;
		private UUID systemEntity;
		
		public Builder setOperationType(ProvisioningEventType operationType) {
			this.operationType = operationType;
			return this;
		}
		
		/**
		 * Maps {@linkAccountOperationType} to {@link ProvisioningEventType}.
		 * @param operationType
		 * @return
		 */
		public Builder setOperationType(ProvisioningOperationType operationType) {
			switch (operationType) {
				case CREATE: {
					this.operationType = ProvisioningEventType.CREATE;
					break;
				}
				case UPDATE: {
					this.operationType = ProvisioningEventType.UPDATE;
					break;
				}
				case DELETE: {
					this.operationType = ProvisioningEventType.DELETE;
					break;
				}
				default: {
					throw new UnsupportedOperationException(MessageFormat.format("Account operation type [{}] is not supported for provisioning", operationType));
				}
			}
			
			return this;
		}
		
		public Builder setProvisioningContext(ProvisioningContext provisioningContext) {
			this.provisioningContext = provisioningContext;
			return this;
		}
		
		
		public Builder setEntityIdentifier(UUID entityIdentifier) {
			this.entityIdentifier = entityIdentifier;
			return this;
		}
		
		public Builder setSystemEntity(UUID systemEntity) {
			this.systemEntity = systemEntity;
			return this;
		}
		
		/**
		 * Returns newly constructed SysProvisioningOperation object.
		 * 
		 * @return
		 */
		public SysProvisioningOperationDto build() {
			SysProvisioningOperationDto provisioningOperation = new SysProvisioningOperationDto();
			provisioningOperation.setOperationType(operationType);
			provisioningOperation.setSystemEntity(systemEntity);
			provisioningOperation.setEntityIdentifier(entityIdentifier);
			provisioningOperation.setProvisioningContext(provisioningContext);
			return provisioningOperation;
		}
	}
}
