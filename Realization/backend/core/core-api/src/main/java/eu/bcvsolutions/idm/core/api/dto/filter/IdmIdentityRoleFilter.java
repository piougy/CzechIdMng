package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;

/**
 * Filter for identity role
 *
 * @author svandav
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class IdmIdentityRoleFilter extends DataFilter implements ExternalIdentifiable {
	
	private UUID roleId;
    private UUID identityId;
    private UUID roleCatalogueId;
    private Boolean valid;
    private Boolean automaticRole;
    private UUID automaticRoleId;
    private UUID identityContractId;
    
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

	public Boolean getAutomaticRole() {
		return automaticRole;
	}

	public void setAutomaticRole(Boolean automaticRole) {
		this.automaticRole = automaticRole;
	}

	public UUID getAutomaticRoleId() {
		return automaticRoleId;
	}

	public void setAutomaticRoleId(UUID automaticRoleId) {
		this.automaticRoleId = automaticRoleId;
	}

	public UUID getIdentityContractId() {
		return identityContractId;
	}

	public void setIdentityContractId(UUID identityContractId) {
		this.identityContractId = identityContractId;
	}
	
	@Override
	public String getExternalId() {
		return (String) data.getFirst(PROPERTY_EXTERNAL_ID);
	}
	
	@Override
	public void setExternalId(String externalId) {
		data.set(PROPERTY_EXTERNAL_ID, externalId);
	}
	
}
