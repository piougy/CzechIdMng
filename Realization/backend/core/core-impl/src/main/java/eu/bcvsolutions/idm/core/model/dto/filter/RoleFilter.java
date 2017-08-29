package eu.bcvsolutions.idm.core.model.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.CorrelationFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;

/**
 * Filter for roles
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
public class RoleFilter extends DataFilter implements CorrelationFilter {

	private RoleType roleType;
	private UUID roleCatalogueId;
	private UUID guaranteeId;
	/**
	 * Little dynamic search by role property and value
	 */
	private String property;
	private String value;
	
	public RoleFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public RoleFilter(MultiValueMap<String, Object> data) {
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
		return property;
	}

	@Override
	public void setProperty(String property) {
		this.property = property;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
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
}
