package eu.bcvsolutions.idm.core.model.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.CorrelationFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;

/**
 * Filter for roles
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
public class RoleFilter extends DataFilter implements CorrelationFilter {

	private RoleType roleType;
	private IdmRoleCatalogue roleCatalogue;
	private IdmIdentity guarantee;
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
	
	public IdmRoleCatalogue getRoleCatalogue() {
		return roleCatalogue;
	}

	public void setRoleCatalogue(IdmRoleCatalogue roleCatalogue) {
		this.roleCatalogue = roleCatalogue;
	}

	public RoleType getRoleType() {
		return roleType;
	}

	public void setRoleType(RoleType roleType) {
		this.roleType = roleType;
	}
	
	public IdmIdentity getGuarantee() {
		return guarantee;
	}
	
	public void setGuarantee(IdmIdentity guarantee) {
		this.guarantee = guarantee;
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
}
