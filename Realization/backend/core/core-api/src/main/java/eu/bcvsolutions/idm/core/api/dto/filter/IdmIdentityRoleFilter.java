package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Filter for identity role
 *
 * @author svandav
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class IdmIdentityRoleFilter extends DataFilter implements ExternalIdentifiable, CorrelationFilter, FormableFilter {
	
	public static final String PARAMETER_DIRECT_ROLE = "directRole"; //if its direct role (true) or not (false - depends on some filled direct role)
	public static final String PARAMETER_DIRECT_ROLE_ID = "directRoleId";
	public static final String PARAMETER_ROLE_COMPOSITION_ID = "roleCompositionId";
	public static final String PARAMETER_ROLE_ID = "roleId";
	public static final String PARAMETER_IDENTITY_ID = "identityId";
	public static final String PARAMETER_ROLE_CATALOGUE_ID = "roleCatalogueId";
	public static final String PARAMETER_VALID = "valid"; // valid identity roles with valid contract
	public static final String PARAMETER_AUTOMATIC_ROLE = "automaticRole"; // true / false
	public static final String PARAMETER_AUTOMATIC_ROLE_ID = "automaticRoleId";
	public static final String PARAMETER_IDENTITY_CONTRACT_ID = "identityContractId";
	public static final String PARAMETER_CONTRACT_POSITION_ID = "contractPositionId";
    
    public IdmIdentityRoleFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmIdentityRoleFilter(MultiValueMap<String, Object> data) {
		super(IdmIdentityRoleDto.class, data);
	}

    public UUID getIdentityId() {
    	return DtoUtils.toUuid(data.getFirst(PARAMETER_IDENTITY_ID));
    }

    public void setIdentityId(UUID identityId) {
    	data.set(PARAMETER_IDENTITY_ID, identityId);
    }
    
    public UUID getRoleId() {
    	return DtoUtils.toUuid(data.getFirst(PARAMETER_ROLE_ID));
	}
    
    public void setRoleId(UUID roleId) {
    	data.set(PARAMETER_ROLE_ID, roleId);
	}

	public UUID getRoleCatalogueId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_ROLE_CATALOGUE_ID));
	}

	public void setRoleCatalogueId(UUID roleCatalogueId) {
		data.set(PARAMETER_ROLE_CATALOGUE_ID, roleCatalogueId);
	}

	public Boolean getValid() {
		Object first = data.getFirst(PARAMETER_VALID);
    	if (first == null) {
    		return null;
    	}
    	return BooleanUtils.toBoolean(first.toString());
	}

	public void setValid(Boolean valid) {
		data.set(PARAMETER_VALID, valid);
	}

	public Boolean getAutomaticRole() {
		Object first = data.getFirst(PARAMETER_AUTOMATIC_ROLE);
    	if (first == null) {
    		return null;
    	}
    	return BooleanUtils.toBoolean(first.toString());
	}

	public void setAutomaticRole(Boolean automaticRole) {
		data.set(PARAMETER_AUTOMATIC_ROLE, automaticRole);
	}

	public UUID getAutomaticRoleId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_AUTOMATIC_ROLE_ID));
	}

	public void setAutomaticRoleId(UUID automaticRoleId) {
		data.set(PARAMETER_AUTOMATIC_ROLE_ID, automaticRoleId);
	}

	public UUID getIdentityContractId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_IDENTITY_CONTRACT_ID));
	}

	public void setIdentityContractId(UUID identityContractId) {
		data.set(PARAMETER_IDENTITY_CONTRACT_ID, identityContractId);
	}
	
	@Override
	public String getExternalId() {
		return (String) data.getFirst(PROPERTY_EXTERNAL_ID);
	}
	
	@Override
	public void setExternalId(String externalId) {
		data.set(PROPERTY_EXTERNAL_ID, externalId);
	}
	
	public Boolean getDirectRole() {
		Object first = data.getFirst(PARAMETER_DIRECT_ROLE);
    	if (first == null) {
    		return null;
    	}
    	return BooleanUtils.toBoolean(first.toString());
	}

	public void setDirectRole(Boolean directRole) {
		data.set(PARAMETER_DIRECT_ROLE, directRole);
	}

	public UUID getDirectRoleId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_DIRECT_ROLE_ID));
	}

	public void setDirectRoleId(UUID directRoleId) {
		data.set(PARAMETER_DIRECT_ROLE_ID, directRoleId);
	}

	public UUID getRoleCompositionId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_ROLE_COMPOSITION_ID));
	}

	public void setRoleCompositionId(UUID roleCompositionId) {
		data.set(PARAMETER_ROLE_COMPOSITION_ID, roleCompositionId);
	}
	
	public UUID getContractPositionId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_CONTRACT_POSITION_ID));
	}
	
	public void setContractPositionId(UUID contractPositionId) {
		data.set(PARAMETER_CONTRACT_POSITION_ID, contractPositionId);
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
	public Boolean getAddEavMetadata() {
		Object first = data.getFirst(PARAMETER_ADD_EAV_METADATA);
    	if (first == null) {
    		return null;
    	}
    	return BooleanUtils.toBoolean(first.toString());
	}

	@Override
	public void setAddEavMetadata(Boolean value) {
		data.set(PARAMETER_ADD_EAV_METADATA, value);
	}
}
