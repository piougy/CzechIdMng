package eu.bcvsolutions.idm.core.model.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for asigned evaluators to roles
 * 
 * @author Radek Tomiška
 *
 */
public class AuthorizationPolicyFilter implements BaseFilter {

	private UUID roleId;

	public UUID getRoleId() {
		return roleId;
	}

	public void setRoleId(UUID roleId) {
		this.roleId = roleId;
	}
}
