package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for a definition of delegation.
 *
 * @author Vít Švanda
 *
 */
public class IdmDelegationDefinitionFilter extends DataFilter {

	public static final String PARAMETER_DELEGATOR_ID = "delegatorId";
	public static final String PARAMETER_DELEGATE_ID = "delegateId";
	public static final String PARAMETER_DELEGATOR_CONTRACT_ID = "delegatorContractId";
	public static final String PARAMETER_TYPE = "type";
	public static final String PARAMETER_VALID = "valid";

	public IdmDelegationDefinitionFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmDelegationDefinitionFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmDelegationDefinitionFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmExportImportDto.class, data, parameterConverter);
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

	public String getType() {
		return getParameterConverter().toString(getData(), PARAMETER_TYPE);
	}

	public void setType(String type) {
		set(PARAMETER_TYPE, type);
	}

	public Boolean getValid() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_VALID);
	}

	public void setValid(Boolean valid) {
		set(PARAMETER_VALID, valid);
	}
}
