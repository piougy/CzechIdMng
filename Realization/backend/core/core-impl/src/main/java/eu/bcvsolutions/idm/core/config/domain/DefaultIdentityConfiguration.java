package eu.bcvsolutions.idm.core.config.domain;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;

/**
 * Configuration for features with identities.
 * 
 * @author Radek Tomiška
 *
 */
@Component("identityConfiguration")
public class DefaultIdentityConfiguration extends AbstractConfiguration implements IdentityConfiguration {	
	
	@Override
	public boolean isCreateDefaultContractEnabled() {
		return getConfigurationService().getBooleanValue(PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT, DEFAULT_IDENTITY_CREATE_DEFAULT_CONTRACT);
	}
}
