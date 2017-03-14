package eu.bcvsolutions.idm.acc.security.authentication.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.authentication.Authenticator;
import eu.bcvsolutions.idm.core.security.api.domain.AuthenticationResponseEnum;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.core.security.service.GrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultLoginService;
import eu.bcvsolutions.idm.core.security.service.impl.JwtAuthenticationMapper;
import eu.bcvsolutions.idm.core.security.service.impl.OAuthAuthenticationManager;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcUidAttributeImpl;

/**
 * Component for authenticate over system
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("ACC module authenticator, authenticate over system defined by properties.")
public class DefaultAccAuthenticator implements Authenticator {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAccAuthenticator.class);
	
	public static final String PROPERTY_AUTH_SYSTEM_ID = "idm.sec.security.auth.systemId";
	private static final String AUTHENTICATOR_NAME = "acc-authenticator";
	
	private final ConfigurationService configurationService;
	
	private final SysSystemService systemService;
	
	private final ProvisioningService provisioningService;
	
	private final AccAccountService accountService;
	
	private final IdmIdentityService identityService;
	
	private final SysSystemAttributeMappingService systemAttributeMappingService;

	private final JwtAuthenticationMapper jwtTokenMapper;
	
	private final GrantedAuthoritiesFactory grantedAuthoritiesFactory;
	
	private final OAuthAuthenticationManager authenticationManager;
	
	@Autowired
	public DefaultAccAuthenticator(ConfigurationService configurationService,
			SysSystemService systemService,
			ProvisioningService provisioningService,
			AccAccountService accountService,
			IdmIdentityService identityService,
			SysSystemAttributeMappingService systemAttributeMappingService,
			JwtAuthenticationMapper jwtTokenMapper,
			GrantedAuthoritiesFactory grantedAuthoritiesFactory,
			OAuthAuthenticationManager authenticationManager) {
		//
		Assert.notNull(accountService);
		Assert.notNull(configurationService);
		Assert.notNull(systemService);
		Assert.notNull(provisioningService);
		Assert.notNull(identityService);
		Assert.notNull(systemAttributeMappingService);
		Assert.notNull(jwtTokenMapper);
		Assert.notNull(grantedAuthoritiesFactory);
		Assert.notNull(authenticationManager);
		//
		this.systemService = systemService;
		this.configurationService = configurationService;
		this.provisioningService = provisioningService;
		this.accountService = accountService;
		this.identityService = identityService;
		this.systemAttributeMappingService = systemAttributeMappingService;
		this.jwtTokenMapper = jwtTokenMapper;
		this.grantedAuthoritiesFactory = grantedAuthoritiesFactory;
		this.authenticationManager = authenticationManager;
	}
	
	@Override
	public int getOrder() {
		return DEFAULT_AUTHENTICATOR_ORDER - 10;
	}

	@Override
	public String getName() {
		return DefaultAccAuthenticator.AUTHENTICATOR_NAME;
	}

	@Override
	public String getModule() {
		return EntityUtils.getModule(this.getClass());
	}

	@Override
	public LoginDto authenticate(LoginDto loginDto) {
		// temporary solution for get system id, this is not nice.
		String systemId = configurationService.getValue(PROPERTY_AUTH_SYSTEM_ID);
		if (systemId == null || systemId.isEmpty()) {
			return null; // without system can't check
		}
		//
		SysSystem system = systemService.get(systemId);
		//
		if (system == null) {
			return null; // system dont exist
		}
		IdmIdentity identity = identityService.getByName(loginDto.getUsername());
		if (identity == null) {	
			throw new IdmAuthenticationException(MessageFormat.format("Check identity can login: The identity [{0}] either doesn't exist or is deleted.", loginDto.getUsername()));
		}
		//
		// search authentication attribute for system with provisioning mapping
		SysSystemAttributeMapping attribute = systemAttributeMappingService.getAuthenticationAttribute(system.getId());
		//
		if (attribute == null) {
			throw new ResultCodeException(AccResultCode.AUTHENTICATION_AUTHENTICATION_ATTRIBUTE_DONT_SET,  ImmutableMap.of("name", system.getName()));
		}
		//
		// find if identity has account on system
		List<AccAccount> accounts = accountService.getAccouts(system.getId(), identity.getId());
		if (accounts.isEmpty()) {
			// user hasn't account on system, continue
			return null;
		}
		//
		ResultCodeException authFailedException = null;
		//
		// authenticate over all accounts find first, or throw error
		for (AccAccount account : accounts) {
			IcConnectorObject attributes = systemService.readObject(system, attribute.getSystemMapping(),
					new IcUidAttributeImpl(attribute.getName(), account.getUid(), null));
			String transformUsername = null;
			// iterate over all attributes to fined authentication attribute
			for (IcAttribute icAttribute : attributes.getAttributes()) {
				if (icAttribute.getName().equals(attribute.getName())) {
					transformUsername = String.valueOf(icAttribute.getValue());
					break;
				}
			}
			if (transformUsername == null) {
				throw new ResultCodeException(AccResultCode.AUTHENTICATION_USERNAME_DONT_EXISTS,  ImmutableMap.of("username", loginDto.getUsername() ,"name", system.getName()));
			}
			// other method to get username for system: String.valueOf(systemAttributeMappingService.transformValueToResource(loginDto.getUsername(), attribute, identity));
			//
			// authentication over system, when password or username not exist or bad credentials - throw error
			IcUidAttribute auth = null;
			//
			try {
				// authentication against system
				auth = provisioningService.authenticate(transformUsername, loginDto.getPassword(), system, SystemEntityType.IDENTITY);
				authFailedException = null;
				// check auth
				if (auth == null || auth.getValue() == null) {
					authFailedException = new ResultCodeException(AccResultCode.AUTHENTICATION_AGAINST_SYSTEM_FAILED,  ImmutableMap.of("name", system.getName(), "username", loginDto.getUsername()));
					// failed, continue to another
					break;
				}
				// everything success break
				break;
			} catch (ResultCodeException e) {
				// failed, continue to another
				authFailedException = new ResultCodeException(CoreResultCode.AUTH_FAILED, "Invalid login or password.", e);
			}
		}
		//
		if (authFailedException != null) {
			throw authFailedException;
		}
		//
		// new expiration date
		Date expiration = new Date(System.currentTimeMillis() + configurationService.getIntegerValue(DefaultLoginService.PROPERTY_EXPIRATION_TIMEOUT, DefaultLoginService.DEFAULT_EXPIRATION_TIMEOUT));
		//
		IdmJwtAuthentication authentication = new IdmJwtAuthentication(
				new IdentityDto(identity, identity.getUsername()),
				expiration,
				grantedAuthoritiesFactory.getGrantedAuthorities(loginDto.getUsername()),
				this.getModule());
		//
		authenticationManager.authenticate(authentication);
		//
		LOG.info("Identity with username [{}] is authenticated on system [{}]", loginDto.getUsername(), system.getName());
		//
		IdmJwtAuthenticationDto authenticationDto = jwtTokenMapper.toDto(authentication);
		//
		try {
			// set authentication module
			loginDto.setAuthenticationModule(this.getModule());
			loginDto.setAuthentication(authenticationDto);
			loginDto.setToken(jwtTokenMapper.writeToken(authenticationDto));
			return loginDto;
		} catch (IOException ex) {
			throw new IdmAuthenticationException(ex.getMessage(), ex);
		}
	}

	@Override
	public AuthenticationResponseEnum getResponse() {
		return AuthenticationResponseEnum.REQUISITE;
	}

}
