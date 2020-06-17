package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;

/**
 * Filter for a delegation.
 *
 * @author Vít Švanda
 *
 */
public class IdmDelegationFilter extends DataFilter {

	public static final String PARAMETER_DELEGATOR_ID = "delegatorId";
	public static final String PARAMETER_DELEGATE_ID = "delegateId";
	public static final String PARAMETER_DELEGATOR_CONTRACT_ID = "delegatorContractId";
	public static final String PARAMETER_DELEGATION_DEFINITION_ID = "delegationDefinitionId";
	public static final String PARAMETER_OWNER_TYPE = "ownerType";
	public static final String PARAMETER_OWNER_ID = "ownerId";
	public static final String PARAMETER_OPERATION_STATE = "operationState";
	public static final String PARAMETER_INCLUDE_OWNER = "includeOwner"; // Context property - if true, then entity owns this request will be load and setts to a request DTO.

	public IdmDelegationFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmDelegationFilter(MultiValueMap<String, Object> data) {
		super(IdmExportImportDto.class, data);
	}

	public UUID getDelegatorId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_DELEGATOR_ID);
	}

	public void setDelegatorId(UUID sourceIdentity) {
		set(PARAMETER_DELEGATOR_ID, sourceIdentity);
	}

	public UUID getDelegateId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_DELEGATE_ID);
	}

	public void setDelegateId(UUID targetIdentity) {
		set(PARAMETER_DELEGATE_ID, targetIdentity);
	}

	public UUID getDelegatorContractId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_DELEGATOR_CONTRACT_ID);
	}

	public void setDelegatorContractId(UUID contractId) {
		set(PARAMETER_DELEGATOR_CONTRACT_ID, contractId);
	}

	public UUID getOwnerId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_OWNER_ID);
	}

	public void setOwnerId(UUID ownerId) {
		set(PARAMETER_OWNER_ID, ownerId);
	}

	public UUID getDelegationDefinitionId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_DELEGATION_DEFINITION_ID);
	}

	public void setDelegationDefinitionId(UUID ownerId) {
		set(PARAMETER_DELEGATION_DEFINITION_ID, ownerId);
	}

	public String getOwnerType() {
		return getParameterConverter().toString(getData(), PARAMETER_OWNER_TYPE);
	}

	public void setOwnerType(String ownerType) {
		set(PARAMETER_OWNER_TYPE, ownerType);
	}

	public boolean isIncludeOwner() {
		Boolean result = getParameterConverter().toBoolean(getData(), PARAMETER_INCLUDE_OWNER);
		if (result == null) {
			return false;
		}
		return result;
	}

	public void setIncludeOwner(boolean includeOwner) {
		set(PARAMETER_INCLUDE_OWNER, includeOwner);
	}

	public void setOperationState(OperationState operation) {
		data.set(PARAMETER_OPERATION_STATE, operation);
	}

	public OperationState getOperationState() {
		return getParameterConverter().toEnum(data, PARAMETER_OPERATION_STATE, OperationState.class);
	}

}
