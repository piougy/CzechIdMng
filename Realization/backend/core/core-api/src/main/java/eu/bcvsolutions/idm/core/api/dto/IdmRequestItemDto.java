package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;
import eu.bcvsolutions.idm.core.api.domain.RequestState;

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
	@NotNull
	private UUID ownerId;
	private UUID originalOwnerId;
	@NotNull
	private RequestOperationType operation = RequestOperationType.ADD;
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String requestType;
	@NotNull
	private RequestState state = RequestState.CONCEPT;
	@Size(max = DefaultFieldLengths.NAME)
	private String name;
	private OperationResultDto result;

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

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
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

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public RequestState getState() {
		return state;
	}

	public void setState(RequestState state) {
		this.state = state;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public OperationResultDto getResult() {
		return result;
	}

	public void setResult(OperationResultDto result) {
		this.result = result;
	}

}