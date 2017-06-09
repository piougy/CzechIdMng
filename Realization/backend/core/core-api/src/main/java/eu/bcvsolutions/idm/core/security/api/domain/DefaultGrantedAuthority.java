package eu.bcvsolutions.idm.core.security.api.domain;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

/**
 * 	Default implementation of granted authority
 *
 * 	@author svandav
 */
public class DefaultGrantedAuthority implements GrantedAuthority {

	private static final long serialVersionUID = -5465498431654671L;
		
	private String authority;
	
	public DefaultGrantedAuthority(String authority) {
		Assert.notNull(authority, "roleName must be filled");		
		this.authority = authority;
	}
	
	/**
	 * Construct authority from given permissions (e.g. group_base)
	 * 
	 * @param permissions
	 */
	public DefaultGrantedAuthority(BasePermission... permissions) {
		this(StringUtils.join(permissions, BasePermission.SEPARATOR));
	}
	
	/**
	 * Construct authority from given permissions (e.g. group_base)
	 * 
	 * @param permissions
	 */
	public DefaultGrantedAuthority(String... permissions) {
		this(StringUtils.join(permissions, BasePermission.SEPARATOR));
	}

	@Override
	public String getAuthority() {
		return authority;
	}
	
	@Override
	public String toString() {
		return getAuthority();
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(getAuthority());
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || !object.getClass().equals(getClass())) {
			return false;
		}
		DefaultGrantedAuthority other = (DefaultGrantedAuthority) object;
		return Objects.equals(this.getAuthority(), other.getAuthority());
	}
}
