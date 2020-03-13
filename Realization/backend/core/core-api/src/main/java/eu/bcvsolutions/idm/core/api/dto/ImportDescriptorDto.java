package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.api.domain.BaseDtoDeserializer;
import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Import descriptor (log of import)
 * 
 * @author Vít Švanda
 *
 */
public class ImportDescriptorDto implements BaseDto {

	private static final long serialVersionUID = 1L;

	@JsonDeserialize(using = BaseDtoDeserializer.class)
	private BaseDto dto;
	@JsonDeserialize(as = UUID.class)
	private UUID id;
	private UUID superParentId;
	private Class<? extends BaseDto> type;
	private RequestOperationType operation;
	private OperationResultDto result;

	public ImportDescriptorDto() {
		super();
	}

	public ImportDescriptorDto(BaseDto dto, RequestOperationType operation, Serializable superParentId) {
		super();

		this.dto = dto;
		this.id = (UUID) dto.getId();
		this.type = dto.getClass();
		this.operation = operation;
		this.superParentId = (UUID) superParentId;
	}

	public BaseDto getDto() {
		return dto;
	}

	public void setDto(BaseDto dto) {
		this.dto = dto;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		try {
			this.id = EntityUtils.toUuid(id);
		} catch (ClassCastException ex) {
			throw new IllegalArgumentException(
					"ImportDescriptor supports only UUID identifier. For different identifier generalize BaseEntity.",
					ex);
		}
	}

	public Class<? extends BaseDto> getType() {
		return type;
	}

	public void setType(Class<? extends BaseDto> type) {
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

	public UUID getSuperParentId() {
		return superParentId;
	}

	public void setSuperParentId(UUID superParentId) {
		this.superParentId = superParentId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ImportDescriptorDto)) {
			return false;
		}
		ImportDescriptorDto other = (ImportDescriptorDto) obj;
		return Objects.equals(id, other.id);
	}

}