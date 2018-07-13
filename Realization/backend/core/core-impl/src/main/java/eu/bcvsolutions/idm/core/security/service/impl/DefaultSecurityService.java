package eu.bcvsolutions.idm.core.security.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.security.api.domain.AbstractAuthentication;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;

/**
 * Default implementation of security service
 * 
 * @author svandav
 *
 */
public class DefaultSecurityService implements SecurityService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSecurityService.class);
	private final RoleHierarchy authorityHierarchy;
		
	@Autowired
	public DefaultSecurityService(RoleHierarchy authorityHierarchy) {
		Assert.notNull(authorityHierarchy, "Authority hierarchy is required!");
		
		this.authorityHierarchy = authorityHierarchy;
	}
	
	@Override
	public void setAuthentication(AbstractAuthentication authentication) {
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
	
	@Override
	public void setSystemAuthentication() {
		this.setAuthentication(new IdmJwtAuthentication(new IdmIdentityDto(SYSTEM_NAME), null, getAdminAuthorities(), null));
	}
	
	@Override
	public void logout() {
		// setAuthentication(null);
		SecurityContextHolder.clearContext();
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
	public UUID getId() {
		if (!isAuthenticated()) {
			return null;
		}
		Authentication authentication = getAuthentication();
		if (authentication instanceof AbstractAuthentication) {
			return ((AbstractAuthentication) authentication).getId();
		}
		return null;
	}
	
	@Override
	public String getCurrentUsername() {
		return getUsername();
	}
	
	@Override
	public UUID getCurrentId() {
		if (!isAuthenticated()) {
			return null;
		}
		Authentication authentication = getAuthentication();
		if (authentication instanceof AbstractAuthentication) {
			return ((AbstractAuthentication) authentication).getCurrentIdentity().getId();
		}
		return null;
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
		//
		return AuthorityUtils.authorityListToSet(authorityHierarchy.getReachableGrantedAuthorities(authentication.getAuthorities()));
	}

	@Override
	public boolean hasAnyAuthority(String... authorities) {
		if (authorities == null || authorities.length == 0) {
			return false;
		}
		Set<String> requiredAuthorities = new HashSet<>(Arrays.asList(authorities));
		Set<String> allAuthorities = getAllAuthorities();
		
		boolean result = CollectionUtils.containsAny(requiredAuthorities, allAuthorities);
		LOG.trace("Logged identity hasAnyAuthotity [{}] evaluated to [{}]", authorities, result);
		return result;
	}

	@Override
	public boolean isAdmin() {
		return hasAnyAuthority(IdmAuthorityUtils.getAdminAuthority().getAuthority());
	}
	
	private ArrayList<GrantedAuthority> getAdminAuthorities() {
		return Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority());
	}
	
}
