package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.domain.PasswordChangeType;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Public configuration for identity
 * 
 * @see PrivateIdentityConfiguration
 * @author Radek Tomiška
 */	
public interface IdentityConfiguration extends Configurable {
	
	/**
	 * Supports identity delete operation
	 */
	String PROPERTY_IDENTITY_DELETE = 
			ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.identity.delete";
	boolean DEFAULT_IDENTITY_DELETE = true;
	
	/**
	 * Password change type for custom users
	 * 
	 * @see PasswordChangeType
	 */
	String PROPERTY_IDENTITY_CHANGE_PASSWORD = 
			ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.identity.passwordChange";
	PasswordChangeType DEFAULT_IDENTITY_CHANGE_PASSWORD = PasswordChangeType.DISABLED;
	
	/**
	 * Requires previous password, when password is changed
	 */
	String PROPERTY_REQUIRE_OLD_PASSWORD = 
			ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.identity.passwordChange.requireOldPassword";
	boolean DEFAULT_REQUIRE_OLD_PASSWORD = true;
	
	/**
	 * Public change password for IdM, property must be public
	 */
	String PROPERTY_PUBLIC_CHANGE_PASSWORD_FOR_IDM_ENABLED =
			ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.identity.passwordChange.public.idm.enabled";
	boolean DEFAULT_PUBLIC_CHANGE_PASSWORD_FOR_IDM_ENABLED = true;
	
	/**
	 * Creates default identity's contract, when new identity is created
	 * 
	 * TODO: move to private #813
	 */
	String PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT = 
			ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.identity.create.defaultContract.enabled";
	boolean DEFAULT_IDENTITY_CREATE_DEFAULT_CONTRACT = true;	
	
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
		properties.add(getPropertyName(PROPERTY_REQUIRE_OLD_PASSWORD));
		properties.add(getPropertyName(PROPERTY_PUBLIC_CHANGE_PASSWORD_FOR_IDM_ENABLED));
		return properties;
	}
	
	/**
	 * Returns true, when default contract will be created with new identity
	 * 
	 * TODO: move to private #813
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
	boolean isAllowedPublicChangePasswordForIdm();
}
