package eu.bcvsolutions.idm.core.api.config.domain;

/**
 * Configuration for identity
 * 
 * @author Radek Tomiška
 */
public interface IdentityConfiguration {
	
	/**
	 * Supports identity delete operation
	 */
	public static final String PROPERTY_IDENTITY_DELETE = "idm.pub.core.identity.delete";
	
	/**
	 * Default type password change for custom users
	 * TODO: check this when change password? Or is this useless?
	 */
	public static final String PROPERTY_IDENTITY_CHANGE_PASSWORD = "idm.pub.core.identity.passwordChange";

}
