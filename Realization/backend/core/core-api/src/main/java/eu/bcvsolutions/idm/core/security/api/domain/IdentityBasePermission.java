package eu.bcvsolutions.idm.core.security.api.domain;

/**
 * Identity added base permissions.
 * 
 * @author Radek Tomiška
 *
 */
public enum IdentityBasePermission implements BasePermission {
	
	PASSWORDCHANGE, // password change.
	CHANGEPERMISSION, // create role request for changing identity permissions.
	MANUALLYDISABLE, // @since 9.7.3 manually disable
	MANUALLYENABLE, // @since 9.7.3 manually enable
	CHANGEPROJECTION; // @since 10.2.0 change form projection 
	
	@Override
	public String getName() {
		return name();
	}
	
	@Override
	public String getModule() {
		// common base permission without module
		return null;
	}
}
