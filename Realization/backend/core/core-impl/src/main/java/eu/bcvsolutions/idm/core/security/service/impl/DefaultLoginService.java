package eu.bcvsolutions.idm.core.security.service.impl;

import java.io.IOException;
import java.text.MessageFormat;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.core.security.service.GrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.core.security.service.LoginService;

/**
 * Default login service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultLoginService implements LoginService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultLoginService.class);

	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private GrantedAuthoritiesFactory grantedAuthoritiesFactory;
	
	@Autowired
	private OAuthAuthenticationManager authenticationManager;
	
	@Autowired
	private JwtAuthenticationMapper jwtTokenMapper;
	
	@Autowired
	private IdmPasswordService passwordService;
	
	@Autowired
	private SecurityService securityService;

	@Override
	public LoginDto login(LoginDto loginDto) {
		String username = loginDto.getUsername();
		LOG.info("Identity with username [{}] authenticating", username);
		
		IdmIdentity identity = identityService.getByUsername(username);
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

		IdmJwtAuthentication authentication = new IdmJwtAuthentication(
				new IdmIdentityDto(identity, identity.getUsername()),
				getLoginExpiration(),
				grantedAuthoritiesFactory.getGrantedAuthorities(username),
				loginDto.getAuthenticationModule());
		
		authenticationManager.authenticate(authentication);

		LOG.info("Identity with username [{}] is authenticated", username);

		IdmJwtAuthenticationDto authenticationDto = jwtTokenMapper.toDto(authentication);
	
		try {
			loginDto.setUsername(username);
			loginDto.setAuthentication(authenticationDto);
			loginDto.setToken(jwtTokenMapper.writeToken(authenticationDto));
			return loginDto;
		} catch (IOException ex) {
			throw new IdmAuthenticationException(ex.getMessage(), ex);
		}
	}

	

	@Override
	public LoginDto loginAuthenticatedUser() {
		if (!securityService.isAuthenticated()) {
			throw new IdmAuthenticationException("Not authenticated!");
		}
		
		String username = securityService.getAuthentication().getCurrentUsername();
		
		LOG.info("Identity with username [{}] authenticating", username);
		
		IdmIdentity identity = identityService.getByUsername(username);
		// identity exists
		if (identity == null) {			
			throw new IdmAuthenticationException(MessageFormat.format(
					"Check identity can login: The identity "
					+ "[{0}] either doesn't exist or is deleted.",
					username));
		}
		
		IdmJwtAuthentication authentication = new IdmJwtAuthentication(
				new IdmIdentityDto(identity, identity.getUsername()),
				getLoginExpiration(),
				grantedAuthoritiesFactory.getGrantedAuthorities(username),
				EntityUtils.getModule(this.getClass()));
		
		authenticationManager.authenticate(authentication);

		LOG.info("Identity with username [{}] is authenticated", username);

		IdmJwtAuthenticationDto authenticationDto = jwtTokenMapper.toDto(authentication);
	
		try {
			LoginDto loginDto = new LoginDto();
			loginDto.setUsername(username);
			loginDto.setAuthentication(authenticationDto);
			loginDto.setToken(jwtTokenMapper.writeToken(authenticationDto));
			return loginDto;
		} catch (IOException ex) {
			throw new IdmAuthenticationException(ex.getMessage(), ex);
		}
	}



	/**
	 * Validates given identity can log in
	 * 
	 * @param identity
	 * @param password
	 * @return
	 */
	private boolean validate(IdmIdentity identity, LoginDto loginDto) {
		if (identity.isDisabled()) {
			throw new IdmAuthenticationException(MessageFormat.format("Check identity can login: The identity [{0}] is disabled.", identity.getUsername() ));
		}
		// GuardedString isn't necessary password is in hash
		IdmPassword idmPassword = passwordService.get(identity);
		if (idmPassword == null) {
			LOG.warn("Identity [{}] does not have pasword in idm", identity.getUsername());
			return false;
		}
		// check if user must change password, skip this check if loginDto contains flag
		if (idmPassword.isMustChange() && loginDto.isSkipMustChange()) {
			throw new ResultCodeException(CoreResultCode.MUST_CHANGE_IDM_PASSWORD, ImmutableMap.of("user", identity.getUsername()));
		}
		// check if password expired
		if (idmPassword.getValidTill() != null && idmPassword.getValidTill().isBefore(new LocalDate())) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_EXPIRED);
		}
		return passwordService.checkPassword(loginDto.getPassword(), idmPassword);
	}
	
	private DateTime getLoginExpiration() {
		// new expiration date
		DateTime expiration = DateTime.now()
				.plus(configurationService.getIntegerValue(PROPERTY_EXPIRATION_TIMEOUT, DEFAULT_EXPIRATION_TIMEOUT));
		return expiration;
	}
	
}
