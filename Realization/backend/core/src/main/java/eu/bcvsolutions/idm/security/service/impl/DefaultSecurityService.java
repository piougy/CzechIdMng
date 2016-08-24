package eu.bcvsolutions.idm.security.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.model.domain.CustomGroupPermission;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.security.domain.AbstractAuthentication;
import eu.bcvsolutions.idm.security.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.security.domain.GroupPermission;
import eu.bcvsolutions.idm.security.service.SecurityService;

/**
 * Default implementation of security service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSecurityService implements SecurityService {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSecurityService.class);

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
	
	@Override
	public List<GroupPermission> getAvailableGroupPermissions() {
		//
		// TODO: SPI / osgi for register module permissions
		List<GroupPermission> groupPermissions = new ArrayList<>();
		groupPermissions.addAll(Arrays.asList(IdmGroupPermission.values()));
		groupPermissions.addAll(Arrays.asList(CustomGroupPermission.values()));
		groupPermissions.addAll(Arrays.asList(NotificationGroupPermission.values()));
		log.debug("Loaded available groupPermissions [size:{}]", groupPermissions.size());
		return groupPermissions;
	}
	
	@Override
	public List<GrantedAuthority> getAvailableAuthorities() {
		Set<GrantedAuthority> authorities = new HashSet<>();
		getAvailableGroupPermissions().forEach(groupPermission -> {
			groupPermission.getPermissions().forEach(basePermission -> {
				authorities.add(new DefaultGrantedAuthority(groupPermission, basePermission));
			});					
			
		});
		return new ArrayList<>(authorities);
	}

}
