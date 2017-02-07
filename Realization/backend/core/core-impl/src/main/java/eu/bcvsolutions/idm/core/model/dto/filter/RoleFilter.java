package eu.bcvsolutions.idm.core.model.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.model.domain.IdmRoleType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;

/**
 * Filter for roles
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class RoleFilter extends QuickFilter {

	private IdmRoleType roleType;
	private IdmRoleCatalogue roleCatalogue;
	private IdmIdentity guarantee;
	
	public IdmRoleCatalogue getRoleCatalogue() {
		return roleCatalogue;
	}

	public void setRoleCatalogue(IdmRoleCatalogue roleCatalogue) {
		this.roleCatalogue = roleCatalogue;
	}

	public IdmRoleType getRoleType() {
		return roleType;
	}

	public void setRoleType(IdmRoleType roleType) {
		this.roleType = roleType;
	}
	
	public IdmIdentity getGuarantee() {
		return guarantee;
	}
	
	public void setGuarantee(IdmIdentity guarantee) {
		this.guarantee = guarantee;
	}
}
