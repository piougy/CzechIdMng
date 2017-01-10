package eu.bcvsolutions.idm.security.service.impl;

import org.activiti.engine.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.security.api.service.SecurityService;
import eu.bcvsolutions.idm.security.exception.IdmAuthenticationException;

/**
 * Default implementation of OAuth - jwt authenticate service
 * 
 * @author svandav
 */
public class OAuthAuthenticationManager implements AuthenticationManager {

	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	private IdentityService workflowIdentityService;
	
	@Autowired
	private SecurityService securityService;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!(authentication instanceof IdmJwtAuthentication)) {
			throw new IdmAuthenticationException(
					"Unsupported granted authority " + authentication.getClass().getName());
		}

		IdmJwtAuthentication idmJwtAuthentication = (IdmJwtAuthentication) authentication;

		if (idmJwtAuthentication.isExpired()) {
			throw new ResultCodeException(CoreResultCode.AUTH_EXPIRED);
		}

		String usernameFromToken = idmJwtAuthentication.getName();
		IdmIdentity identity = identityService.getByUsername(usernameFromToken);
		if (identity == null) {
			throw new IdmAuthenticationException("Identity [" + usernameFromToken + "] not found!");
		}
		if (identity.isDisabled()) {
			throw new IdmAuthenticationException("Identity [" + usernameFromToken + "] is disabled!");
		}
		//
		// TODO: this is on wrong place ... shuld be outside in login service etc.
		//
		//Set logged user to workflow engine
		workflowIdentityService.setAuthenticatedUserId(identity.getUsername());
		// set authentication
		securityService.setAuthentication(idmJwtAuthentication);
		//
		return idmJwtAuthentication;
	}
}
