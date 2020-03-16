package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;

/**
 * Import log
 * 
 * @author Vít Švanda
 *
 */
@Entity
@Table(name = "idm_import_log", indexes = { @Index(name = "idx_i_export_log_parent_id", columnList = "parent_id"),
		@Index(name = "idx_i_export_log_suparent_id", columnList = "super_parent_id"),
		@Index(name = "idx_i_export_log_dto_id", columnList = "dto_id") })
public class IdmImportLog extends AbstractEntity implements AttachableEntity {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("deprecation")
	@ManyToOne(fetch = FetchType.LAZY)
	@NotNull
	@JoinColumn(name = "batch_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT), nullable = false)
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmExportImport batch;
	@Column(name = "dto", length = Integer.MAX_VALUE)
	//@JsonDeserialize(using = BaseDtoDeserializer.class)
	private BaseDto dto;
	@Column(name = "super_parent_id")
	private UUID superParentId;
	@Column(name = "parent_id")
	private UUID parentId;
	@NotNull
	@Column(name = "dto_id", nullable = false)
	private UUID dtoId;
	@NotNull
	@Column(name = "type", nullable = false, length = 255)
	private String type;
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "operation", nullable = false, length = 45)
	private RequestOperationType operation;
	@Embedded
	private OperationResult result;

	public IdmExportImport getBatch() {
		return batch;
	}

	public void setBatch(IdmExportImport batch) {
		this.batch = batch;
	}

	public BaseDto getDto() {
		return dto;
	}

	public void setDto(BaseDto dto) {
		this.dto = dto;
	}

	public UUID getSuperParentId() {
		return superParentId;
	}

	public void setSuperParentId(UUID superParentId) {
		this.superParentId = superParentId;
	}

	public UUID getParentId() {
		return parentId;
	}

	public void setParentId(UUID parentId) {
		this.parentId = parentId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public RequestOperationType getOperation() {
		return operation;
	}

	public void setOperation(RequestOperationType operation) {
		this.operation = operation;
	}

	public OperationResult getResult() {
		return result;
	}

	public void setResult(OperationResult result) {
		this.result = result;
	}

	public UUID getDtoId() {
		return dtoId;
	}

	public void setDtoId(UUID dtoId) {
		this.dtoId = dtoId;
	}

}
