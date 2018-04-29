package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

@Entity
@Table(name = "idm_bulk_operation_item")
public class IdmBulkOperationItem extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "entity_id", nullable = false)
	private UUID entityId;
	
	@Column(name = "finished", nullable = false)
	private boolean finished = false;
	
	@Column(name = "entity_class", nullable = false)
	private String entityClass;
	
	@Column(name = "operation_id", nullable = false)
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
