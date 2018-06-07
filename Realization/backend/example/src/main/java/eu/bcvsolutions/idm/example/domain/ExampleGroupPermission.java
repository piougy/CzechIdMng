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
	
	EXAMPLEPRODUCT(
			IdmBasePermission.ADMIN,
			IdmBasePermission.COUNT,
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.READ,
			IdmBasePermission.CREATE,
			IdmBasePermission.UPDATE,
			IdmBasePermission.DELETE);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String EXAMPLE_PRODUCT_ADMIN = "EXAMPLEPRODUCT" + BasePermission.SEPARATOR + "ADMIN";
	public static final String EXAMPLE_PRODUCT_COUNT = "EXAMPLEPRODUCT" + BasePermission.SEPARATOR + "COUNT";
	public static final String EXAMPLE_PRODUCT_AUTOCOMPLETE = "EXAMPLEPRODUCT" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String EXAMPLE_PRODUCT_READ = "EXAMPLEPRODUCT" + BasePermission.SEPARATOR + "READ";
	public static final String EXAMPLE_PRODUCT_CREATE = "EXAMPLEPRODUCT" + BasePermission.SEPARATOR + "CREATE";
	public static final String EXAMPLE_PRODUCT_UPDATE = "EXAMPLEPRODUCT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String EXAMPLE_PRODUCT_DELETE = "EXAMPLEPRODUCT" + BasePermission.SEPARATOR + "DELETE";
	
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
