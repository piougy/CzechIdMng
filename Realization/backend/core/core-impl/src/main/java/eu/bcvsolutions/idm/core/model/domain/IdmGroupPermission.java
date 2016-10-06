package eu.bcvsolutions.idm.core.model.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.security.api.domain.GroupPermission;

/**
 * Aggregate base permission. Name can't contain character '_' - its used for joining to authority name.
 * 
 * @author Radek Tomiška
 *
 */
public enum IdmGroupPermission implements GroupPermission {
	
	APP(IdmBasePermission.ADMIN), // wildcard - system admin has all permissions
	IDENTITY(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE, IdmBasePermission.DELETE),
	ROLE(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE, IdmBasePermission.DELETE),
	TREENODE(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE, IdmBasePermission.DELETE),
	TREETYPE(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE, IdmBasePermission.DELETE),
	CONFIGURATION(IdmBasePermission.ADMIN, IdmBasePermission.WRITE, IdmBasePermission.DELETE), // read configuration is public operation
	CONFIGURATIONSECURED(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE, IdmBasePermission.DELETE),
	AUDIT(IdmBasePermission.ADMIN, IdmBasePermission.READ),
	MODULE(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String APP_ADMIN = "APP" + BasePermission.SEPARATOR + "ADMIN";
	//
	public static final String IDENTITY_WRITE = "IDENTITY" + BasePermission.SEPARATOR + "WRITE";
	public static final String IDENTITY_DELETE = "IDENTITY" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String CONFIGURATION_WRITE = "CONFIGURATION" + BasePermission.SEPARATOR + "WRITE";
	public static final String CONFIGURATION_DELETE = "CONFIGURATION" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String CONFIGURATIONSECURED_READ = "CONFIGURATIONSECURED" + BasePermission.SEPARATOR + "READ";
	public static final String CONFIGURATIONSECURED_WRITE = "CONFIGURATIONSECURED" + BasePermission.SEPARATOR + "WRITE";
	public static final String CONFIGURATIONSECURED_DELETE = "CONFIGURATIONSECURED" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String TREENODE_WRITE = "TREENODE" + BasePermission.SEPARATOR + "WRITE";
	public static final String TREENODE_DELETE = "TREENODE" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String TREETYPE_WRITE = "TREETYPE" + BasePermission.SEPARATOR + "WRITE";
	public static final String TREETYPE_DELETE = "TREETYPE" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String ROLE_READ = "ROLE" + BasePermission.SEPARATOR + "READ";
	public static final String ROLE_WRITE = "ROLE" + BasePermission.SEPARATOR + "WRITE";
	public static final String ROLE_DELETE = "ROLE" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String AUDIT_READ = "AUDIT" + BasePermission.SEPARATOR + "READ";
	//
	public static final String MODULE_READ = "MODULE" + BasePermission.SEPARATOR + "READ";
	public static final String MODULE_WRITE = "MODULE" + BasePermission.SEPARATOR + "WRITE";
	
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
