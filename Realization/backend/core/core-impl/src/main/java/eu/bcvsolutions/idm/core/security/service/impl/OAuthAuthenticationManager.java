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
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;

/**
 * Default implementation of OAuth - jwt authenticate service
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
		Assert.notNull(identityService);
		Assert.notNull(workflowIdentityService);
		Assert.notNull(securityService);
		Assert.notNull(tokenManager);
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
		IdmIdentityDto identity = null;
		//
		// verify persisted token
		if (idmJwtAuthentication.getId() != null) {
			// get verified (valid) token
			IdmTokenDto token = tokenManager.verifyToken(idmJwtAuthentication.getId());
			// valid identity - token manager doesn't know about owner validity
			identity = identityService.get(token.getOwnerId());
		}
		//
		// verify given authentication (token could not be persisted)
		if (idmJwtAuthentication.isExpired()) {
			throw new ResultCodeException(CoreResultCode.AUTH_EXPIRED);
		}
		if (identity == null) { // identity given by id in token has higher priority
			identity = identityService.getByUsername(idmJwtAuthentication.getName());
		}
		//
		// verify identity
		if (identity == null) {
			throw new IdmAuthenticationException("Identity [" + idmJwtAuthentication.getName() + "] not found!");
		}
		if (identity.isDisabled()) {
			throw new IdmAuthenticationException(String.format("Identity [%s] is disabled!", identity.getId()));
		}
		//
		return idmJwtAuthentication;
	}
}
