package eu.bcvsolutions.idm.acc.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Configuration for provisioning
 * 
 * @author Radek Tomiška
 *
 */
public interface ProvisioningConfiguration extends Configurable {

	/**
	 * Supports sending password attributes in one provisioning operation
	 * - true: additional password attributes will be send in one provisioning operation together with password
	 * - false: additional password attributes will be send in new provisioning operation, after password change operation
	 */
	static final String PROPERTY_SUPPORT_SEND_PASSWORD_ATTRIBUTES = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "acc.provisioning.supportSendPasswordAttributes";
	static final boolean DEFAULT_SUPPORT_SEND_PASSWORD_ATTRIBUTES = true;
	
	@Override
	default java.lang.String getConfigurableType() {
		return "provisioning";
	}
	
	@Override
	default boolean isDisableable() {
		return false;
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>();  // we are not using superclass properties - enable and order does not make a sense here
		properties.add(PROPERTY_SUPPORT_SEND_PASSWORD_ATTRIBUTES);
		return properties;
	}
	
	/**
	 * Return break configuration
	 * 
	 * @return
	 */
	ProvisioningBreakConfiguration getBeakConfiguration();
	
	
	/**
	 * Returns true, when supports sending password attributes in one provisioning operation
	 * 
	 * @return
	 */
	boolean isSupportSendPasswordAttributes();
}
