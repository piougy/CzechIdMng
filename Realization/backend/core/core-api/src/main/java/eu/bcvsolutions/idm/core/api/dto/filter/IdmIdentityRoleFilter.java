package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;

/**
 * Filter for identity role
 *
 * @author svandav
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class IdmIdentityRoleFilter extends DataFilter {
	
	private UUID roleId;
    private UUID identityId;
    private UUID roleCatalogueId;
    private Boolean valid;
    
    public IdmIdentityRoleFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmIdentityRoleFilter(MultiValueMap<String, Object> data) {
		super(IdmRoleDto.class, data);
	}

    public UUID getIdentityId() {
        return identityId;
    }

    public void setIdentityId(UUID identityId) {
        this.identityId = identityId;
    }
    
    public UUID getRoleId() {
		return roleId;
	}
    
    public void setRoleId(UUID roleId) {
		this.roleId = roleId;
	}

	public UUID getRoleCatalogueId() {
		return roleCatalogueId;
	}

	public void setRoleCatalogueId(UUID roleCatalogueId) {
		this.roleCatalogueId = roleCatalogueId;
	}

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}
	
}
