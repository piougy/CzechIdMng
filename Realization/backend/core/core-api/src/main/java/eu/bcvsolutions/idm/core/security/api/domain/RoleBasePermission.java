package eu.bcvsolutions.idm.core.security.api.domain;

/**
 * Role added base permissions.
 * 
 * @author Radek Tomiška
 *
 */
public enum RoleBasePermission implements BasePermission {
	
	CANBEREQUESTED; // role can be requested.
	
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
