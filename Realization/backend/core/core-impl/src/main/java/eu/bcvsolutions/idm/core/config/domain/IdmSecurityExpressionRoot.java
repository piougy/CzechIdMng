package eu.bcvsolutions.idm.core.config.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;

/**
 * Adds admin evaluation
 * 
 * @author Radek Tomiška
 *
 */
public class IdmSecurityExpressionRoot implements SecurityExpressionOperations {
	
	protected final Authentication authentication;
	private AuthenticationTrustResolver trustResolver;
	private Set<String> roles;
	private PermissionEvaluator permissionEvaluator;

	/**
	 * Creates a new instance
	 * @param authentication the {@link Authentication} to use. Cannot be null.
	 */
	public IdmSecurityExpressionRoot(Authentication authentication) {
		Assert.notNull(authentication);
		//
		this.authentication = authentication;
	}

	@Override
	public boolean hasAuthority(String authority) {
		return hasAnyAuthority(authority);
	}

	@Override
	public boolean hasAnyAuthority(String... authorities) {
		return hasAnyRole(authorities);
	}

	@Override
	public boolean hasRole(String role) {
		return hasAnyRole(role);
	}
	
	@Override
	public boolean hasAnyRole(String... roles) {
		Set<String> roleSet = getAuthoritySet();
		if (roleSet.contains(IdmGroupPermission.APP_ADMIN)) {
			return true;
		}

		for (String role : roles) {
			if (roleSet.contains(role)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Authentication getAuthentication() {
		return authentication;
	}

	@Override
	public boolean permitAll() {
		return true;
	}

	@Override
	public boolean denyAll() {
		return false;
	}

	@Override
	public boolean isAnonymous() {
		return trustResolver.isAnonymous(authentication);
	}
	
	@Override
	public boolean isAuthenticated() {
		return !isAnonymous();
	}

	@Override
	public boolean isRememberMe() {
		return trustResolver.isRememberMe(authentication);
	}

	@Override
	public boolean isFullyAuthenticated() {
		return !trustResolver.isAnonymous(authentication)
				&& !trustResolver.isRememberMe(authentication);
	}

	/**
	 * Convenience method to access {@link Authentication#getPrincipal()} from
	 * {@link #getAuthentication()}
	 * @return
	 */
	public Object getPrincipal() {
		return authentication.getPrincipal();
	}

	public void setTrustResolver(AuthenticationTrustResolver trustResolver) {
		this.trustResolver = trustResolver;
	}


	private Set<String> getAuthoritySet() {
		if (roles == null) {
			roles = new HashSet<String>();
			Collection<? extends GrantedAuthority> userAuthorities = authentication
					.getAuthorities();
			roles = AuthorityUtils.authorityListToSet(userAuthorities);
		}
		return roles;
	}

	@Override
	public boolean hasPermission(Object target, Object permission) {
		return permissionEvaluator.hasPermission(authentication, target, permission);
	}

	@Override
	public boolean hasPermission(Object targetId, String targetType, Object permission) {
		return permissionEvaluator.hasPermission(authentication, (Serializable) targetId,
				targetType, permission);
	}
	
	public void setPermissionEvaluator(PermissionEvaluator permissionEvaluator) {
		this.permissionEvaluator = permissionEvaluator;
	}
}
