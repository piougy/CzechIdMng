package eu.bcvsolutions.idm.core.security.service.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.security.api.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.service.GrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;

/**
 * Load identity's granted authorities
 * 
 * @author svandav
 * @author Radek Tomiška
 */
@Component("grantedAuthoritiesFactory")
public class DefaultGrantedAuthoritiesFactory implements GrantedAuthoritiesFactory {

	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	
	@Override
	public List<GrantedAuthority> getGrantedAuthorities(String username) {
		IdmIdentityDto identity = identityService.getByUsername(username);
		if (identity == null) {
			throw new IdmAuthenticationException("Identity " + username + " not found!");
		}
		return Lists.newArrayList(getGrantedAuthoritiesForIdentity(identity.getId()));
	}

	@Override
	public Collection<GrantedAuthority> getGrantedAuthoritiesForIdentity(UUID identityId) {
		return getGrantedAuthoritiesForValidRoles(identityId, identityRoleService.findValidRoles(identityId, null).getContent());
	}
	
	@Override
	public Collection<GrantedAuthority> getGrantedAuthoritiesForValidRoles(UUID identityId, Collection<IdmIdentityRoleDto> identityRoles) {
		// unique set of authorities from all active identity roles and subroles
		Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
		identityRoles.stream()
			.filter(EntityUtils::isValid) // valid identity role
			.filter(ir -> { // valid role's contract
				IdmIdentityContractDto contract = DtoUtils.getEmbedded(ir, IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT);
				return contract.isValid() && contract.getState() != ContractState.EXCLUDED;
			})
			.forEach(identityRole -> {
				IdmRoleDto role = DtoUtils.getEmbedded(identityRole, IdmIdentityRoleDto.PROPERTY_ROLE, (IdmRoleDto) null);
				if (role == null) {
					role = roleService.get(identityRole.getRole());
				}
				grantedAuthorities.addAll(getActiveRoleAuthorities(identityId, role, new HashSet<>()));
			});
		// add default authorities
		grantedAuthorities.addAll(authorizationPolicyService.getDefaultAuthorities(identityId));
		//
		return Lists.newArrayList(trimAdminAuthorities(grantedAuthorities))
				.stream()
			    .sorted(Comparator.comparing(GrantedAuthority::getAuthority))
			    .collect(Collectors.toList());
	}

	@Override
	public Collection<GrantedAuthority> getActiveRoleAuthorities(UUID identityId, IdmRoleDto role) {
		return getActiveRoleAuthorities(identityId, role, new HashSet<>());
	}
	
	@Override
	public boolean containsAllAuthorities(
			Collection<? extends GrantedAuthority> original,
			Collection<? extends GrantedAuthority> subset) {
		return original.containsAll(subset);
	}
	
	/**
	 * Returns authorities from active role and active role's subRoles 
	 * 
	 * @param role
	 * @param processedRoles
	 * @return
	 */
	private Set<GrantedAuthority> getActiveRoleAuthorities(UUID identityId, IdmRoleDto role, Set<IdmRoleDto> processedRoles) {
		processedRoles.add(role);
		//
		Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
		if (role.isDisabled()) {
			return grantedAuthorities;
		}
		grantedAuthorities.addAll(authorizationPolicyService.getEnabledRoleAuthorities(identityId, role.getId()));
		//
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
}
