package eu.bcvsolutions.idm.core.security.service.impl;

import java.text.MessageFormat;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.JwtAuthenticationService;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;

/**
 * Default login service
 * 
 * @author svandav
 * @author Radek Tomiška
 */
@Service("loginService")
public class DefaultLoginService implements LoginService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultLoginService.class);

	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private JwtAuthenticationService jwtAuthenticationService;
	@Autowired
	private IdmPasswordService passwordService;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private TokenManager tokenManager;
	@Autowired
	private JwtAuthenticationMapper jwtTokenMapper;
	@Autowired 
	private OAuthAuthenticationManager oAuthAuthenticationManager;

	@Override
	public LoginDto login(LoginDto loginDto) {
		String username = loginDto.getUsername();
		LOG.info("Identity with username [{}] authenticating", username);
		
		IdmIdentityDto identity = identityService.getByUsername(username);
		// identity exists
		if (identity == null) {			
			throw new IdmAuthenticationException(MessageFormat.format(
					"Check identity can login: The identity "
					+ "[{0}] either doesn't exist or is deleted.",
					username));
		}
		// validate identity
		if (!validate(identity, loginDto)) {
			LOG.debug("Username or password for identity [{}] is not correct!", username);			
			throw new IdmAuthenticationException(MessageFormat.format(
					"Check identity password: Failed for identity "
					+ "{0} because the password digests differ.",
					username));
		}

		loginDto = jwtAuthenticationService.createJwtAuthenticationAndAuthenticate(
				loginDto, new IdmIdentityDto(identity, identity.getUsername()), loginDto.getAuthenticationModule());
		
		LOG.info("Identity with username [{}] is authenticated", username);
		
		return loginDto;
	}

	@Override
	public LoginDto loginAuthenticatedUser() {
		if (!securityService.isAuthenticated()) {
			throw new IdmAuthenticationException("Not authenticated!");
		}
		
		String username = securityService.getAuthentication().getCurrentUsername();
		
		LOG.info("Identity with username [{}] authenticating", username);
		
		IdmIdentityDto identity = identityService.getByUsername(username);
		// identity doesn't exist
		if (identity == null) {			
			throw new IdmAuthenticationException(MessageFormat.format(
					"Check identity can login: The identity "
					+ "[{0}] either doesn't exist or is deleted.",
					username));
		}
		
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(username);
		loginDto = jwtAuthenticationService.createJwtAuthenticationAndAuthenticate(
				loginDto, 
				identity,
				CoreModuleDescriptor.MODULE_ID);
		
		LOG.info("Identity with username [{}] is authenticated", username);

		return loginDto;
	}

	@Override
	public void logout() {
		logout(tokenManager.getCurrentToken());
	}
	
	@Override
	public void logout(IdmTokenDto token) {
		jwtAuthenticationService.logout(token);
	}
	
	@Override
	public LoginDto switchUser(IdmIdentityDto identity) {
		IdmTokenDto currentToken = tokenManager.getCurrentToken();
		ConfigurationMap properties = currentToken.getProperties();
		// Preserve the first original user => switch is available repetitively, but original user is preserved.
		properties.putIfAbsent(JwtAuthenticationMapper.PROPERTY_ORIGINAL_USERNAME, securityService.getCurrentUsername());
		properties.putIfAbsent(JwtAuthenticationMapper.PROPERTY_ORIGINAL_IDENTITY_ID, securityService.getCurrentId());
		currentToken.setProperties(properties);
		IdmTokenDto switchedToken = jwtTokenMapper.createToken(identity, currentToken);
		//
		// login by updated token
		LOG.info("Identity with username [{}] - login as switched user [{}].", 
				properties.get(JwtAuthenticationMapper.PROPERTY_ORIGINAL_USERNAME), 
				identity.getUsername());
		//
		return login(identity, switchedToken);
	}
	
	@Override
	public LoginDto switchUserLogout() {
		IdmTokenDto currentToken = tokenManager.getCurrentToken();
		ConfigurationMap properties = currentToken.getProperties();
		String originalUsername = properties.getString(JwtAuthenticationMapper.PROPERTY_ORIGINAL_USERNAME);
		//
		if (StringUtils.isEmpty(originalUsername)) {
			throw new ResultCodeException(CoreResultCode.NULL_ATTRIBUTE, ImmutableMap.of("attribute", "originalUsername"));
		}
		// change logged token authorities
		IdmIdentityDto identity = identityService.getByCode(originalUsername);
		if (identity == null) {
			throw new EntityNotFoundException(IdmIdentity.class, originalUsername);
		}
		//
		// Preserve the first original user => switch is available repetitively, but original user is preserved.
		properties.remove(JwtAuthenticationMapper.PROPERTY_ORIGINAL_USERNAME);
		properties.remove(JwtAuthenticationMapper.PROPERTY_ORIGINAL_IDENTITY_ID);
		currentToken.setProperties(properties);
		IdmTokenDto switchedToken = jwtTokenMapper.createToken(identity, currentToken);
		//
		// login by updated token
		LOG.info("Identity with username [{}] - logout from switched user [{}].", originalUsername, securityService.getCurrentUsername());
		//
		return login(identity, switchedToken);
	}

	/**
	 * Validates given identity can log in
	 * 
	 * @param identity
	 * @param password
	 * @return
	 */
	private boolean validate(IdmIdentityDto identity, LoginDto loginDto) {
		if (identity.isDisabled()) {
			throw new IdmAuthenticationException(MessageFormat.format("Check identity can login: The identity [{0}] is disabled.", identity.getUsername() ));
		}
		// GuardedString isn't necessary password is in hash
		IdmPasswordDto idmPassword = passwordService.findOneByIdentity(identity.getId());
		if (idmPassword == null) {
			LOG.warn("Identity [{}] does not have pasword in idm", identity.getUsername());
			return false;
		}
		// check if user must change password, skip this check if loginDto contains flag
		if (idmPassword.isMustChange() && loginDto.isSkipMustChange()) {
			throw new ResultCodeException(CoreResultCode.MUST_CHANGE_IDM_PASSWORD, ImmutableMap.of("user", identity.getUsername()));
		}
		// check if password expired
		if (idmPassword.getValidTill() != null && idmPassword.getValidTill().isBefore(LocalDate.now())) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_EXPIRED);
		}
		return passwordService.checkPassword(loginDto.getPassword(), idmPassword);
	}
	
	private LoginDto login(IdmIdentityDto identity, IdmTokenDto token) {
		IdmJwtAuthentication authentication = jwtTokenMapper.fromDto(token);
		//
		oAuthAuthenticationManager.authenticate(authentication);
		//
		LoginDto loginDto = new LoginDto(identity.getUsername(), null);
		loginDto.setAuthenticationModule(token.getModuleId());
		IdmJwtAuthenticationDto authenticationDto = jwtTokenMapper.toDto(token);
		loginDto.setAuthentication(authenticationDto);
		loginDto.setToken(jwtTokenMapper.writeToken(authenticationDto));
		loginDto.setAuthorities(jwtTokenMapper.getDtoAuthorities(token));
		//
		return loginDto;
	}
}
