package eu.bcvsolutions.idm.core.config.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.PrivateIdentityConfiguration;

/**
 * Configuration for identity (private - sec)
 * 
 * @author Radek Tomiška
 *
 */
@Component("privateIdentityConfiguration")
public class DefaultPrivateIdentityConfiguration extends AbstractConfiguration implements PrivateIdentityConfiguration {	
	
	@Autowired private IdentityConfiguration publicConfiguration;
	
	@Override
	public IdentityConfiguration getPublicConfiguration() {
		return publicConfiguration;
	}
	
	@Override
	public boolean isFormAttributesSecured() {
		return getConfigurationService().getBooleanValue(PROPERTY_IDENTITY_FORM_ATTRIBUTES_SECURED, DEFAULT_IDENTITY_FORM_ATTRIBUTES_SECURED);
	}
}
