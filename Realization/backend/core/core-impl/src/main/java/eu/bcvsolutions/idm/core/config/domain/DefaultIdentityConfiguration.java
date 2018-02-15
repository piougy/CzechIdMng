package eu.bcvsolutions.idm.core.config.domain;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.PasswordChangeType;

/**
 * Configuration for features with identities.
 * 
 * @author Radek Tomiška
 *
 */
@Component("identityConfiguration")
public class DefaultIdentityConfiguration extends AbstractConfiguration implements IdentityConfiguration {	
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdentityConfiguration.class);
	
	@Override
	public boolean isCreateDefaultContractEnabled() {
		return getConfigurationService().getBooleanValue(PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT, DEFAULT_IDENTITY_CREATE_DEFAULT_CONTRACT);
	}

	@Override
	public PasswordChangeType getPasswordChangeType() {
		String passwordChangeProperty = getConfigurationService().getValue(PROPERTY_IDENTITY_CHANGE_PASSWORD, DEFAULT_IDENTITY_CHANGE_PASSWORD.name());
		try {
			return PasswordChangeType.valueOf(passwordChangeProperty);
		} catch (IllegalArgumentException ex) {
			LOG.warn("Password change type [{}] is wrong configured. password will be  [{}], fix configuration property [{}]", 
					passwordChangeProperty, DEFAULT_IDENTITY_CHANGE_PASSWORD, PROPERTY_IDENTITY_CHANGE_PASSWORD,  ex);
			return DEFAULT_IDENTITY_CHANGE_PASSWORD;
		}
	}
	
	@Override
	public boolean isRequireOldPassword() {
		return getConfigurationService().getBooleanValue(PROPERTY_REQUIRE_OLD_PASSWORD, DEFAULT_REQUIRE_OLD_PASSWORD);
	}

	@Override
	public boolean isAllowedPublicChangePasswordForIdm() {
		return getConfigurationService().getBooleanValue(PROPERTY_PUBLIC_CHANGE_PASSWORD_FOR_IDM_ENABLED, DEFAULT_PUBLIC_CHANGE_PASSWORD_FOR_IDM_ENABLED);
	}
}
