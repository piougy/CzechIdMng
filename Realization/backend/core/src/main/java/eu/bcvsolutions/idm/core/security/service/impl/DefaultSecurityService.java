package eu.bcvsolutions.idm.core.security.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.security.domain.AbstractAuthentication;
import eu.bcvsolutions.idm.core.security.service.SecurityService;

/**
 * Default implementation of security service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSecurityService implements SecurityService {

	@Override
	public String getUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof AbstractAuthentication) {
			return ((AbstractAuthentication) authentication).getCurrentUsername();
		}
		return null;
	}
	
	@Override
	public String getOriginalUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication instanceof AbstractAuthentication) {
			return ((AbstractAuthentication) authentication).getOriginalUsername();
		}
		return null;
	}

	@Override
	public AbstractAuthentication getAuthentication() {
		return (AbstractAuthentication) SecurityContextHolder.getContext().getAuthentication();
	}

	@Override
	public Set<String> getAllAuthorities() {
		Set<String> authorities = new HashSet<>();
		Authentication authentication = getAuthentication();		
		if (!authentication.isAuthenticated()) {
			return authorities;
		}
		for (GrantedAuthority authority : getAuthentication().getAuthorities()) {
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

		return CollectionUtils.containsAny(requiredAuthorities, allAuthorities);
	}

}
