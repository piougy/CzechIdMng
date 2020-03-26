package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for identity contract's other positions.
 * 
 * @author Radek Tomiška
 * @since 9.1.0
 */
public class IdmContractPositionFilter extends DataFilter implements ExternalIdentifiableFilter {

	public static final String PARAMETER_IDENTITY = IdmIdentityContractFilter.PARAMETER_IDENTITY;
	public static final String PARAMETER_IDENTITY_CONTRACT_ID = "identityContractId";
	public static final String PARAMETER_WORK_POSITION = "workPosition";
	
	public IdmContractPositionFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmContractPositionFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmContractPositionFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmContractPositionDto.class, data, parameterConverter);
	}

	public UUID getIdentityContractId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_IDENTITY_CONTRACT_ID);
	}
	
	public void setIdentityContractId(UUID identityContractId) {
		set(PARAMETER_IDENTITY_CONTRACT_ID, identityContractId);
	}
	
	public UUID getWorkPosition() {
		return getParameterConverter().toUuid(getData(), PARAMETER_WORK_POSITION);
	}
	
	public void setWorkPosition(UUID workPosition) {
		set(PARAMETER_WORK_POSITION, workPosition);
	}
	
	/**
	 * @since 10.2.0
	 */
	public UUID getIdentity() {
		return getParameterConverter().toUuid(getData(), PARAMETER_IDENTITY);
	}

	/**
	 * @since 10.2.0
	 */
	public void setIdentity(UUID identity) {
		set(PARAMETER_IDENTITY, identity);
	}
}
