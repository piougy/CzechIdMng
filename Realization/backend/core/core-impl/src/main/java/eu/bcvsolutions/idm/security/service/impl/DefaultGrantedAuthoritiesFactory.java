package eu.bcvsolutions.idm.security.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdentityDto;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleAuthority;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.security.api.service.SecurityService;
import eu.bcvsolutions.idm.security.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.security.dto.DefaultGrantedAuthorityDto;
import eu.bcvsolutions.idm.security.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.security.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.security.service.GrantedAuthoritiesFactory;

/**
 * @author svandav
 */
@Component
public class DefaultGrantedAuthoritiesFactory implements GrantedAuthoritiesFactory {

	private final IdmIdentityService identityService;
	private final IdmIdentityRoleService identityRoleService;
	private final SecurityService securityService;
	
	@Autowired
	public DefaultGrantedAuthoritiesFactory(
			IdmIdentityService identityService,
			IdmIdentityRoleService identityRoleService,
			SecurityService securityService) {
		Assert.notNull(identityService);
		Assert.notNull(identityRoleService);
		Assert.notNull(securityService);
		//
		this.identityService = identityService;
		this.identityRoleService = identityRoleService;
		this.securityService = securityService;
	}

	@Override
	public List<GrantedAuthority> getGrantedAuthorities(String username) {
		IdmIdentity identity = identityService.getByUsername(username);
		if (identity == null) {
			throw new IdmAuthenticationException("Identity " + username + " not found!");
		}

		// unique set of authorities from all active identity roles and subroles
		Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
		identityRoleService.getRoles(identity).stream() //
				.filter(EntityUtils::isValid) //
				.forEach(identityRole -> {
					grantedAuthorities.addAll(getActiveRoleAuthorities(identityRole.getRole(), new HashSet<>()));
				});
		return Lists.newArrayList(grantedAuthorities);
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
		for(IdmRoleAuthority roleAuthority : role.getAuthorities()) {
			// check for wildcard permissions
			// TODO: better approach will be extend hasAuhority / hasAnyAuhority evaluation
			if (roleAuthority.getTarget().equals(IdmGroupPermission.APP.name())) {
				grantedAuthorities.addAll(securityService.getAllAvailableAuthorities());
				break;
			}
			if (roleAuthority.getAction().equals(IdmBasePermission.ADMIN.name())) {
				for(GroupPermission groupPermission : securityService.getAvailableGroupPermissions()) {
					if(groupPermission.getName().equals(roleAuthority.getTarget())) {
						groupPermission.getPermissions().forEach(basePermission -> {
							grantedAuthorities.add(new DefaultGrantedAuthority(groupPermission, basePermission));
						});
					}
				}
			} else {
				grantedAuthorities.add(new DefaultGrantedAuthority(roleAuthority.getAuthority()));
			}
		};
		// sub roles
		role.getSubRoles().forEach(subRole -> {
			if (!processedRoles.contains(subRole.getSub())) {
				grantedAuthorities.addAll(getActiveRoleAuthorities(subRole.getSub(), processedRoles));
			}
		});
		return grantedAuthorities;
	}

	@Override
	public IdmJwtAuthentication getIdmJwtAuthentication(IdmJwtAuthenticationDto dto) {
		Collection<DefaultGrantedAuthorityDto> authorities = dto.getAuthorities();
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		if (authorities != null) {
			for (DefaultGrantedAuthorityDto a : authorities) {
				grantedAuthorities.add(new DefaultGrantedAuthority(a.getAuthority()));
			}
		}
		IdmJwtAuthentication authentication = new IdmJwtAuthentication(
				new IdentityDto(dto.getCurrentIdentityId(), dto.getCurrentUsername()),
				new IdentityDto(dto.getOriginaIdentityId(), dto.getOriginalUsername()), 
				dto.getExpiration(), 
				grantedAuthorities);
		return authentication;
	}

	@Override
	@SuppressWarnings("unchecked")
	public IdmJwtAuthenticationDto getIdmJwtAuthenticationDto(IdmJwtAuthentication authentication) {
		IdmJwtAuthenticationDto authenticationDto = new IdmJwtAuthenticationDto();
		authenticationDto.setCurrentUsername(authentication.getCurrentUsername());
		authenticationDto.setCurrentIdentityId(authentication.getCurrentIdentity() == null ? null : authentication.getCurrentIdentity().getId());
		authenticationDto.setOriginalUsername(authentication.getOriginalUsername());
		authenticationDto.setOriginaIdentityId(authentication.getOriginalIdentity() == null ? null : authentication.getOriginalIdentity().getId());
		authenticationDto.setExpiration(authentication.getExpiration());
		Collection<DefaultGrantedAuthority> authorities = (Collection<DefaultGrantedAuthority>) authentication
				.getAuthorities();
		List<DefaultGrantedAuthorityDto> grantedAuthorities = new ArrayList<>();
		if (authorities != null) {
			for (DefaultGrantedAuthority a : authorities) {
				grantedAuthorities.add(new DefaultGrantedAuthorityDto(a.getAuthority()));
			}
		}
		authenticationDto.setAuthorities(grantedAuthorities);
		return authenticationDto;
	}
}
