package eu.bcvsolutions.idm.core.security.service.impl;

import org.activiti.engine.IdentityService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorityChange;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorityChangeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;

/**
 * Default implementation of OAuth - jwt authenticate service
 * 
 * @author svandav
 */
@Component
public class OAuthAuthenticationManager implements AuthenticationManager {

	private IdmAuthorityChangeRepository authorityChangeRepo;
	private IdmIdentityService identityService;
	private IdentityService workflowIdentityService;
	private SecurityService securityService;
	
	@Autowired
	public OAuthAuthenticationManager(IdmIdentityService identityService,
			IdentityService workflowIdentityService,
			SecurityService securityService,
			IdmAuthorityChangeRepository authorityChangeRepo) {
		this.identityService = identityService;
		this.workflowIdentityService = workflowIdentityService;
		this.securityService = securityService;
		this.authorityChangeRepo = authorityChangeRepo;
	}


	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!(authentication instanceof IdmJwtAuthentication)) {
			throw new IdmAuthenticationException(
					"Unsupported granted authority " + authentication.getClass().getName());
		}

		IdmJwtAuthentication idmJwtAuthentication = (IdmJwtAuthentication) authentication;
		IdmIdentity identity = getIdentityForToken(idmJwtAuthentication);
		IdmAuthorityChange authChange = getIdentityAuthorityChange(identity);
		checkIssuedTime(idmJwtAuthentication.getIssuedAt(), authChange);
		checkExpirationTime(idmJwtAuthentication);
		checkDisabled(identity);

		//Set logged user to workflow engine
		workflowIdentityService.setAuthenticatedUserId(identity.getUsername());
		// set authentication
		securityService.setAuthentication(idmJwtAuthentication);
		//
		return idmJwtAuthentication;
	}

	public void checkIssuedTime(DateTime issuedAt, IdmAuthorityChange ac) {
		if (ac != null && !ac.isAuthorizationValid(issuedAt)) {
			throw new ResultCodeException(CoreResultCode.AUTHORITIES_CHANGED);
		}
	}

	public void checkExpirationTime(IdmJwtAuthentication idmJwtAuthentication) {
		if (idmJwtAuthentication.isExpired()) {
			throw new ResultCodeException(CoreResultCode.AUTH_EXPIRED);
		}
	}
	
	public void checkDisabled(IdmIdentity i) {
		if (i.isDisabled()) {
			throw new IdmAuthenticationException("Identity [" + i.getUsername() + "] is disabled!");
		}
	}

	private IdmIdentity getIdentityForToken(IdmJwtAuthentication idmJwtAuthentication) {
		IdmIdentity identity = identityService.getByUsername(idmJwtAuthentication.getName());
		if (identity == null) {
			throw new IdmAuthenticationException("Identity [" + idmJwtAuthentication.getName() + "] not found!");
		}
		return identity;
	}
	
	private IdmAuthorityChange getIdentityAuthorityChange(IdmIdentity identity) {
		return authorityChangeRepo.findByIdentity(identity);
	}
	
}
