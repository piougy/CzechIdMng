package eu.bcvsolutions.idm.core.security.service.impl;

import java.text.MessageFormat;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.core.security.service.JwtAuthenticationService;
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
	private JwtAuthenticationService jwtAuthenticationService;
	
	@Autowired
	private IdmPasswordService passwordService;
	
	@Autowired
	private SecurityService securityService;

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
		// identity exists
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
				new IdmIdentityDto(identity, identity.getUsername()),
				EntityUtils.getModule(this.getClass()));
		
		LOG.info("Identity with username [{}] is authenticated", username);

		return loginDto;
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
		if (idmPassword.getValidTill() != null && idmPassword.getValidTill().isBefore(new LocalDate())) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_EXPIRED);
		}
		return passwordService.checkPassword(loginDto.getPassword(), idmPassword);
	}
}
