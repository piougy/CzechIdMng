package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for identity contract's guarantees.
 * 
 * @author Radek Tomiška
 * @author Ondrej Husnik
 *
 */
public class IdmContractGuaranteeFilter extends DataFilter implements ExternalIdentifiableFilter {

	/**
	 * Related identity - contract (~ position) owner.
	 */
	public static final String PARAMETER_IDENTITY = IdmIdentityContractFilter.PARAMETER_IDENTITY;
	/**
	 * Related contract - position owner.
	 */
	public static final String PARAMETER_IDENTITY_CONTRACT_ID = IdmIdentityRoleFilter.PARAMETER_IDENTITY_CONTRACT_ID;
	/**
	 * Guarantee identity identifier
	 */
	public static final String PARAMETER_GUARANTEE_ID = "guaranteeId";
	
	
	public IdmContractGuaranteeFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmContractGuaranteeFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmContractGuaranteeFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmContractGuaranteeDto.class, data, parameterConverter);
	}

	public UUID getIdentityContractId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_IDENTITY_CONTRACT_ID);
	}
	
	public void setIdentityContractId(UUID identityContractId) {
		set(PARAMETER_IDENTITY_CONTRACT_ID, identityContractId);
	}
	
	public UUID getGuaranteeId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_GUARANTEE_ID);
	}
	
	public void setGuaranteeId(UUID guaranteeId) {
		set(PARAMETER_GUARANTEE_ID, guaranteeId);
	}

	/**
	 * Identity of contract of which is guaranteed.
	 * 
	 * @return identity
	 * @since 10.8.0
	 */
	public UUID getIdentity() {
		return getParameterConverter().toUuid(getData(), PARAMETER_IDENTITY);
	}

	/**
	 * Identity of contract of which is guaranteed.
	 * 
	 * @param identityId identity
	 * @since 10.8.0
	 */
	public void setIdentity(UUID identity) {
		set(PARAMETER_IDENTITY, identity);
	}
}
