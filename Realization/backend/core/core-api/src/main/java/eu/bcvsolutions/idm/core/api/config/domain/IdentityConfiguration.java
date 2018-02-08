package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.domain.PasswordChangeType;
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
	static final String PROPERTY_IDENTITY_DELETE = 
			ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.identity.delete";
	static final boolean DEFAULT_IDENTITY_DELETE = true;
	
	/**
	 * Password change type for custom users
	 * 
	 * @see PasswordChangeType
	 */
	static final String PROPERTY_IDENTITY_CHANGE_PASSWORD = 
			ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.identity.passwordChange";
	static final PasswordChangeType DEFAULT_IDENTITY_CHANGE_PASSWORD = PasswordChangeType.DISABLED;
	
	/**
	 * Requires previous password, when password is changed
	 */
	static final String PROPERTY_REQUIRE_OLD_PASSWORD = 
			ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.identity.passwordChange.requireOldPassword";
	static final boolean DEFAULT_REQUIRE_OLD_PASSWORD = true;
	
	/**
	 * Change password for IdM, property must be public
	 */
	static final String PROPERTY_ENABLED_CHANGE_PASSWORD_FOR_IDM =
			ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.identity.passwordChange.idm.enabled";
	static final boolean DEFAULT_ENABLED_CHANGE_PASSWORD_FOR_IDM = true;
	
	/**
	 * Creates default identity's contract, when new identity is created
	 */
	static final String PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT = 
			ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.identity.create.defaultContract.enabled";
	static final boolean DEFAULT_IDENTITY_CREATE_DEFAULT_CONTRACT = true;
	
	
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
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sense here
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
	
	/**
	 * Returns configured password change type
	 * 
	 * @return
	 */
	PasswordChangeType getPasswordChangeType();
	
	/**
	 * Requires previous password, when password is changed
	 * 
	 * @return
	 */
	boolean isRequireOldPassword();
	
	/**
	 * Return if is allowed password change through IdM
	 * 
	 * @return true if is allowed, otherwise false
	 */
	boolean isAllowedChangePasswordForIdm();
}
