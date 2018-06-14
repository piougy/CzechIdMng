package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;

/**
 * Filter for roles
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
public class IdmRoleFilter 
		extends DataFilter 
		implements CorrelationFilter, ExternalIdentifiable {

	private RoleType roleType;
	private UUID roleCatalogueId;
	private UUID guaranteeId;
	
	public IdmRoleFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmRoleFilter(MultiValueMap<String, Object> data) {
		super(IdmRoleDto.class, data);
	}

	public RoleType getRoleType() {
		return roleType;
	}

	public void setRoleType(RoleType roleType) {
		this.roleType = roleType;
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

	public UUID getRoleCatalogueId() {
		return roleCatalogueId;
	}

	public void setRoleCatalogueId(UUID roleCatalogueId) {
		this.roleCatalogueId = roleCatalogueId;
	}

	public UUID getGuaranteeId() {
		return guaranteeId;
	}

	public void setGuaranteeId(UUID guaranteeId) {
		this.guaranteeId = guaranteeId;
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
