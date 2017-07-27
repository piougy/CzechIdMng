package eu.bcvsolutions.idm.example.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Example configuration - interface
 * 
 * @author Radek Tomiška
 *
 */
public interface ExampleConfiguration extends Configurable {

	static final String PROPERTY_PRIVATE = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "example.test.private";
	static final String PROPERTY_CONFIDENTIAL = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "example.test.confidential.token";
	
	@Override
	default String getConfigurableType() {
		return "test";
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sense here
		properties.add(getPropertyName(PROPERTY_PRIVATE));
		properties.add(getPropertyName(PROPERTY_CONFIDENTIAL));
		return properties;
	}
	
	/**
	 * Read private value from module-example.properties
	 * 
	 * @return
	 */
	String getPrivateValue();
	
	/**
	 * Read confidential value from module-example.properties
	 * 
	 * @return
	 */
	String getConfidentialValue();
}
