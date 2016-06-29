package eu.bcvsolutions.idm.core.security.service.impl;

import java.util.Date;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.RestApplicationException;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.security.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;

/**
 * Default implementation of OAuth - jwt authenticate service
 * 
 * @author svandav
 */
public class OAuthAuthenticationManager implements AuthenticationManager {

	@Autowired
	private IdmIdentityRepository idmIdentityRepository;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!(authentication instanceof IdmJwtAuthentication)) {
			throw new IdmAuthenticationException(
					"Unsupported granted authority " + authentication.getClass().getName());
		}

		IdmJwtAuthentication idmJwtAuthentication = (IdmJwtAuthentication) authentication;
		Date currentDate = new Date();

		if (idmJwtAuthentication.getExpiration() == null || currentDate.after(idmJwtAuthentication.getExpiration())) {
			throw new RestApplicationException(CoreResultCode.AUTH_EXPIRED);
		}

		String usernameFromToken = idmJwtAuthentication.getName();
		IdmIdentity identity = idmIdentityRepository.findOneByUsername(usernameFromToken);
		if (identity == null) {
			throw new IdmAuthenticationException("Identity [" + usernameFromToken + "] not found!");
		}
		if (identity.isDisabled()) {
			throw new IdmAuthenticationException("Identity [" + usernameFromToken + "] is disabled!");
		}

		return authentication;
	}
}
