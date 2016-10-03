package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.core.model.dto.BaseFilter;

/**
 * Filter for role system mapping
 * 
 * @author Radek Tomiška
 *
 */
public class AccRoleSystemFilter implements BaseFilter {
	
	private Long roleId;
	
	private Long systemId;

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public Long getSystemId() {
		return systemId;
	}

	public void setSystemId(Long systemId) {
		this.systemId = systemId;
	}

	
}
