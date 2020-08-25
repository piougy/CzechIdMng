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
	CHANGEPROJECTION, // @since 10.2.0 change form projection 
	CHANGEUSERNAME, // @since 10.3.0 change login
	CHANGENAME, // @since 10.3.0 change full name (+titles)
	CHANGEPHONE, // @since 10.3.0 change phone
	CHANGEEMAIL, // @since 10.3.0 change email
	CHANGEEXTERNALCODE, // @since 10.3.0 change personal number
	CHANGEDESCRIPTION, // @since 10.3.0 change description
	SWITCHUSER; // @since 10.5.0 - logged user can login as selected user (switch user).
	
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
