package eu.bcvsolutions.idm.core.model.dto;

import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.domain.IdmRoleType;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;

/**
 * Default filter for roles, RoleType, RoleCatalogue
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class RoleFilter extends QuickFilter {

	private IdmRoleType roleType;
	
	private IdmRoleCatalogue roleCatalogue;
	
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
}
