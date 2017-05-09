package eu.bcvsolutions.idm.core.model.dto.filter;

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

    private IdmRole role;

    private IdmRoleCatalogue roleCatalogue;

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

}
