package eu.bcvsolutions.idm.rpt.api.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;

/**
 * Aggregate base permission. Name can't contain character '_' - its used for joining to authority name.
 * 
 * @author Radek Tomiška
 *
 */
public enum RptGroupPermission implements GroupPermission {
	
	REPORT(
			IdmBasePermission.ADMIN, 
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.READ, 
			IdmBasePermission.CREATE, 
			IdmBasePermission.UPDATE, 
			IdmBasePermission.DELETE);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String REPORT_ADMIN = "REPORT" + BasePermission.SEPARATOR + "ADMIN";
	public static final String REPORT_AUTOCOMPLETE = "REPORT" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String REPORT_READ = "REPORT" + BasePermission.SEPARATOR + "READ";
	public static final String REPORT_CREATE = "REPORT" + BasePermission.SEPARATOR + "CREATE";
	public static final String REPORT_UPDATE = "REPORT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String REPORT_DELETE = "REPORT" + BasePermission.SEPARATOR + "DELETE";
	
	private final List<BasePermission> permissions;

	private RptGroupPermission(BasePermission... permissions) {
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
		return RptModuleDescriptor.MODULE_ID;
	}
}
