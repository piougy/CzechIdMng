package eu.bcvsolutions.idm.core.security.api.domain;

public enum IdentityBasePermission implements BasePermission {
	
	PASSWORDCHANGE, // password change
	CHANGEPERMISSION; // create role request for changing identity permissions
	
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
