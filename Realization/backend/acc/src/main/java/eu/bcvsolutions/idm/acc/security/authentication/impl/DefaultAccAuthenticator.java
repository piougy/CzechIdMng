package eu.bcvsolutions.idm.acc.security.authentication.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.config.domain.AuthenticatorConfiguration;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.security.api.authentication.Authenticator;
import eu.bcvsolutions.idm.core.security.api.domain.AuthenticationResponseEnum;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;

/**
 * Component for authenticate over system
 * 
 * @author Ondrej Kopr
 * @author Roman Kucera
 * @deprecated @since 10.4.0 Whole behavior of this authenticator {@link DefaultAccAuthenticator} was fully cover by {@link DefaultAccMultipleSystemAuthenticator} please use and configure only the newer.
 *
 */
@Deprecated
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("ACC module authenticator, authenticate over system defined by properties.")
public class DefaultAccAuthenticator extends AbstractAccAuthenticator implements Authenticator {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAccAuthenticator.class);
	
	@Deprecated
	public static final String PROPERTY_AUTH_SYSTEM_ID = AuthenticatorConfiguration.PROPERTY_AUTH_SYSTEM_ID;
	private static final String AUTHENTICATOR_NAME = "acc-authenticator";
	
	@Autowired
	private AuthenticatorConfiguration authenticatorConfiguration;
	
	@Override
	public int getOrder() {
		// After CORE module
		return DEFAULT_AUTHENTICATOR_ORDER + 5;
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
		IdmIdentityDto identity = this.getIdentity(loginDto);

		SysSystemDto system = authenticatorConfiguration.getSystem();
		
		IcUidAttribute auth = this.authenticateOverSystem(system, loginDto, identity);
		if (auth == null || auth.getValue() == null) {
			throw new ResultCodeException(AccResultCode.AUTHENTICATION_AGAINST_SYSTEM_FAILED,  ImmutableMap.of("name", system.getCode(), "username", loginDto.getUsername()));
		}

		String module = this.getModule();
		loginDto = jwtAuthenticationService.createJwtAuthenticationAndAuthenticate(loginDto, identity, module);
		LOG.info("Identity with username [{}] is authenticated by system identifier [{}]", loginDto.getUsername(), system.getCode());
		return loginDto;
	}

	@Override
	public AuthenticationResponseEnum getExceptedResult() {
		return AuthenticationResponseEnum.SUFFICIENT;
	}

}
