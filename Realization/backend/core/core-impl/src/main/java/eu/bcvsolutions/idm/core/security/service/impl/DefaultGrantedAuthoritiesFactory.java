package eu.bcvsolutions.idm.core.security.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.core.security.service.GrantedAuthoritiesFactory;

/**
 * @author svandav
 */
@Component
public class DefaultGrantedAuthoritiesFactory implements GrantedAuthoritiesFactory {

	private final IdmIdentityService identityService;
	private final IdmIdentityRoleService identityRoleService;
	private final IdmAuthorizationPolicyService authorizationPolicyService;
	private final IdmRoleService roleService;
	
	@Autowired
	public DefaultGrantedAuthoritiesFactory(
			IdmIdentityService identityService,
			IdmIdentityRoleService identityRoleService,
			IdmAuthorizationPolicyService authorizationPolicyService,
			IdmRoleService roleService) {
		Assert.notNull(identityService);
		Assert.notNull(identityRoleService);
		Assert.notNull(authorizationPolicyService);
		Assert.notNull(roleService);
		//
		this.identityService = identityService;
		this.identityRoleService = identityRoleService;
		this.authorizationPolicyService = authorizationPolicyService;
		this.roleService = roleService;
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<GrantedAuthority> getGrantedAuthorities(String username) {
		IdmIdentity identity = identityService.getByUsername(username);
		if (identity == null) {
			throw new IdmAuthenticationException("Identity " + username + " not found!");
		}
		return Lists.newArrayList(getGrantedAuthoritiesForIdentity(identity));
	}

	@Transactional
	@Override
	public Collection<GrantedAuthority> getGrantedAuthoritiesForIdentity(IdmIdentity identity) {
		return getGrantedAuthoritiesForValidRoles(identityRoleService.getRoles(identity));
	}
	
	@Transactional
	@Override
	public Collection<GrantedAuthority> getGrantedAuthoritiesForValidRoles(Collection<IdmIdentityRole> roles) {
		// unique set of authorities from all active identity roles and subroles
		Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
		roles.stream()
			.filter(EntityUtils::isValid) //
			.forEach(identityRole -> {
				grantedAuthorities.addAll(getActiveRoleAuthorities(identityRole.getRole(), new HashSet<>()));
			});
		// add default authorities
		grantedAuthorities.addAll(authorizationPolicyService.getDefaultAuthorities());
		//
		return Lists.newArrayList(trimAdminAuthorities(grantedAuthorities));
	}
	
	@Transactional
	@Override
	public Collection<GrantedAuthority> getActiveRoleAuthorities(IdmRole role) {
		return getActiveRoleAuthorities(role, new HashSet<>());
	}
	
	/**
	 * Returns authorities from active role and active role's subRoles 
	 * 
	 * @param role
	 * @param processedRoles
	 * @return
	 */
	private Set<GrantedAuthority> getActiveRoleAuthorities(IdmRole role, Set<IdmRole> processedRoles) {
		processedRoles.add(role);
		Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
		if (role.isDisabled()) {
			return grantedAuthorities;
		}
		grantedAuthorities.addAll(authorizationPolicyService.getEnabledRoleAuthorities(role.getId()));
		// sub roles
		roleService.getSubroles(role.getId()).forEach(subRole -> {
			if (!processedRoles.contains(subRole)) {
				grantedAuthorities.addAll(getActiveRoleAuthorities(subRole, processedRoles));
			}
		});		
		return grantedAuthorities;
	}
	
	/**
	 * trims redundant authorities
	 * 
	 * @param authorities
	 * @return
	 */
	private Set<GrantedAuthority> trimAdminAuthorities(Set<GrantedAuthority> authorities) {
		if (authorities.contains(new DefaultGrantedAuthority(IdmGroupPermission.APP_ADMIN))) {
			return Sets.newHashSet(new DefaultGrantedAuthority(IdmGroupPermission.APP_ADMIN));
		}
		Set<GrantedAuthority> trimmedAuthorities = new HashSet<>();
		authorities.forEach(grantedAuthority -> {
			String authority = grantedAuthority.getAuthority();
			if (authority.endsWith(IdmAuthorityHierarchy.ADMIN_SUFFIX)) {
				trimmedAuthorities.add(grantedAuthority);
			} else {
				String groupName = IdmAuthorityHierarchy.getGroupName(authority);
				if (!authorities.contains(new DefaultGrantedAuthority(groupName, IdmBasePermission.ADMIN.getName()))) {
					trimmedAuthorities.add(grantedAuthority);
				}				
			}	
			
		});
		return trimmedAuthorities;
	}

	@Override
	public boolean containsAllAuthorities(
			Collection<GrantedAuthority> original, Collection<GrantedAuthority> subset) {
		return original.containsAll(subset);
	}
	
}
