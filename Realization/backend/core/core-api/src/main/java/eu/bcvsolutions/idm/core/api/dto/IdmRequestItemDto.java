package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;

/**
 * DTO for request item
 *
 * @author svandav
 */
@Relation(collectionRelation = "requestItems")
public class IdmRequestItemDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = IdmRequestDto.class)
	private UUID request;
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String ownerType;
	private UUID originalOwnerId;
	@NotNull
	private RequestOperationType operation = RequestOperationType.ADD;
	private OperationResultDto result;
	private String data; // JSON represented target DTO

	public UUID getRequest() {
		return request;
	}

	public void setRequest(UUID request) {
		this.request = request;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public UUID getOriginalOwnerId() {
		return originalOwnerId;
	}

	public void setOriginalOwnerId(UUID originalOwnerId) {
		this.originalOwnerId = originalOwnerId;
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

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}