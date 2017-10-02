package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

/**
 * Filter for {@link IdmRoleCatalogueRole}
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class IdmRoleCatalogueRoleFilter extends QuickFilter {
    
    private UUID roleId;    
    private UUID roleCatalogueId;    
    private String roleCatalogueCode;

	public UUID getRoleId() {
		return roleId;
	}

	public void setRoleId(UUID roleId) {
		this.roleId = roleId;
	}

	public String getRoleCatalogueCode() {
		return roleCatalogueCode;
	}

	public void setRoleCatalogueCode(String roleCatalogueCode) {
		this.roleCatalogueCode = roleCatalogueCode;
	}

	public UUID getRoleCatalogueId() {
		return roleCatalogueId;
	}

	public void setRoleCatalogueId(UUID roleCatalogueId) {
		this.roleCatalogueId = roleCatalogueId;
	}
}
