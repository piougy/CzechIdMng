package eu.bcvsolutions.idm.core.security.api.domain;

/**
 * Contract added base permissions.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
public enum ContractBasePermission implements BasePermission {
	
	CHANGEPERMISSION; // create role request for changing identity permissions on related contract.
	
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
