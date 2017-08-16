package eu.bcvsolutions.idm.vs.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.vs.VirtualSystemModuleDescriptor;

/**
 * Aggregate base permission.
 * 
 * @author Svanda
 *
 */
public enum VirtualSystemGroupPermission implements GroupPermission {
	
	VSACCOUNT(
			IdmBasePermission.ADMIN, 
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.READ,
			IdmBasePermission.CREATE,
			IdmBasePermission.UPDATE,
			IdmBasePermission.DELETE);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String VS_ACCOUNT_ADMIN = "VSACCOUNT" + BasePermission.SEPARATOR + "ADMIN";
	public static final String VS_ACCOUNT_AUTOCOMPLETE = "VSACCOUNT" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String VS_ACCOUNT_READ = "VSACCOUNT" + BasePermission.SEPARATOR + "READ";
	public static final String VS_ACCOUNT_CREATE = "VSACCOUNT" + BasePermission.SEPARATOR + "CREATE";
	public static final String VS_ACCOUNT_UPDATE = "VSACCOUNT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String VS_ACCOUNT_DELETE = "VSACCOUNT" + BasePermission.SEPARATOR + "DELETE";
	
	private final List<BasePermission> permissions;
	
	private VirtualSystemGroupPermission(BasePermission... permissions) {
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
		return VirtualSystemModuleDescriptor.MODULE_ID;
	}
}
