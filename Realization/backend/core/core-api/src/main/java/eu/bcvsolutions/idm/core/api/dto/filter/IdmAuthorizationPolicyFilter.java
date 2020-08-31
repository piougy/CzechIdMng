package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for asigned evaluators to roles
 *
 * @author Radek Tomiška
 */
public class IdmAuthorizationPolicyFilter extends DataFilter implements DisableableFilter {
	
	public static final String PARAMETER_GROUP_PERMISSION = "groupPermission";
	public static final String PARAMETER_ROLE_ID = "roleId";
	public static final String PARAMETER_AUTHORIZABLE_TYPE = "authorizableType";
	/**
	 * Authorization policy assigned by given identity by assigned or default role.
	 * 
	 * @since 10.6.0
	 * @see RoleConfiguration#getDefaultRole()
	 * @see IdmIdentityRoleDto
	 */
    public static final String PARAMETER_IDENTITY_ID = "identityId";
    
    public IdmAuthorizationPolicyFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmAuthorizationPolicyFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmAuthorizationPolicyFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmAuthorizationPolicyDto.class, data, parameterConverter);
	}

    public UUID getRoleId() {
    	return getParameterConverter().toUuid(getData(), PARAMETER_ROLE_ID);
    }

    public void setRoleId(UUID roleId) {
    	set(PARAMETER_ROLE_ID, roleId);
    }

    public void setAuthorizableType(String authorizableType) {
    	set(PARAMETER_AUTHORIZABLE_TYPE, authorizableType);
    }

    public String getAuthorizableType() {
    	return getParameterConverter().toString(getData(), PARAMETER_AUTHORIZABLE_TYPE);
    }
    
    public void setGroupPermission(String groupPermission) {
    	set(PARAMETER_GROUP_PERMISSION, groupPermission);
	}
    
    public String getGroupPermission() {
    	return getParameterConverter().toString(getData(), PARAMETER_GROUP_PERMISSION);
	}
    
    public void setIdentityId(UUID identityId) {
    	set(PARAMETER_IDENTITY_ID, identityId);
	}
    
    public UUID getIdentityId() {
    	return getParameterConverter().toUuid(getData(), PARAMETER_IDENTITY_ID);
	}
}
