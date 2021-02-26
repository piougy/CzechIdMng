package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for identity contract's other positions.
 * 
 * @author Radek Tomiška
 * @since 9.1.0
 */
public class IdmContractPositionFilter extends DataFilter implements ExternalIdentifiableFilter {

	/**
	 * Related identity - contract (~ position) owner.
	 */
	public static final String PARAMETER_IDENTITY = IdmIdentityContractFilter.PARAMETER_IDENTITY;
	/**
	 * Related contract - position owner.
	 */
	public static final String PARAMETER_IDENTITY_CONTRACT_ID = IdmIdentityRoleFilter.PARAMETER_IDENTITY_CONTRACT_ID;
	/**
	 * Position work position (tree node identifier).
	 */
	public static final String PARAMETER_WORK_POSITION = IdmIdentityContractFilter.PARAMETER_WORK_POSITION;
	/**
	 * Position for tree structure recursively. {@link RecursionType#NO} is used as default for filtering, if filter is not set.
	 * Additional parameter - effective, when {@link #PARAMETER_WORK_POSITION} is set.
	 * 
	 * @Since 10.4.0
	 */
	public static final String PARAMETER_RECURSION_TYPE = IdmIdentityContractFilter.PARAMETER_RECURSION_TYPE;
	/**
	 * Related contract is valid now or in future.
	 * 
	 * @Since 10.4.0
	 */
	public static final String PARAMETER_VALID_NOW_OR_FUTURE = IdmIdentityContractFilter.PARAMETER_VALID_NOW_OR_FUTURE;
	
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
	
	/**
	 * Contract for tree structure recursively. {@link RecursionType#NO} is used as default for filtering, if filter is not set.
	 * Additional parameter - effective, when {@link #PARAMETER_WORK_POSITION} is set.
	 * 
	 * @return
	 * @since 10.4.0
	 */
	public RecursionType getRecursionType() {
		RecursionType recursionType = getParameterConverter().toEnum(getData(), PARAMETER_RECURSION_TYPE, RecursionType.class);
		//
		return recursionType == null ? RecursionType.NO : recursionType;
	}
	
	/**
	 * Contract for tree structure recursively. {@link RecursionType#NO} is used as default for filtering, if filter is not set.
	 * Additional parameter - effective, when {@link #PARAMETER_WORK_POSITION} is set.
	 * 
	 * @param recursionType recursion type
	 * @since 10.4.0
	 */
	public void setRecursionType(RecursionType recursionType) {
		set(PARAMETER_RECURSION_TYPE, recursionType);
	}
	
	/**
	 * Related contract is valid now or in future.
	 * 
	 * @param validNowOrInFuture valid now or in the future
	 * @since 10.4.0
	 */
	public void setValidNowOrInFuture(Boolean validNowOrInFuture) {
		set(PARAMETER_VALID_NOW_OR_FUTURE, validNowOrInFuture);
	}

	/**
	 * Related contract is valid now or in future.
	 * 
	 * @return valid now or in the future
	 * @since 10.4.0
	 */
	public Boolean getValidNowOrInFuture() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_VALID_NOW_OR_FUTURE);
	}
}
