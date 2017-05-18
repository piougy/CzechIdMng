package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Configuration for identity
 * 
 * @author Radek Tomiška
 */	
public interface IdentityConfiguration extends Configurable {
	
	/**
	 * Supports identity delete operation
	 */
	public static final String PROPERTY_IDENTITY_DELETE = 
			ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.identity.delete";
	
	/**
	 * Default type password change for custom users
	 */
	public static final String PROPERTY_IDENTITY_CHANGE_PASSWORD = 
			ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.identity.passwordChange";
	
	/**
	 * Creates default identity's contract, when new identity is created
	 */
	public static final String PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT = 
			ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.identity.create.defaultContract.enabled";
	public static final boolean DEFAULT_IDENTITY_CREATE_DEFAULT_CONTRACT = true;
	
	@Override
	default String getConfigurableType() {
		return "identity";
	}
	
	@Override
	default boolean isDisableable() {
		return false;
	}
	
	@Override
	default public boolean isSecured() {
		return false;
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sence here
		properties.add(getPropertyName(PROPERTY_IDENTITY_DELETE));
		properties.add(getPropertyName(PROPERTY_IDENTITY_CHANGE_PASSWORD));
		properties.add(getPropertyName(PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT));
		return properties;
	}
	
	/**
	 * Returns true, when default contract will be created with new identity
	 * 
	 * @return
	 */
	boolean isCreateDefaultContractEnabled();
}
