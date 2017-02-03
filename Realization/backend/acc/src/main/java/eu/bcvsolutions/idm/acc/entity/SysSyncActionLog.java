package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.sun.istack.NotNull;

import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * <i>SysSyncActionLog</i> is responsible for keep log informations about
 * specific synchronization operation.
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = "sys_sync_action_log")
public class SysSyncActionLog extends AbstractEntity {

	private static final long serialVersionUID = -5447620157233410338L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "sync_log_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysSyncLog syncLog;

	@Enumerated(EnumType.STRING)
	@Column(name = "sync_action", nullable = false)
	private SynchronizationActionType syncAction;
	
	@NotNull
	@Column(name = "operation_count", nullable = false)
	private Integer operationCount = 0;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "result", nullable = false)
	private OperationResultType operationResult;

	public SysSyncLog getSyncLog() {
		return syncLog;
	}

	public void setSyncLog(SysSyncLog syncLog) {
		this.syncLog = syncLog;
	}

	public SynchronizationActionType getSyncAction() {
		return syncAction;
	}

	public void setSyncAction(SynchronizationActionType syncAction) {
		this.syncAction = syncAction;
	}

	public Integer getOperationCount() {
		if(operationCount == null){
			operationCount = Integer.valueOf(0);
		}
		return operationCount;
	}

	public void setOperationCount(Integer operationCount) {
		this.operationCount = operationCount;
	}

	public OperationResultType getOperationResult() {
		return operationResult;
	}

	public void setOperationResult(OperationResultType operationResult) {
		this.operationResult = operationResult;
	}
}
