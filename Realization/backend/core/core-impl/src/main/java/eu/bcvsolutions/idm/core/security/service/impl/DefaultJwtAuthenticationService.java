package eu.bcvsolutions.idm.core.security.service.impl;

import java.io.IOException;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.GrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.core.security.api.service.JwtAuthenticationService;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.core.security.service.impl.JwtAuthenticationMapper;
import eu.bcvsolutions.idm.core.security.service.impl.OAuthAuthenticationManager;

/**
 * Default implementation of {@link JwtAuthenticationService}.
 * @author Alena Peterová
 *
 */
@Service("jwtAuthenticationService")
public class DefaultJwtAuthenticationService implements JwtAuthenticationService {

	private final OAuthAuthenticationManager oauthAuthenticationManager;
	
	private final ConfigurationService configurationService;
	
	private final GrantedAuthoritiesFactory grantedAuthoritiesFactory;
	
	private final JwtAuthenticationMapper jwtTokenMapper;
	
	@Autowired
	public DefaultJwtAuthenticationService(OAuthAuthenticationManager oauthAuthenticationManager,
			ConfigurationService configurationService,
			GrantedAuthoritiesFactory grantedAuthoritiesFactory,
			JwtAuthenticationMapper jwtTokenMapper) {
	
		this.oauthAuthenticationManager = oauthAuthenticationManager;
		this.configurationService = configurationService;
		this.grantedAuthoritiesFactory = grantedAuthoritiesFactory;
		this.jwtTokenMapper = jwtTokenMapper;
	}
	
	@Override
	public LoginDto createJwtAuthenticationAndAuthenticate(
			LoginDto loginDto, IdmIdentityDto identity, String module) {
		
		IdmJwtAuthentication authentication = new IdmJwtAuthentication(
				identity, 
				getAuthExpiration(),
				grantedAuthoritiesFactory.getGrantedAuthorities(loginDto.getUsername()), 
				module);

		oauthAuthenticationManager.authenticate(authentication);

		IdmJwtAuthenticationDto authenticationDto = jwtTokenMapper.toDto(authentication);

		try {
			loginDto.setAuthenticationModule(module);
			loginDto.setAuthentication(authenticationDto);
			loginDto.setToken(jwtTokenMapper.writeToken(authenticationDto));
			return loginDto;
		} catch (IOException ex) {
			throw new IdmAuthenticationException(ex.getMessage(), ex);
		}
	}

	private DateTime getAuthExpiration() {
		// new expiration date
		DateTime expiration = DateTime.now()
			.plus(configurationService.getIntegerValue(LoginService.PROPERTY_EXPIRATION_TIMEOUT, LoginService.DEFAULT_EXPIRATION_TIMEOUT));
		return expiration;
	}

}
