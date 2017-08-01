package eu.bcvsolutions.idm.example.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;

/**
 * Aggregate base permission. Name can't contain character '_' - its used for joining to authority name.
 * 
 * @author Radek Tomiška
 *
 */
public enum ExampleGroupPermission implements GroupPermission {
	
	PRODUCT(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String PRODUCT_ADMIN = "PRODUCT" + BasePermission.SEPARATOR + "ADMIN";
	public static final String PRODUCT_READ = "PRODUCT" + BasePermission.SEPARATOR + "READ";
	public static final String PRODUCT_CREATE = "PRODUCT" + BasePermission.SEPARATOR + "CREATE";
	public static final String PRODUCT_UPDATE = "PRODUCT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String PRODUCT_DELETE = "PRODUCT" + BasePermission.SEPARATOR + "DELETE";
	
	private final List<BasePermission> permissions;
	
	private ExampleGroupPermission(BasePermission... permissions) {
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
		return ExampleModuleDescriptor.MODULE_ID;
	}
}
