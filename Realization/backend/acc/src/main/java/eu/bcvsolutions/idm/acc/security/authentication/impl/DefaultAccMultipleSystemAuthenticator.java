package eu.bcvsolutions.idm.acc.security.authentication.impl;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
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
 * Authenticator for authenticate over multiple system
 * 
 * @author Ondrej Kopr
 *
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("ACC authenticator that authenticate over multiple systems defined by properties.")
public class DefaultAccMultipleSystemAuthenticator extends AbstractAccAuthenticator implements Authenticator {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAccMultipleSystemAuthenticator.class);
	
	private static final String AUTHENTICATOR_NAME = "acc-multiple-system-authenticator";

	@Autowired
	private AuthenticatorConfiguration authenticatorConfiguration;
	
	@Override
	public int getOrder() {
		// After CORE module and after original ACC module. Original authenticator has bigger priority than this authenticator.
		return DEFAULT_AUTHENTICATOR_ORDER + 10;
	}

	@Override
	public String getName() {
		return DefaultAccMultipleSystemAuthenticator.AUTHENTICATOR_NAME;
	}

	@Override
	public String getModule() {
		return EntityUtils.getModule(this.getClass());
	}

	@Override
	public LoginDto authenticate(LoginDto loginDto) {
		IdmIdentityDto identity = getIdentity(loginDto);

		List<SysSystemDto> systems = authenticatorConfiguration.getSystems();

		if (CollectionUtils.isEmpty(systems)) {
			LOG.debug("System configuration is empty");
			return null;
		}
		
		IcUidAttribute auth = null;
		String finalSystemIdentifier = null;

		for (SysSystemDto system : systems) {
			auth = this.authenticateOverSystem(system, loginDto, identity);

			if (auth != null && auth.getValue() != null) {
				// Store final system for log
				finalSystemIdentifier = system.getCode();
				break;
			}
		}
		
		if (auth == null || auth.getValue() == null) {
			throw new ResultCodeException(AccResultCode.AUTHENTICATION_AGAINST_MULTIPLE_SYSTEM_FAILED,  ImmutableMap.of("username", loginDto.getUsername()));
		}

		String module = this.getModule();
		loginDto = jwtAuthenticationService.createJwtAuthenticationAndAuthenticate(loginDto, identity, module);
		LOG.info("Identity with username [{}] is authenticated by system id [{}]. Multiple system authentication.", loginDto.getUsername(), finalSystemIdentifier);
		
		return loginDto;
	}

	@Override
	public AuthenticationResponseEnum getExceptedResult() {
		return AuthenticationResponseEnum.SUFFICIENT;
	}

}
