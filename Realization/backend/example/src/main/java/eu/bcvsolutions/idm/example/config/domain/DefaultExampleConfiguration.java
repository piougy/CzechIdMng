package eu.bcvsolutions.idm.example.config.domain;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;

/**
 * Example configuration - implementation
 * 
 * @author Radek Tomiška
 *
 */
@Component("exampleConfiguration")
public class DefaultExampleConfiguration 
		extends AbstractConfiguration
		implements ExampleConfiguration {

	@Override
	public String getPrivateValue() {
		return getConfigurationService().getValue(PROPERTY_PRIVATE);
	}

	@Override
	public String getConfidentialValue() {
		return getConfigurationService().getValue(PROPERTY_CONFIDENTIAL);
	}
}
