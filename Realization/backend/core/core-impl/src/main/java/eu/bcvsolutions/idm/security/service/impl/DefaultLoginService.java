package eu.bcvsolutions.idm.security.service.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.dto.IdentityDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.security.dto.LoginDto;
import eu.bcvsolutions.idm.security.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.security.service.GrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.security.service.LoginService;

/**
 * Default login service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultLoginService implements LoginService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultLoginService.class);
	public static final String PROPERTY_EXPIRATION_TIMEOUT = "idm.sec.core.security.jwt.expirationTimeout";
	public static final int DEFAULT_EXPIRATION_TIMEOUT = 36000000;

	@Autowired
	private IdmIdentityService identityService;

	@Autowired
	@Qualifier("objectMapper")
	private ObjectMapper mapper;
	
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

	@Override
	public LoginDto login(String username, GuardedString password) {
		LOG.info("Identity with username [{}] authenticating", username);
		
		IdmIdentity identity = identityService.getByUsername(username);
		// identity exists
		if (identity == null) {			
			throw new IdmAuthenticationException(MessageFormat.format("Check identity can login: The identity [{0}] either doesn't exist or is deleted.", username));
		}
		// validate identity
		if (!validate(identity, password)) {
			LOG.debug("Username or password for identity [{}] is not correct!", username);			
			throw new IdmAuthenticationException(MessageFormat.format("Check identity password: Failed for identity {0} because the password digests differ.", username));
		}
		// new expiration date
		Date expiration = new Date(System.currentTimeMillis() + configurationService.getIntegerValue(PROPERTY_EXPIRATION_TIMEOUT, DEFAULT_EXPIRATION_TIMEOUT));

		IdmJwtAuthentication authentication = new IdmJwtAuthentication(
				new IdentityDto(identity, identity.getUsername()),
				expiration,
				grantedAuthoritiesFactory.getGrantedAuthorities(username));
		
		authenticationManager.authenticate(authentication);

		LOG.info("Identity with username [{}] is authenticated", username);

		//JwtAuthenticationMapper jwtTokenMapper = new JwtAuthenticationMapper(mapper, configurationService.getValue(PROPERTY_SECRET_TOKEN, DEFAULT_SECRET_TOKEN));
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
	private boolean validate(IdmIdentity identity, GuardedString password) {
		if (identity.isDisabled()) {
			throw new IdmAuthenticationException(MessageFormat.format("Check identity can login: The identity [{0}] is disabled.", identity.getUsername() ));
		}
		// TODO: for now is full access for admin, unskip test testBadCredentialsLogIn
		if (identity.getUsername().equals("admin")) {
			return true;
		}
		// GuardedString isn't nesessary password is in hash
		IdmPassword idmPassword = passwordService.get(identity);
		if (idmPassword == null) {
			LOG.warn("Identity [{}] does not have pasword in idm", identity.getUsername());
			return false;
		}
		return passwordService.checkPassword(password, idmPassword);
	}

}
