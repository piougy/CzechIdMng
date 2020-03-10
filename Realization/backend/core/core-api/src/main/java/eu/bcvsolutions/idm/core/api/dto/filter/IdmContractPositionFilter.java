package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Filter for identity contract's other positions
 * 
 * @author Radek Tomiška
 * @since 9.1.0
 */
public class IdmContractPositionFilter extends DataFilter implements ExternalIdentifiableFilter {

	public static final String PARAMETER_IDENTITY_CONTRACT_ID = "identityContractId";
	public static final String PARAMETER_WORK_POSITION = "workPosition";
	
	public IdmContractPositionFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmContractPositionFilter(MultiValueMap<String, Object> data) {
		super(IdmContractPositionDto.class, data);
	}

	public UUID getIdentityContractId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_IDENTITY_CONTRACT_ID));
	}
	
	public void setIdentityContractId(UUID identityContractId) {
		data.set(PARAMETER_IDENTITY_CONTRACT_ID, identityContractId);
	}
	
	public UUID getWorkPosition() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_WORK_POSITION));
	}
	
	public void setWorkPosition(UUID workPosition) {
		data.set(PARAMETER_WORK_POSITION, workPosition);
	}
}
