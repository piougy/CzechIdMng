package eu.bcvsolutions.idm.security.service.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.configuration.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.security.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.security.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.security.dto.LoginDto;
import eu.bcvsolutions.idm.security.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.security.service.GrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.security.service.LoginService;

@Service
public class DefaultLoginService implements LoginService {

	private static final Logger log = LoggerFactory.getLogger(DefaultLoginService.class);
	public static final String PROPERTY_EXPIRATION_TIMEOUT = "idm.sec.core.security.jwt.expirationTimeout";
	public static final int DEFAULT_EXPIRATION_TIMEOUT = 36000000;
	public static final String PROPERTY_SECRET_TOKEN = "idm.sec.core.security.jwt.secret.token";
	public static final String DEFAULT_SECRET_TOKEN = "idmSecret";

//	@Autowired
//	private PasswordEncoder passwordEncoder;

	@Autowired
	private IdmIdentityRepository idmIdentityRepository;

	@Autowired
	private ObjectMapper jsonMapper;
	
	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private GrantedAuthoritiesFactory grantedAuthoritiesFactory;

	@Override
	public LoginDto login(String username, String password) {
		log.info("Identity with username [{}] authenticating", username);

		if (!validate(username, password)) {
			log.debug("Username or password for identity [{}] is not correct!", username);			
			throw new IdmAuthenticationException(MessageFormat.format("Check identity password: Failed for identity {0} because the password digests differ.", username));
		}

		log.info("Identity with username [{}] is authenticated", username);

		Date expiration = new Date(System.currentTimeMillis() + configurationService.getIntegerValue(PROPERTY_EXPIRATION_TIMEOUT, DEFAULT_EXPIRATION_TIMEOUT));

		IdmJwtAuthentication authentication = new IdmJwtAuthentication(username, expiration,
				grantedAuthoritiesFactory.getGrantedAuthorities(username));

		IdmJwtAuthenticationDto authenticationDto = grantedAuthoritiesFactory
				.getIdmJwtAuthenticationDto(authentication);
		String authenticationJson;
		try {
			authenticationJson = jsonMapper.writeValueAsString(authenticationDto);
		} catch (IOException e) {
			throw new IdmAuthenticationException(e.getMessage());
		}

		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(username);
		loginDto.setAuthentication(authenticationDto);
		loginDto.setToken(JwtHelper.encode(authenticationJson, new MacSigner(configurationService.getValue(PROPERTY_SECRET_TOKEN, DEFAULT_SECRET_TOKEN))).getEncoded());
		return loginDto;
	}

	private boolean validate(String username, String password) {
		IdmIdentity identity = idmIdentityRepository.findOneByUsername(username);
		if (identity == null) {			
			throw new IdmAuthenticationException(MessageFormat.format("Check identity can login: The identity [{0}] either doesn't exist or is deleted.", username));
		}
		if (identity.isDisabled()) {
			throw new IdmAuthenticationException(MessageFormat.format("Check identity can login: The identity [{0}] is disabled.",  username ));
		}

		if (password.equals(new String(identity.getPassword()))) {
			return true;
		}
		return false;
	}

}
