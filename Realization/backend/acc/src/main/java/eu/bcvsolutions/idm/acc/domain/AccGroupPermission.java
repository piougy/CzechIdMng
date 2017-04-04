package eu.bcvsolutions.idm.acc.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Aggregate base permission. Name can't contain character '_' - its used for joining to authority name.
 * 
 * @author Radek Tomiška
 *
 */
public enum AccGroupPermission implements GroupPermission {
	
	SYSTEM(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	ACCOUNT(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	SYNCHRONIZATION(IdmBasePermission.CREATE, IdmBasePermission.UPDATE);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String SYSTEM_READ = "SYSTEM" + BasePermission.SEPARATOR + "READ";
	public static final String SYSTEM_CREATE = "SYSTEM" + BasePermission.SEPARATOR + "CREATE";
	public static final String SYSTEM_UPDATE = "SYSTEM" + BasePermission.SEPARATOR + "UPDATE";
	public static final String SYSTEM_DELETE = "SYSTEM" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String ACCOUNT_READ = "ACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String ACCOUNT_CREATE = "ACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String ACCOUNT_UPDATE = "ACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String ACCOUNT_DELETE = "ACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	
	public static final String SYNCHRONIZATION_CREATE = "SYNCHRONIZATION" + BasePermission.SEPARATOR + "CREATE";
	public static final String SYNCHRONIZATION_UPDATE = "SYNCHRONIZATION" + BasePermission.SEPARATOR + "UPDATE";
	
	private final List<BasePermission> permissions;

	private AccGroupPermission(BasePermission... permissions) {
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
	
	@Override
	public String getModule() {
		return AccModuleDescriptor.MODULE_ID;
	}
}
