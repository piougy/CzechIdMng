package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;
import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

/**
 * Entity for universal request item
 * 
 * @author svandav
 * @since 9.0.0
 *
 */
@Entity
@Table(name = "idm_request_item", indexes = { //
		@Index(name = "idx_idm_req_item_o_id", columnList = "owner_id"), //
		@Index(name = "idx_idm_req_item_o_o_id", columnList = "orig_owner_id"), //
		@Index(name = "idx_idm_req_item_o_type", columnList = "owner_type"), //
		@Index(name = "idx_idm_req_item_state", columnList = "state"), //
		@Index(name = "idx_idm_req_item_operation", columnList = "operation"), //
		@Index(name = "idx_idm_req_item_req_id", columnList = "request_id"), //
})
public class IdmRequestItem extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "request_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmRequest request;

	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "owner_type", length = DefaultFieldLengths.NAME, nullable = false)
	private String ownerType;

	@Column(name = "owner_id", length = 16)
	private UUID ownerId;

	@Column(name = "orig_owner_id", length = 16)
	private UUID originalOwnerId;

	@Audited
	@NotNull
	@Column(name = "request_type", nullable = false)
	private String requestType;

	@Audited
	@NotNull
	@Column(name = "state")
	@Enumerated(EnumType.STRING)
	private RequestState state = RequestState.CONCEPT;

	@Embedded
	private OperationResult result;

	@Audited
	@NotNull
	@Column(name = "execute_immediately")
	private boolean executeImmediately = false;

	@Audited
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description")
	private String description;

	@Audited
	@Enumerated(EnumType.STRING)
	@Column(name = "operation")
	private RequestOperationType operation = RequestOperationType.UPDATE;

	public IdmRequest getRequest() {
		return request;
	}

	public void setRequest(IdmRequest request) {
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

	public OperationResult getResult() {
		return result;
	}

	public void setResult(OperationResult result) {
		this.result = result;
	}

	public boolean isExecuteImmediately() {
		return executeImmediately;
	}

	public void setExecuteImmediately(boolean executeImmediately) {
		this.executeImmediately = executeImmediately;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public RequestOperationType getOperation() {
		return operation;
	}

	public void setOperation(RequestOperationType operation) {
		this.operation = operation;
	}

}