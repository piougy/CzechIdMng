package eu.bcvsolutions.idm.core.model.entity;

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
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.ExportImportType;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.ecm.entity.IdmAttachment;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;

/**
 * Export and import IdM data
 * - Data as zip object is stored in attachment manager
 * 
 * @author Vít Švanda
 *
 */
@Entity
@Table(name = "idm_export_import", indexes = {
		@Index(name = "idx_idm_export_name", columnList = "name"),
		@Index(name = "idx_idm_export_executor", columnList = "executor_name"),
		@Index(name = "idx_idm_export_lrt_id", columnList = "long_running_task_id")})
public class IdmExportImport extends AbstractEntity implements AttachableEntity {
	
	private static final long serialVersionUID = 1L;
	
	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 45)
	private ExportImportType type;
	//
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name; // user friendly name
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "executor_name", length = DefaultFieldLengths.NAME)
	private String executorName;
	
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@SuppressWarnings("deprecation")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "data_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmAttachment data; // data stored in attachment manager
	
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

	public IdmLongRunningTask getLongRunningTask() {
		return longRunningTask;
	}

	public void setLongRunningTask(IdmLongRunningTask longRunningTask) {
		this.longRunningTask = longRunningTask;
	}

	public ExportImportType getType() {
		return type;
	}

	public void setType(ExportImportType type) {
		this.type = type;
	}
}
