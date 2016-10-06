package eu.bcvsolutions.idm.core.model.dto;

import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.domain.IdmRoleType;

/**
 * Default filter for roles, RoleType
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class RoleFilter extends QuickFilter {

	private IdmRoleType roleType;

	public IdmRoleType getRoleType() {
		return roleType;
	}

	public void setRoleType(IdmRoleType roleType) {
		this.roleType = roleType;
	}
}
