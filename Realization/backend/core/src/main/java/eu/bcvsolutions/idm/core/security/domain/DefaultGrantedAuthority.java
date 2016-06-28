package eu.bcvsolutions.idm.core.security.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

/**
 * 	Default implementation of granted authority
 *
 * 	@author svandav
 */
public class DefaultGrantedAuthority implements GrantedAuthority {

	private static final long serialVersionUID = -5465498431654671L;
		
	private String roleName;
	
	public DefaultGrantedAuthority(String roleName) {
		Assert.notNull(roleName, "roleName must be filled");
		
		this.roleName = roleName;
	}
	
	public String getRoleName() {
		return roleName;
	}


	@Override
	public String getAuthority() {
		return roleName;
	}
	
	@Override
	public String toString() {
		return getAuthority();
	}
}
