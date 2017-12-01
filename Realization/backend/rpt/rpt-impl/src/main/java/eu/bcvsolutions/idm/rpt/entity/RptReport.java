package eu.bcvsolutions.idm.rpt.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.ecm.entity.IdmAttachment;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;

/**
 * Report data ~ generated report
 * - Report data as json object is stored in attachment manager
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "rpt_report", indexes = {
		@Index(name = "idx_rpt_report_name", columnList = "name"),
		@Index(name = "idx_rpt_report_executor", columnList = "executor_name"),
		@Index(name = "idx_rpt_report_lrt_id", columnList = "long_running_task_id")})
public class RptReport extends AbstractEntity 
		implements FormableEntity, AttachableEntity {
	
	private static final long serialVersionUID = 1L;
	//
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name; // user friendly name
	
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "executor_name", length = DefaultFieldLengths.NAME, nullable = false)
	private String executorName;
	
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@SuppressWarnings("deprecation")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "data_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmAttachment data; // json data as string as stored in attachment manager
	
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@SuppressWarnings("deprecation")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "long_running_task_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmLongRunningTask longRunningTask;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getExecutorName() {
		return executorName;
	}
	
	public void setExecutorName(String executorName) {
		this.executorName = executorName;
	}
	
	public IdmAttachment getData() {
		return data;
	}
	
	public void setData(IdmAttachment data) {
		this.data = data;
	}

	public OperationResult getResult() {
		return longRunningTask == null ? null : longRunningTask.getResult();
	}

	public IdmLongRunningTask getLongRunningTask() {
		return longRunningTask;
	}

	public void setLongRunningTask(IdmLongRunningTask longRunningTask) {
		this.longRunningTask = longRunningTask;
	}
}
