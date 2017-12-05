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
	 * Supports sending password attributes in one provisioning operation - true:
	 * additional password attributes will be send in one provisioning operation
	 * together with password - false: additional password attributes will be send
	 * in new provisioning operation, after password change operation
	 */
	static final String PROPERTY_SEND_PASSWORD_ATTRIBUTES_TOGETHER = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX
			+ "acc.provisioning.sendPasswordAttributesTogether";
	static final boolean DEFAULT_SEND_PASSWORD_ATTRIBUTES_TOGETHER = true;
	
	static final String PROPERTY_ALLOW_AUTO_MAPPING_ON_EXISTING_ACCOUNT = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX
			+ "acc.provisioning.allowedAutoMappingOnExistingAccount";
	static final boolean DEFAULT_ALLOW_AUTO_MAPPING_ON_EXISTING_ACCOUNT = true;

	@Override
	default String getConfigurableType() {
		return "provisioning";
	}

	@Override
	default boolean isDisableable() {
		return false;
	}

	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does
														// not make a sense here
		properties.add(PROPERTY_SEND_PASSWORD_ATTRIBUTES_TOGETHER);
		properties.add(PROPERTY_ALLOW_AUTO_MAPPING_ON_EXISTING_ACCOUNT);
		return properties;
	}

	/**
	 * Return break configuration
	 * 
	 * @return
	 */
	ProvisioningBreakConfiguration getBeakConfiguration();

	/**
	 * Returns true, when supports sending password attributes in one provisioning
	 * operation
	 * 
	 * @return
	 */
	boolean isSendPasswordAttributesTogether();

	/**
	 * Returns true, when we want mapped existed account on the target system. It
	 * means, before create new account (call create on the connector), we try found
	 * account on the target system. When account will be returned, then will
	 * IdM account mapped on him and call only connector update.
	 * 
	 * @return
	 */
	boolean isAllowedAutoMappingOnExistingAccount();
}
