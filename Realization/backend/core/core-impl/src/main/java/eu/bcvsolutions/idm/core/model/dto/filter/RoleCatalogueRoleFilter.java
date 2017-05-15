package eu.bcvsolutions.idm.core.model.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogueRole;

/**
 * Filter for {@link IdmRoleCatalogueRole}
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */
public class RoleCatalogueRoleFilter extends QuickFilter {
	
	// TODO: remove entities
    private IdmRole role;

    private IdmRoleCatalogue roleCatalogue;
    
    private UUID roleId;
    
    private UUID roleCatalogueId;
    
    private String roleCatalogueCode;

    public IdmRole getRole() {
        return role;
    }

    public IdmRoleCatalogue getRoleCatalogue() {
        return roleCatalogue;
    }

    public void setRole(IdmRole role) {
        this.role = role;
    }

    public void setRoleCatalogue(IdmRoleCatalogue roleCatalogue) {
        this.roleCatalogue = roleCatalogue;
    }

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
