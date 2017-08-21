package eu.bcvsolutions.idm.acc.security.authentication.impl;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
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
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.authentication.AbstractAuthenticator;
import eu.bcvsolutions.idm.core.security.api.authentication.Authenticator;
import eu.bcvsolutions.idm.core.security.api.domain.AuthenticationResponseEnum;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.core.security.service.JwtAuthenticationService;
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
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("ACC module authenticator, authenticate over system defined by properties.")
public class DefaultAccAuthenticator extends AbstractAuthenticator implements Authenticator {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAccAuthenticator.class);
	
	public static final String PROPERTY_AUTH_SYSTEM_ID = "idm.sec.security.auth.systemId";
	private static final String AUTHENTICATOR_NAME = "acc-authenticator";
	
	private final ConfigurationService configurationService;
	
	private final SysSystemService systemService;
	
	private final ProvisioningService provisioningService;
	
	private final AccAccountService accountService;
	
	private final IdmIdentityService identityService;
	
	private final SysSystemAttributeMappingService systemAttributeMappingService;
	
	private final JwtAuthenticationService jwtAuthenticationService;
	
	@Autowired
	public DefaultAccAuthenticator(ConfigurationService configurationService,
			SysSystemService systemService,
			ProvisioningService provisioningService,
			AccAccountService accountService,
			IdmIdentityService identityService,
			SysSystemAttributeMappingService systemAttributeMappingService,
			JwtAuthenticationService jwtAuthenticationService) {
		//
		Assert.notNull(accountService);
		Assert.notNull(configurationService);
		Assert.notNull(systemService);
		Assert.notNull(provisioningService);
		Assert.notNull(identityService);
		Assert.notNull(systemAttributeMappingService);
		Assert.notNull(jwtAuthenticationService);
		//
		this.systemService = systemService;
		this.configurationService = configurationService;
		this.provisioningService = provisioningService;
		this.accountService = accountService;
		this.identityService = identityService;
		this.systemAttributeMappingService = systemAttributeMappingService;
		this.jwtAuthenticationService = jwtAuthenticationService;
	}
	
	@Override
	public int getOrder() {
		return DEFAULT_AUTHENTICATOR_ORDER + 10;
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
		IdmIdentityDto identity = identityService.getByUsername(loginDto.getUsername());
		if (identity == null) {	
			throw new IdmAuthenticationException(MessageFormat.format("Check identity can login: The identity [{0}] either doesn't exist or is deleted.", loginDto.getUsername()));
		}
		//
		// search authentication attribute for system with provisioning mapping
		SysSystemAttributeMapping attribute = systemAttributeMappingService.getAuthenticationAttribute(system.getId());
		//
		if (attribute == null) {
			// attribute MUST exist
			throw new ResultCodeException(AccResultCode.AUTHENTICATION_AUTHENTICATION_ATTRIBUTE_DONT_SET,  ImmutableMap.of("name", system.getName()));
		}
		//
		// find if identity has account on system
		List<AccAccount> accounts = accountService.getAccounts(system.getId(), identity.getId());
		if (accounts.isEmpty()) {
			// user hasn't account on system, continue
			return null;
		}
		//
		ResultCodeException authFailedException = null;
		IcUidAttribute auth = null;
		//
		// authenticate over all accounts find first, or throw error
		for (AccAccount account : accounts) {
			IcConnectorObject connectorObject = systemService.readObject(system, attribute.getSystemMapping(),
					new IcUidAttributeImpl(null, account.getSystemEntity().getUid(), null));
			//
			if (connectorObject == null) {
				continue;
			}
			//
			String transformUsername = null;
			// iterate over all attributes to find authentication attribute
			for (IcAttribute icAttribute : connectorObject.getAttributes()) {
				if (icAttribute.getName().equals(attribute.getSchemaAttribute().getName())) {
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
		if (auth == null || auth.getValue() == null) {
			authFailedException = new ResultCodeException(AccResultCode.AUTHENTICATION_AGAINST_SYSTEM_FAILED,  ImmutableMap.of("name", system.getName(), "username", loginDto.getUsername()));
		}
		//
		if (authFailedException != null) {
			throw authFailedException;
		}
		String module = this.getModule();
		
		loginDto = jwtAuthenticationService.createJwtAuthenticationAndAuthenticate(
				loginDto, identity, module);
		
		LOG.info("Identity with username [{}] is authenticated on system [{}]", loginDto.getUsername(), system.getName());
		
		return loginDto;
	}

	@Override
	public AuthenticationResponseEnum getExceptedResult() {
		return AuthenticationResponseEnum.SUFFICIENT;
	}

}
