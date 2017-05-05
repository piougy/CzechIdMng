package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

/**
 * Filter for asigned evaluators to roles
 *
 * @author Radek Tomiška
 */
public class AuthorizationPolicyFilter implements BaseFilter {

    private UUID roleId;
    private Boolean disabled;
    private String authorizableType;

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
