package eu.bcvsolutions.idm.core.security.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.JwtAuthenticationService;

/**
 * Default implementation of {@link JwtAuthenticationService}.
 * 
 * TODO: logic could be moved into {@link OAuthAuthenticationManager}? - login / logout - token is constructed internally all time
 * 
 * @author Alena Peterová
 * @author Radek Tomiška
 */
@Service("jwtAuthenticationService")
public class DefaultJwtAuthenticationService implements JwtAuthenticationService {

	@Autowired private OAuthAuthenticationManager oauthAuthenticationManager;
	@Autowired private JwtAuthenticationMapper jwtTokenMapper;
	
	@Override
	public LoginDto createJwtAuthenticationAndAuthenticate(LoginDto loginDto, IdmIdentityDto identity, String moduleId) {
		IdmTokenDto preparedToken = new IdmTokenDto();
		preparedToken.setModuleId(moduleId);
		return createJwtAuthenticationAndAuthenticate(loginDto, identity, preparedToken);
	}
	
	@Override
	public LoginDto createJwtAuthenticationAndAuthenticate(LoginDto loginDto, IdmIdentityDto identity, IdmTokenDto preparedToken) {
		IdmTokenDto token = jwtTokenMapper.createToken(identity, preparedToken);
		//
		return login(loginDto, token);
	}
	
	/**
	 * TODO: immutable result ... create new result dto - e.g. LoginResponse ...
	 * TODO: then move logic into oauthAuthenticationManager ...   LoginResponse login(token);
	 * 
	 * @param loginDto
	 * @param token
	 * @return
	 */
	private LoginDto login(LoginDto loginDto, IdmTokenDto token) {
		IdmJwtAuthentication authentication = jwtTokenMapper.fromDto(token);
		//
		oauthAuthenticationManager.authenticate(authentication);
		//
		loginDto.setAuthenticationModule(token.getModuleId());
		IdmJwtAuthenticationDto authenticationDto = jwtTokenMapper.toDto(token);
		loginDto.setAuthentication(authenticationDto);
		loginDto.setToken(jwtTokenMapper.writeToken(authenticationDto));
		loginDto.setAuthorities(jwtTokenMapper.getDtoAuthorities(token));
		return loginDto;
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
