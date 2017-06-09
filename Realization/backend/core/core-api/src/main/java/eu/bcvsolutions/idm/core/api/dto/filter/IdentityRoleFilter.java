package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

/**
 * Filter for identity role
 *
 * @author svandav
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * 
 */
public class IdentityRoleFilter extends QuickFilter {
	
    private UUID identityId;
    private UUID roleCatalogueId;

    public UUID getIdentityId() {
        return identityId;
    }

    public void setIdentityId(UUID identityId) {
        this.identityId = identityId;
    }

	public UUID getRoleCatalogueId() {
		return roleCatalogueId;
	}

	public void setRoleCatalogueId(UUID roleCatalogueId) {
		this.roleCatalogueId = roleCatalogueId;
	}

}
