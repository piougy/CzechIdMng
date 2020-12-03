package eu.bcvsolutions.idm.core.security.service.impl;

import org.activiti.engine.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.core.security.api.exception.IdmAuthenticationException;

/**
 * Default implementation of OAuth - jwt authenticate service.
 * 
 * @author svandav
 * @author Radek Tomiška
 */
@Component
public class OAuthAuthenticationManager implements AuthenticationManager {

	private TokenManager tokenManager;
	private IdmIdentityService identityService;
	private IdentityService workflowIdentityService;
	private SecurityService securityService;
	
	@Autowired
	public OAuthAuthenticationManager(
			IdmIdentityService identityService,
			IdentityService workflowIdentityService,
			SecurityService securityService,
			TokenManager tokenManager) {
		Assert.notNull(identityService, "Service is required.");
		Assert.notNull(workflowIdentityService, "Service is required.");
		Assert.notNull(securityService, "Service is required.");
		Assert.notNull(tokenManager, "Manager is required.");
		//
		this.identityService = identityService;
		this.workflowIdentityService = workflowIdentityService;
		this.securityService = securityService;
		this.tokenManager = tokenManager;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!(authentication instanceof IdmJwtAuthentication)) {
			throw new IdmAuthenticationException(
					"Unsupported granted authority " + authentication.getClass().getName());
		}
		//
		IdmJwtAuthentication idmJwtAuthentication = verifyAuthentication(authentication);
		//Set logged user to workflow engine
		workflowIdentityService.setAuthenticatedUserId(idmJwtAuthentication.getCurrentUsername());
		// set authentication
		securityService.setAuthentication(idmJwtAuthentication);
		//
		return idmJwtAuthentication;
	}
	
	/**
	 * Logout currently authenticated identity
	 */
	public void logout() {
		workflowIdentityService.setAuthenticatedUserId(null);
		securityService.logout();
	}
	
	private IdmJwtAuthentication verifyAuthentication(Authentication authentication) {
		if (!(authentication instanceof IdmJwtAuthentication)) {
			throw new UnsupportedOperationException(String.format(
					"JWT authentication is supported only, given [%s].",
					authentication.getClass().getCanonicalName()));
		}
		IdmJwtAuthentication idmJwtAuthentication = (IdmJwtAuthentication) authentication;
		//
		// verify persisted token
		boolean tokenVerified = false;
		if (idmJwtAuthentication.getId() != null) {
			// get verified (valid) token
			tokenManager.verifyToken(idmJwtAuthentication.getId());
			tokenVerified = true;
		} 
		//
		// verify given authentication (token could not be persisted)
		if (idmJwtAuthentication.isExpired()) {
			throw new ResultCodeException(CoreResultCode.AUTH_EXPIRED);
		}
		if (tokenVerified) {
			// when token is verified, then identity is not disabled => tokens are disabled after identity is disabled 
			return idmJwtAuthentication;
		}
		//
		// verify identity
		IdmIdentityDto identity = identityService.getByUsername(idmJwtAuthentication.getName());
		if (identity == null) {
			throw new IdmAuthenticationException(String.format("Identity [%s] not found!", idmJwtAuthentication.getName()));
		}
		if (identity.isDisabled()) {
			throw new IdmAuthenticationException(String.format("Identity [%s] is disabled!", identity.getId()));
		}
		//
		return idmJwtAuthentication;
	}
}
