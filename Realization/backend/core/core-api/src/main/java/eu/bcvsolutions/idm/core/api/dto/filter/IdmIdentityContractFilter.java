package eu.bcvsolutions.idm.core.api.dto.filter;

import java.time.LocalDate;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for {@link IdmIdentityDto} dtos.
 *
 * @author Radek Tomiška
 */
public class IdmIdentityContractFilter
		extends DataFilter
		implements CorrelationFilter, ExternalIdentifiable {

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
	//
	private UUID identity;
	private UUID workPosition;
	private LocalDate validFrom;
	private LocalDate validTill;
	private Boolean externe;
	private Boolean disabled;
	private Boolean valid;
	private Boolean main;
	private Boolean validNowOrInFuture;
	private ContractState state;
	private String position;
	private UUID roleId;

	public IdmIdentityContractFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmIdentityContractFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmIdentityContractFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		this(IdmIdentityContractDto.class, data, null);
	}

	public IdmIdentityContractFilter(Class<? extends BaseDto> dtoClass, MultiValueMap<String, Object> data) {
		this(dtoClass, data, null);
	}
	
	public IdmIdentityContractFilter(Class<? extends BaseDto> dtoClass, MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(dtoClass, data, parameterConverter);
	}

	public UUID getIdentity() {
		return identity;
	}

	public void setIdentity(UUID identity) {
		this.identity = identity;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTill() {
		return validTill;
	}

	public void setValidTill(LocalDate validTill) {
		this.validTill = validTill;
	}

	public Boolean getExterne() {
		return externe;
	}

	public void setExterne(Boolean externe) {
		this.externe = externe;
	}

	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public Boolean getMain() {
		return main;
	}

	public void setMain(Boolean main) {
		this.main = main;
	}

	public void setValidNowOrInFuture(Boolean validNowOrInFuture) {
		this.validNowOrInFuture = validNowOrInFuture;
	}

	public Boolean getValidNowOrInFuture() {
		return validNowOrInFuture;
	}

	public void setState(ContractState state) {
		this.state = state;
	}

	public ContractState getState() {
		return state;
	}
	
	@Override
	public String getProperty() {
		return (String) data.getFirst(PARAMETER_CORRELATION_PROPERTY);
	}

	@Override
	public void setProperty(String property) {
		data.set(PARAMETER_CORRELATION_PROPERTY, property);
	}

	@Override
	public String getValue() {
		return (String) data.getFirst(PARAMETER_CORRELATION_VALUE);
	}

	@Override
	public void setValue(String value) {
		data.set(PARAMETER_CORRELATION_VALUE, value);
	}

	@Override
	public String getExternalId() {
		return (String) data.getFirst(PROPERTY_EXTERNAL_ID);
	}

	@Override
	public void setExternalId(String externalId) {
		data.set(PROPERTY_EXTERNAL_ID, externalId);
	}
	
	public void setPosition(String position) {
		this.position = position;
	}
	
	public String getPosition() {
		return position;
	}
	
	public void setWorkPosition(UUID workPosition) {
		this.workPosition = workPosition;
	}
	
	public UUID getWorkPosition() {
		return workPosition;
	}
	
	public Boolean getExcluded() {
		Object first = data.getFirst(PARAMETER_EXCLUDED);
    	if (first == null) {
    		return null;
    	}
    	return BooleanUtils.toBoolean(first.toString());
	}
	
	public void setExcluded(Boolean excluded) {
		data.set(PARAMETER_EXCLUDED, excluded);
	}
	
	/**
	 * Subordinate contracts for given identity (manager or guarantee).
	 * 
	 * @return
	 * @since 9.7.0
	 */
	public UUID getSubordinatesFor() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_SUBORDINATES_FOR));
	}

	/**
	 * Subordinate contracts for given identity (manager or guarantee).
	 * 
	 * @param subordinatesFor
	 * @since 9.7.0
	 */
	public void setSubordinatesFor(UUID subordinatesFor) {
		data.set(PARAMETER_SUBORDINATES_FOR, subordinatesFor);
	}
	
	/**
	 * Subordinate contracts by given tree structure.
	 * 
	 * @return
	 * @since 9.7.0
	 */
	public UUID getSubordinatesByTreeType() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_SUBORDINATES_BY_TREE_TYPE));
	}

	/**
	 * Subordinate contracts by given tree structure.
	 * 
	 * @param subordinatesByTreeType
	 * @since 9.7.0
	 */
	public void setSubordinatesByTreeType(UUID subordinatesByTreeType) {
		data.set(PARAMETER_SUBORDINATES_BY_TREE_TYPE, subordinatesByTreeType);
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
		data.set(PARAMETER_INCLUDE_GUARANTEES, includeGuarantees);
	}

	public UUID getRoleId() {
		return roleId;
	}

	public void setRoleId(UUID roleId) {
		this.roleId = roleId;
	}
}
