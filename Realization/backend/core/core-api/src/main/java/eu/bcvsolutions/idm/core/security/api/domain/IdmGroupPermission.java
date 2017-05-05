package eu.bcvsolutions.idm.core.security.api.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Aggregate base permission. Name can't contain character '_' - its used for joining to authority name.
 * 
 * @author Radek Tomiška
 */
public enum IdmGroupPermission implements GroupPermission {
	
	APP(IdmBasePermission.ADMIN); // wildcard - system admin has all permissions
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String APP_ADMIN = "APP" + BasePermission.SEPARATOR + "ADMIN"; // big boss
	
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
	
	@Override
	public String getModule() {
		// common group permission without module
		return null;
	}
}
