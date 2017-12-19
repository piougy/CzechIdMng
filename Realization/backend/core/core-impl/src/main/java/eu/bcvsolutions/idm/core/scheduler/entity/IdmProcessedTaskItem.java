package eu.bcvsolutions.idm.core.scheduler.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.CoreException;

/**
 * Processed task item entity persists the progress
 * of long running tasks processing. Items with relation
 * to {@link IdmScheduledTask} are taken as state storage
 * for long running tasks. Items with relation are to
 * {@link IdmLongRunningTask} are simple processing logs.
 * 
 * @author Jan Helbich
 *
 */
@Entity
@Table(name = "idm_processed_task_item", indexes = {
	@Index(name = "idm_processed_t_i_l_r_t", columnList = "long_running_task"),
	@Index(name = "idm_processed_t_i_q_o", columnList = "scheduled_task_queue_owner")
})
public class IdmProcessedTaskItem extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Column(name = "referenced_entity_id", updatable = false, length = 16)
	private UUID referencedEntityId;
	
	@NotEmpty
	@Column(name = "referenced_dto_type", updatable = false)
	private String referencedDtoType;
	
	@SuppressWarnings("deprecation")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "long_running_task",
		updatable = false,
		referencedColumnName = "id",
		foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmLongRunningTask longRunningTask;
	
	@SuppressWarnings("deprecation")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "scheduled_task_queue_owner",
		updatable = false,
		referencedColumnName = "id",
		foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmScheduledTask scheduledTaskQueueOwner;
	
	@Embedded
	private OperationResult operationResult;

	public UUID getReferencedEntityId() {
		return referencedEntityId;
	}

	public void setReferencedEntityId(UUID referencedEntityId) {
		this.referencedEntityId = referencedEntityId;
	}

	public String getReferencedDtoType() {
		return referencedDtoType;
	}

	public void setReferencedDtoType(String referencedDtoType) {
		validateDtoType(referencedDtoType);
		this.referencedDtoType = referencedDtoType;
	}

	private void validateDtoType(String referencedDtoType) {
		try {
			Assert.isAssignable(AbstractDto.class, Class.forName(referencedDtoType));
		} catch (ClassNotFoundException e) {
			throw new CoreException("Class [{e}] not exists.",
					ImmutableMap.of("type", referencedDtoType), e);
		}
	}

	public IdmLongRunningTask getLongRunningTask() {
		return longRunningTask;
	}

	public void setLongRunningTask(IdmLongRunningTask longRunningTask) {
		this.longRunningTask = longRunningTask;
	}

	public OperationResult getOperationResult() {
		return operationResult;
	}

	public void setOperationResult(OperationResult operationResult) {
		this.operationResult = operationResult;
	}

	public IdmScheduledTask getScheduledTaskQueueOwner() {
		return scheduledTaskQueueOwner;
	}

	public void setScheduledTaskQueueOwner(IdmScheduledTask scheduledTaskQueueOwner) {
		this.scheduledTaskQueueOwner = scheduledTaskQueueOwner;
	}

}
