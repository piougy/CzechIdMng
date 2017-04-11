package eu.bcvsolutions.idm.core.security.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;

/**
 * Adds admin wildcard roles APP_ADMIN > *, ROLE_ADMIN > ROLE_*
 * 
 * TODO: group permission cache
 * 
 * @author Radek Tomiška
 *
 */
public class IdmAuthorityHierarchy implements RoleHierarchy {
	
	private final ModuleService moduleService;
	public static final String ADMIN_SUFFIX = String.format("_%s", IdmBasePermission.ADMIN);
	
	public IdmAuthorityHierarchy(ModuleService moduleService) {
		Assert.notNull(moduleService);
		//
		this.moduleService = moduleService;
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
				return IdmAuthorityUtils.toAuthorities(moduleService.getAvailablePermissions());
			}
			reachableRoles.add(grantedAuthority);
			if (authority.endsWith(ADMIN_SUFFIX)) {
				String groupName = getGroupName(authority);
				for (GroupPermission groupPermission : moduleService.getAvailablePermissions()) {
					if (groupPermission.getName().equals(groupName)) {
						reachableRoles.addAll(IdmAuthorityUtils.toAuthorities(groupPermission));
						break;
					}
				}
			}			
		}		
		return Collections.unmodifiableCollection(reachableRoles);
	}
	
	/**
	 * Returns group permission name from given authority.
	 * 
	 * TODO: Move to utils
	 * 
	 * @param authority
	 * @return
	 */
	public static String getGroupName(String authority) {
		return authority.substring(0, authority.lastIndexOf('_'));
	}

}
