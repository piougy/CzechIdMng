package eu.bcvsolutions.idm.core.security.service.impl;

import java.text.MessageFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.exception.TwoFactorAuthenticationRequiredException;
import eu.bcvsolutions.idm.core.security.api.service.JwtAuthenticationService;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.core.security.api.service.TwoFactorAuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.exception.IdmAuthenticationException;

/**
 * Default implementation of {@link JwtAuthenticationService}.
 * 
 * @author Alena Peterová
 * @author Radek Tomiška
 */
@Service("jwtAuthenticationService")
public class DefaultJwtAuthenticationService implements JwtAuthenticationService {

	@Autowired private OAuthAuthenticationManager oauthAuthenticationManager;
	@Autowired private JwtAuthenticationMapper jwtTokenMapper;
	@Autowired private TwoFactorAuthenticationManager twoFactorAuthenticationManager;
	@Autowired private TokenManager tokenManager;
	
	@Override
	public LoginDto createJwtAuthenticationAndAuthenticate(LoginDto loginDto, IdmIdentityDto identity, String moduleId) {
		IdmTokenDto preparedToken = new IdmTokenDto();
		preparedToken.setModuleId(moduleId);
		return createJwtAuthenticationAndAuthenticate(loginDto, identity, preparedToken);
	}
	
	@Override
	public LoginDto createJwtAuthenticationAndAuthenticate(LoginDto loginDto, IdmIdentityDto identity, IdmTokenDto preparedToken) {
		Assert.notNull(identity, "Identity is required.");
		UUID identityId = identity.getId();
		Assert.notNull(identityId, "Identity identifier is required.");
		//
		// check identity is valid
		if (identity.isDisabled()) {
			throw new IdmAuthenticationException(
					MessageFormat.format("Check identity can login: The identity [{0}] is disabled.", identity.getUsername())
			);
		}
		//
		// two factor authentication is not configured for given identity
		if (tokenManager.isNew(preparedToken)) {
			if (loginDto.isSkipMustChange() // public password change page => login is needed, before password is changed
					|| twoFactorAuthenticationManager.getTwoFactorAuthenticationType(identityId) == null) {
				preparedToken.setSecretVerified(true);
			} else {
				// two factor needed
				preparedToken.setSecretVerified(false);
			}
		}
		// create token
		IdmTokenDto token = jwtTokenMapper.createToken(identity, preparedToken);
		//
		// check two factor authentication is required
		if (twoFactorAuthenticationManager.requireTwoFactorAuthentication(identityId, token.getId())) {
			IdmJwtAuthenticationDto authenticationDto = jwtTokenMapper.toDto(token);
			// token is needed in exception => sso login can be used and client doesn't have token
			throw new TwoFactorAuthenticationRequiredException(jwtTokenMapper.writeToken(authenticationDto));
		}
		//
		return login(loginDto, token);
	}
	
	/**
	 * Create login response with filled token and authorities.
	 * 
	 * @param loginDto login request
	 * @param token cidmst token
	 * @return login response 
	 */
	private LoginDto login(LoginDto loginDto, IdmTokenDto token) {
		IdmJwtAuthentication authentication = jwtTokenMapper.fromDto(token);
		//
		oauthAuthenticationManager.authenticate(authentication);
		//
		LoginDto result = new LoginDto();
		result.setUsername(loginDto.getUsername());
		result.setSkipMustChange(loginDto.isSkipMustChange());
		result.setPassword(loginDto.getPassword());
		result.setAuthenticationModule(token.getModuleId());
		IdmJwtAuthenticationDto authenticationDto = jwtTokenMapper.toDto(token);
		result.setAuthentication(authenticationDto);
		result.setToken(jwtTokenMapper.writeToken(authenticationDto));
		result.setAuthorities(jwtTokenMapper.getDtoAuthorities(token));
		//
		return result;
	}
	
	@Override
	public void logout(IdmTokenDto token) {
		if (token != null) {
			// disable token if given
			jwtTokenMapper.disableToken(token.getId());
		}
		// clear security context
		oauthAuthenticationManager.logout();
	}
}
