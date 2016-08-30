package eu.bcvsolutions.idm.core.model.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.security.domain.BasePermission;
import eu.bcvsolutions.idm.security.domain.GroupPermission;

/**
 * Aggregate base permission. Name can't contain character '_' - its used for joining to authority name.
 * 
 * @author Radek Tomiška
 *
 */
public enum IdmGroupPermission implements GroupPermission {
	
	SYSTEM(IdmBasePermission.ADMIN), // wildcard - system admin has all permissions
	IDENTITY(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE, IdmBasePermission.DELETE),
	ROLE(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE, IdmBasePermission.DELETE),
	ORGANIZATION(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE, IdmBasePermission.DELETE),
	WORKFLOW(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE, IdmBasePermission.DELETE),
	CONFIGURATION(IdmBasePermission.ADMIN, IdmBasePermission.WRITE, IdmBasePermission.DELETE), // read configuration is public operation
	CONFIGURATIONSECURED(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE, IdmBasePermission.DELETE);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String SYSTEM_ADMIN = "SYSTEM" + BasePermission.SEPARATOR + "ADMIN";
	//
	public static final String IDENTITY_WRITE = "IDENTITY" + BasePermission.SEPARATOR + "READ";
	public static final String IDENTITY_READ = "IDENTITY" + BasePermission.SEPARATOR + "WRITE";
	public static final String IDENTITY_DELETE = "IDENTITY" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String CONFIGURATION_WRITE = "CONFIGURATION" + BasePermission.SEPARATOR + "WRITE";
	public static final String CONFIGURATION_DELETE = "CONFIGURATION" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String CONFIGURATIONSECURED_READ = "CONFIGURATIONSECURED" + BasePermission.SEPARATOR + "READ";
	public static final String CONFIGURATIONSECURED_WRITE = "CONFIGURATIONSECURED" + BasePermission.SEPARATOR + "WRITE";
	public static final String CONFIGURATIONSECURED_DELETE = "CONFIGURATIONSECURED" + BasePermission.SEPARATOR + "DELETE";
	
	
	private final List<BasePermission> permissions;

	private IdmGroupPermission(BasePermission... permissions) {
		this.permissions = Arrays.asList(permissions);
	}
	
	@Override
	public List<BasePermission> getPermissions() {		
		return permissions;
	}
	
	@Override
	public String getName() {
		return name();
	}	
}
