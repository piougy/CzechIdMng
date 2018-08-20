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
	private UUID ownerId;
	@NotNull
	private RequestOperationType operation = RequestOperationType.ADD;
	private OperationResultDto result;
	private String data; // JSON represented target DTO
	private String wfProcessId;
	private String superOwnerType; // Super owner ... using for form value where super owner is FormableEntity
	private UUID superOwnerId;
	@NotNull
	protected RequestState state = RequestState.CONCEPT;

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

	public String getSuperOwnerType() {
		return superOwnerType;
	}

	public void setSuperOwnerType(String superOwnerType) {
		this.superOwnerType = superOwnerType;
	}

	public UUID getSuperOwnerId() {
		return superOwnerId;
	}

	public void setSuperOwnerId(UUID superOwnerId) {
		this.superOwnerId = superOwnerId;
	}

	public String getWfProcessId() {
		return wfProcessId;
	}

	public void setWfProcessId(String wfProcessId) {
		this.wfProcessId = wfProcessId;
	}

	public RequestState getState() {
		return state;
	}

	public void setState(RequestState state) {
		this.state = state;
	}
}
