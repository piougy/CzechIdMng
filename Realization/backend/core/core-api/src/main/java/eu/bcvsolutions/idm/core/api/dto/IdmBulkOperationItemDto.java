package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

public class IdmBulkOperationItemDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	private UUID entityId;
	private boolean finished = false;
	private String entityClass;
	private UUID operationId;

	public UUID getEntityId() {
		return entityId;
	}

	public void setEntityId(UUID entityId) {
		this.entityId = entityId;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public String getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}

	public UUID getOperationId() {
		return operationId;
	}

	public void setOperationId(UUID operationId) {
		this.operationId = operationId;
	}

}
