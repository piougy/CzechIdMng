package eu.bcvsolutions.idm.core.security.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Adds admin wildcard roles APP_ADMIN > *, ROLE_ADMIN > ROLE_*
 * 
 * TODO: group permission cache
 * 
 * @author Radek Tomiška
 *
 */
public class IdmAuthorityHierarchy implements RoleHierarchy {
	
	private final SecurityService securityService;
	public final String ADMIN_SUFFIX = String.format("_%s", IdmBasePermission.ADMIN);
	
	public IdmAuthorityHierarchy(SecurityService securityService) {
		Assert.notNull(securityService);
		//
		this.securityService = securityService;
	}

	@Override
	public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(
			Collection<? extends GrantedAuthority> authorities) {
		if (authorities == null || authorities.isEmpty()) {
			return AuthorityUtils.NO_AUTHORITIES;
		}
		//
		Set<GrantedAuthority> reachableRoles = new HashSet<GrantedAuthority>();
		for(GrantedAuthority grantedAuthority : authorities) {
			String authority = grantedAuthority.getAuthority();
			//
			if (authority.equals(IdmGroupPermission.APP_ADMIN)) {
				// super admin has all available authorities
				return securityService.getAvailableAuthorities();
			}
			reachableRoles.add(grantedAuthority);
			if (authority.endsWith(ADMIN_SUFFIX)) {
				String groupName = authority.substring(0, - ADMIN_SUFFIX.length());
				for (GroupPermission groupPermission : securityService.getAvailableGroupPermissions()) {
					if (groupPermission.getName().equals(groupName)) {
						reachableRoles.addAll(DefaultSecurityService.toAuthorities(groupPermission));
						break;
					}
				}
			}			
		}		
		return Collections.unmodifiableCollection(reachableRoles);
	}

}
