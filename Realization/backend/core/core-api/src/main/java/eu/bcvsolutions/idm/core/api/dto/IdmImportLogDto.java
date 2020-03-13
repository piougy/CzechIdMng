package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;

/**
 * Import log
 * 
 * @author Vít Švanda
 *
 */
@Relation(collectionRelation = "logs")
public class IdmImportLogDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = IdmExportImportDto.class)
	private UUID batch;
	private BaseDto dto;
	private UUID superParentId;
	private UUID parentId;
	private UUID dtoId;
	private String type;
	private RequestOperationType operation;
	private OperationResultDto result;
	// Count is not persisted in DB.
	private long childrenCount;

	public IdmImportLogDto() {
		super();
	}

	public IdmImportLogDto(IdmExportImportDto batch, BaseDto dto, RequestOperationType operation,
			Serializable parentId) {
		super();

		this.batch = batch.getId();
		this.dto = dto;
		this.dtoId = (UUID) dto.getId();
		this.type = dto.getClass().getName();
		this.operation = operation;
		this.parentId = (UUID) parentId;

		// If DTO ID is same as parent ID, then it is root and parentId should be set to
		// the null.
		if (this.dtoId.equals(this.parentId)) {
			this.parentId = null;
		}
	}

	public BaseDto getDto() {
		return dto;
	}

	public void setDto(BaseDto dto) {
		this.dto = dto;
	}

	public UUID getBatch() {
		return batch;
	}

	public void setBatch(UUID batch) {
		this.batch = batch;
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

	public UUID getDtoId() {
		return dtoId;
	}

	public void setDtoId(UUID dtoId) {
		this.dtoId = dtoId;
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

	public OperationResultDto getResult() {
		return result;
	}

	public void setResult(OperationResultDto result) {
		this.result = result;
	}

	public long getChildrenCount() {
		return childrenCount;
	}

	public void setChildrenCount(long childrenCount) {
		this.childrenCount = childrenCount;
	}
	
}