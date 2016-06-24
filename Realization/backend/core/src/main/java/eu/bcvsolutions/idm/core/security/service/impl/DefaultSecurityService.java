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
class DefaultSecurityService implements SecurityService {

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
	public Set<String> getAllRoleNames() {
		Set<String> roleNames = new HashSet<>();
		Authentication authentication = getAuthentication();		
		if (!authentication.isAuthenticated()) {
			return roleNames;
		}
		for (GrantedAuthority authority : getAuthentication().getAuthorities()) {
			roleNames.add(authority.getAuthority());
		}
		return roleNames;
	}

	@Override
	public boolean hasAnyRole(String... roleNames) {
		if (roleNames == null || roleNames.length == 0) {
			return false;
		}
		Set<String> requiredRoleNames = new HashSet<>(Arrays.asList(roleNames));
		Set<String> allRoleNames = getAllRoleNames();

		return CollectionUtils.containsAny(requiredRoleNames, allRoleNames);
	}

}
