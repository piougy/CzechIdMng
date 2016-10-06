package eu.bcvsolutions.idm.acc.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.core.model.domain.IdmBasePermission;
import eu.bcvsolutions.idm.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.security.api.domain.GroupPermission;

/**
 * Aggregate base permission. Name can't contain character '_' - its used for joining to authority name.
 * 
 * @author Radek Tomiška
 *
 */
public enum AccGroupPermission implements GroupPermission {
	
	SYSTEM(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE, IdmBasePermission.DELETE),
	ACCOUNT(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE, IdmBasePermission.DELETE);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String SYSTEM_READ = "SYSTEM" + BasePermission.SEPARATOR + "READ";
	public static final String SYSTEM_WRITE = "SYSTEM" + BasePermission.SEPARATOR + "WRITE";
	public static final String SYSTEM_DELETE = "SYSTEM" + BasePermission.SEPARATOR + "DELETE";
	
	public static final String ACCOUNT_READ = "ACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String ACCOUNT_WRITE = "ACCOUNT" + BasePermission.SEPARATOR + "WRITE";
	public static final String ACCOUNT_DELETE = "ACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	
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
}
