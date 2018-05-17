package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;

/**
 * Filter for asigned evaluators to roles
 *
 * @author Radek Tomiška
 */
public class IdmAuthorizationPolicyFilter extends DataFilter {

    private UUID roleId;
    private Boolean disabled;
    private String authorizableType;
    
    public IdmAuthorizationPolicyFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmAuthorizationPolicyFilter(MultiValueMap<String, Object> data) {
		super(IdmAuthorizationPolicyDto.class, data);
	}

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public void setAuthorizableType(String authorizableType) {
        this.authorizableType = authorizableType;
    }

    public String getAuthorizableType() {
        return authorizableType;
    }
}
