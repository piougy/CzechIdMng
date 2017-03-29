package eu.bcvsolutions.idm.core.security.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdentityDto;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.security.api.domain.AbstractAuthentication;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.domain.DefaultGrantedAuthority;

/**
 * Default implementation of security service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSecurityService implements SecurityService {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSecurityService.class);
	
	private final ModuleService moduleService;
	
	@Autowired
	public DefaultSecurityService(ModuleService moduleService) {
		Assert.notNull(moduleService, "Module service is required!");
		
		this.moduleService = moduleService;
	}
	
	@Override
	public void setAuthentication(AbstractAuthentication authentication) {
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
	
	@Override
	public void setSystemAuthentication() {
		this.setAuthentication(new IdmJwtAuthentication(new IdentityDto("[SYSTEM]"), null, getAllAvailableAuthorities(), null));
	}
	
	@Override
	public AbstractAuthentication getAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		// TODO: support different authentications?
		if (!(authentication instanceof AbstractAuthentication)) {
			return null;
		}
		return (AbstractAuthentication) authentication;
	}
	
	@Override
	public boolean isAuthenticated() {
		Authentication authentication = getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}
		return true;
	}

	@Override
	public String getUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!isAuthenticated()) {
			return GUEST_NAME;
		}
		return authentication.getName();
	}
	
	@Override
	public String getOriginalUsername() {
		if (!isAuthenticated()) {
			return null;
		}	
		Authentication authentication = getAuthentication();
		if (authentication instanceof AbstractAuthentication) {
			return ((AbstractAuthentication) authentication).getOriginalUsername();
		}
		return null;
	}

	@Override
	public Set<String> getAllAuthorities() {
		Set<String> authorities = new HashSet<>();	
		if (!isAuthenticated()) {
			return authorities;
		}
		Authentication authentication = getAuthentication();
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			authorities.add(authority.getAuthority());
		}
		return authorities;
	}

	@Override
	public boolean hasAnyAuthority(String... authorities) {
		if (authorities == null || authorities.length == 0) {
			return false;
		}
		Set<String> requiredAuthorities = new HashSet<>(Arrays.asList(authorities));
		Set<String> allAuthorities = getAllAuthorities();
		
		boolean result = CollectionUtils.containsAny(requiredAuthorities, allAuthorities);
		log.trace("Logged identity hasAnyAuthotity [{}] evaluated to [{}]", authorities, result);
		return result;
	}
	
	/**
	 * Returns true, if logged identity has APP_ADMIN authority. Could be used for single user mode.
	 */
	@Override
	public boolean isAdmin() {
		return hasAnyAuthority(IdmGroupPermission.APP_ADMIN);
	}
	
	@Override
	public List<GroupPermission> getAvailableGroupPermissions() {
		List<GroupPermission> groupPermissions = moduleService.getAvailablePermissions();
		log.debug("Loaded available groupPermissions [size:{}]", groupPermissions.size());
		return groupPermissions;
	}
	
	@Override
	public List<GrantedAuthority> getAllAvailableAuthorities() {
		return toAuthorities(getAvailableGroupPermissions());
	}
	
	public static List<GrantedAuthority> toAuthorities(List<GroupPermission> groupPermissions) {
		Set<GrantedAuthority> authorities = new HashSet<>();
		groupPermissions.forEach(groupPermission -> {
			authorities.addAll(toAuthorities(groupPermission));				
		});
		return new ArrayList<>(authorities);
	}
	
	public static List<GrantedAuthority> toAuthorities(GroupPermission groupPermission) {
		Set<GrantedAuthority> authorities = new HashSet<>();
		groupPermission.getPermissions().forEach(basePermission -> {
			authorities.add(new DefaultGrantedAuthority(groupPermission, basePermission));
		});					
		return new ArrayList<>(authorities);
	}

}
