package eu.bcvsolutions.idm.core.api.dto.filter;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for contracts.
 *
 * @author Radek Tomiška
 */
public class IdmIdentityContractFilter
		extends DataFilter
		implements CorrelationFilter, ExternalIdentifiableFilter, DisableableFilter, FormableFilter {

	public static final String PARAMETER_EXCLUDED = "excluded"; // true / false
	/**
	 * Subordinate contracts for given identity.
	 */
	public static final String PARAMETER_SUBORDINATES_FOR = IdmIdentityFilter.PARAMETER_SUBORDINATES_FOR;
	/**
	 * Subordinate contracts by given tree structure.
	 */
	public static final String PARAMETER_SUBORDINATES_BY_TREE_TYPE = IdmIdentityFilter.PARAMETER_SUBORDINATES_BY_TREE_TYPE;
	/**
	 * Managers with contract guarantees included (manually assigned guarantees).
	 */
	public static final String PARAMETER_INCLUDE_GUARANTEES = IdmIdentityFilter.PARAMETER_INCLUDE_GUARANTEES;
	
	public static final String PARAMETER_IDENTITY = "identity";
	public static final String PARAMETER_WORK_POSITION = "workPosition";
	public static final String PARAMETER_POSITION = "position";
	public static final String PARAMETER_VALID_FROM = ValidableEntity.PROPERTY_VALID_FROM;  
	public static final String PARAMETER_VALID_TILL = ValidableEntity.PROPERTY_VALID_TILL;
	public static final String PARAMETER_EXTERNE = "externe";
	public static final String PARAMETER_VALID = "valid";
	public static final String PARAMETER_VALID_NOW_OR_FUTURE = "validNowOrInFuture";
	public static final String PARAMETER_MAIN = "main";
	public static final String PARAMETER_STATE = "state";
	public static final String PARAMETER_ROLE = "role";

	public IdmIdentityContractFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmIdentityContractFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmIdentityContractFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		this(IdmIdentityContractDto.class, data, parameterConverter);
	}

	public IdmIdentityContractFilter(Class<? extends BaseDto> dtoClass, MultiValueMap<String, Object> data) {
		this(dtoClass, data, null);
	}
	
	public IdmIdentityContractFilter(Class<? extends BaseDto> dtoClass, MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(dtoClass, data, parameterConverter);
	}

	public UUID getIdentity() {
		return getParameterConverter().toUuid(getData(), PARAMETER_IDENTITY);
	}

	public void setIdentity(UUID identity) {
		set(PARAMETER_IDENTITY, identity);
	}

	public LocalDate getValidFrom() {
		return getParameterConverter().toLocalDate(data, PARAMETER_VALID_FROM);
	}

	public void setValidFrom(LocalDate validFrom) {
		set(PARAMETER_VALID_FROM, validFrom);
	}

	public LocalDate getValidTill() {
		return getParameterConverter().toLocalDate(data, PARAMETER_VALID_TILL);
	}

	public void setValidTill(LocalDate validTill) {
		set(PARAMETER_VALID_TILL, validTill);
	}

	public Boolean getExterne() {
		return getParameterConverter().toBoolean(data, PARAMETER_EXTERNE);
	}

	public void setExterne(Boolean externe) {
		set(PARAMETER_EXTERNE, externe);
	}

	public Boolean getValid() {
		return getParameterConverter().toBoolean(data, PARAMETER_VALID);
	}

	public void setValid(Boolean valid) {
		set(PARAMETER_VALID, valid);
	}

	public Boolean getMain() {
		return getParameterConverter().toBoolean(data, PARAMETER_MAIN);
	}

	public void setMain(Boolean main) {
		set(PARAMETER_MAIN, main);
	}

	public void setValidNowOrInFuture(Boolean validNowOrInFuture) {
		set(PARAMETER_VALID_NOW_OR_FUTURE, validNowOrInFuture);
	}

	public Boolean getValidNowOrInFuture() {
		return getParameterConverter().toBoolean(data, PARAMETER_VALID_NOW_OR_FUTURE);
	}

	public void setState(ContractState state) {
		set(PARAMETER_STATE, state);
	}

	public ContractState getState() {
		return getParameterConverter().toEnum(data, PARAMETER_STATE, ContractState.class);
	}
	
	public void setPosition(String position) {
		set(PARAMETER_POSITION, position);
	}
	
	public String getPosition() {
		return getParameterConverter().toString(data, PARAMETER_POSITION);
	}
	
	public void setWorkPosition(UUID workPosition) {
		set(PARAMETER_WORK_POSITION, workPosition);
	}
	
	public UUID getWorkPosition() {
		return getParameterConverter().toUuid(data, PARAMETER_WORK_POSITION);
	}
	
	public Boolean getExcluded() {
    	return getParameterConverter().toBoolean(data, PARAMETER_EXCLUDED);
	}
	
	public void setExcluded(Boolean excluded) {
		set(PARAMETER_EXCLUDED, excluded);
	}
	
	/**
	 * Subordinate contracts for given identity (manager or guarantee).
	 * 
	 * @return
	 * @since 9.7.0
	 */
	public UUID getSubordinatesFor() {
		return getParameterConverter().toUuid(data, PARAMETER_SUBORDINATES_FOR);
	}

	/**
	 * Subordinate contracts for given identity (manager or guarantee).
	 * 
	 * @param subordinatesFor
	 * @since 9.7.0
	 */
	public void setSubordinatesFor(UUID subordinatesFor) {
		set(PARAMETER_SUBORDINATES_FOR, subordinatesFor);
	}
	
	/**
	 * Subordinate contracts by given tree structure.
	 * 
	 * @return
	 * @since 9.7.0
	 */
	public UUID getSubordinatesByTreeType() {
		return getParameterConverter().toUuid(data, PARAMETER_SUBORDINATES_BY_TREE_TYPE);
	}

	/**
	 * Subordinate contracts by given tree structure.
	 * 
	 * @param subordinatesByTreeType
	 * @since 9.7.0
	 */
	public void setSubordinatesByTreeType(UUID subordinatesByTreeType) {
		set(PARAMETER_SUBORDINATES_BY_TREE_TYPE, subordinatesByTreeType);
	}
	
	/**
	 * Managers with contract guarantees included (manually assigned guarantees).
	 * 
	 * @return
	 * @since 9.7.0
	 */
	public boolean isIncludeGuarantees() {
		return getParameterConverter().toBoolean(data, PARAMETER_INCLUDE_GUARANTEES, true);
	}
	
	/**
	 * Managers with contract guarantees included (manually assigned guarantees).
	 * 
	 * @param includeGuarantees
	 * @since 9.7.0
	 */
	public void setIncludeGuarantees(boolean includeGuarantees) {
		set(PARAMETER_INCLUDE_GUARANTEES, includeGuarantees);
	}

	/**
	 * Role assigned to / by contract.
	 * 
	 * @return role identifier
	 * @since 9.7.0
	 */
	public UUID getRoleId() {
		return getParameterConverter().toUuid(data, PARAMETER_ROLE);
	}

	/**
	 * Role assigned to / by contract.
	 * 
	 * @param roleId
	 * @since 9.7.0
	 */
	public void setRoleId(UUID roleId) {
		set(PARAMETER_ROLE, roleId);
	}
}
