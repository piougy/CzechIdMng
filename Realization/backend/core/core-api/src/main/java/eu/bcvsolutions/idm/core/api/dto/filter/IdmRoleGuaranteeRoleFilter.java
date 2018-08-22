package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeRoleDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;


/**
 * Filter for {@link IdmRoleGuaranteeRoleDto} - roles
 * 
 * @author Radek Tomiška
 * @since 8.2.0
 */
public class IdmRoleGuaranteeRoleFilter extends DataFilter implements ExternalIdentifiable {
	/**
	 * guarantee as role
	 */
	public static final String PARAMETER_GUARANTEE_ROLE = "guaranteeRole";
	
	public IdmRoleGuaranteeRoleFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmRoleGuaranteeRoleFilter(MultiValueMap<String, Object> data) {
		super(IdmRoleGuaranteeRoleDto.class, data);
	}
	
	public UUID getRole() {
		return DtoUtils.toUuid(data.getFirst(IdmRoleGuaranteeFilter.PARAMETER_ROLE));
	}
	
	public void setRole(UUID role) {
		data.set(IdmRoleGuaranteeFilter.PARAMETER_ROLE, role);
	}
	
	public UUID getGuaranteeRole() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_GUARANTEE_ROLE));
	}
	
	public void setGuaranteeRole(UUID guaranteeRole) {
		data.set(PARAMETER_GUARANTEE_ROLE, guaranteeRole);
	}
	
	@Override
	public String getExternalId() {
		return (String) data.getFirst(PROPERTY_EXTERNAL_ID);
	}
	
	@Override
	public void setExternalId(String externalId) {
		data.set(PROPERTY_EXTERNAL_ID, externalId);
	}
}
