package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.Set;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for role system mapping
 * 
 * @author Radek Tomiška
 *
 */
public class SysRoleSystemFilter implements BaseFilter {
	
	private UUID roleId;
	private UUID systemId;
	private UUID systemMappingId;
	private UUID attributeMappingId;
	private Set<UUID> roleIds;

	public UUID getRoleId() {
		return roleId;
	}

	public void setRoleId(UUID roleId) {
		this.roleId = roleId;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}
	
	public UUID getSystemMappingId() {
		return systemMappingId;
	}
	
	public void setSystemMappingId(UUID systemMappingId) {
		this.systemMappingId = systemMappingId;
	}

	public UUID getAttributeMappingId() {
		return attributeMappingId;
	}

	public void setAttributeMappingId(UUID attributeMappingId) {
		this.attributeMappingId = attributeMappingId;
	}

	public Set<UUID> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(Set<UUID> roleIds) {
		this.roleIds = roleIds;
	}
	
}
