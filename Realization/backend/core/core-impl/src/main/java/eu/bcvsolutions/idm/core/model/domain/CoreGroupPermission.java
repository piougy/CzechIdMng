package eu.bcvsolutions.idm.core.model.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Aggregate base permission. Name can't contain character '_' - its used for joining to authority name.
 * 
 * @author Radek Tomiška
 *
 */
public enum CoreGroupPermission implements GroupPermission {
	
	AUTHORIZATIONPOLICY(IdmBasePermission.ADMIN, IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	IDENTITY(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	ROLE(IdmBasePermission.ADMIN, IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	ROLECATALOGUE(IdmBasePermission.ADMIN, IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	TREENODE(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	TREETYPE(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	CONFIGURATION(IdmBasePermission.ADMIN, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE), // read configuration is public operation
	CONFIGURATIONSECURED(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	PASSWORDPOLICY(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	SCRIPT(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	AUDIT(IdmBasePermission.ADMIN, IdmBasePermission.READ),
	MODULE(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE),
	SCHEDULER(IdmBasePermission.ADMIN, IdmBasePermission.EXECUTE, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	ROLEREQUEST(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE, IdmBasePermission.EXECUTE),
	EAVFORMDEFINITIONS(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	EAVFORMATTRIBUTES(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String AUTHORIZATIONPOLICY_AUTOCOMPLETE = "AUTHORIZATIONPOLICY" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String AUTHORIZATIONPOLICY_READ = "AUTHORIZATIONPOLICY" + BasePermission.SEPARATOR + "READ";
	public static final String AUTHORIZATIONPOLICY_CREATE = "AUTHORIZATIONPOLICY" + BasePermission.SEPARATOR + "CREATE";
	public static final String AUTHORIZATIONPOLICY_UPDATE = "AUTHORIZATIONPOLICY" + BasePermission.SEPARATOR + "UPDATE";
	public static final String AUTHORIZATIONPOLICY_DELETE = "AUTHORIZATIONPOLICY" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String IDENTITY_CREATE = "IDENTITY" + BasePermission.SEPARATOR + "CREATE";
	public static final String IDENTITY_UPDATE = "IDENTITY" + BasePermission.SEPARATOR + "UPDATE";
	public static final String IDENTITY_DELETE = "IDENTITY" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String CONFIGURATION_CREATE = "CONFIGURATION" + BasePermission.SEPARATOR + "CREATE";
	public static final String CONFIGURATION_UPDATE = "CONFIGURATION" + BasePermission.SEPARATOR + "UPDATE";
	public static final String CONFIGURATION_DELETE = "CONFIGURATION" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String CONFIGURATIONSECURED_READ = "CONFIGURATIONSECURED" + BasePermission.SEPARATOR + "READ";
	public static final String CONFIGURATIONSECURED_CREATE = "CONFIGURATIONSECURED" + BasePermission.SEPARATOR + "CREATE";
	public static final String CONFIGURATIONSECURED_UPDATE = "CONFIGURATIONSECURED" + BasePermission.SEPARATOR + "UPDATE";
	public static final String CONFIGURATIONSECURED_DELETE = "CONFIGURATIONSECURED" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String TREENODE_CREATE = "TREENODE" + BasePermission.SEPARATOR + "CREATE";
	public static final String TREENODE_UPDATE = "TREENODE" + BasePermission.SEPARATOR + "UPDATE";
	public static final String TREENODE_DELETE = "TREENODE" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String TREETYPE_CREATE = "TREETYPE" + BasePermission.SEPARATOR + "CREATE";
	public static final String TREETYPE_UPDATE = "TREETYPE" + BasePermission.SEPARATOR + "UPDATE";
	public static final String TREETYPE_DELETE = "TREETYPE" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String ROLE_AUTOCOMPLETE = "ROLE" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String ROLE_READ = "ROLE" + BasePermission.SEPARATOR + "READ";
	public static final String ROLE_CREATE = "ROLE" + BasePermission.SEPARATOR + "CREATE";
	public static final String ROLE_UPDATE = "ROLE" + BasePermission.SEPARATOR + "UPDATE";
	public static final String ROLE_DELETE = "ROLE" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String ROLECATALOGUE_READ = "ROLECATALOGUE" + BasePermission.SEPARATOR + "READ";
	public static final String ROLECATALOGUE_CREATE = "ROLECATALOGUE" + BasePermission.SEPARATOR + "CREATE";
	public static final String ROLECATALOGUE_UPDATE = "ROLECATALOGUE" + BasePermission.SEPARATOR + "UPDATE";
	public static final String ROLECATALOGUE_DELETE = "ROLECATALOGUE" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String PASSWORDPOLICY_CREATE = "PASSWORDPOLICY" + BasePermission.SEPARATOR + "CREATE";
	public static final String PASSWORDPOLICY_UPDATE = "PASSWORDPOLICY" + BasePermission.SEPARATOR + "UPDATE";
	public static final String PASSWORDPOLICY_DELETE = "PASSWORDPOLICY" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String SCRIPT_READ = "SCRIPT" + BasePermission.SEPARATOR + "READ";
	public static final String SCRIPT_CREATE = "SCRIPT" + BasePermission.SEPARATOR + "CREATE";
	public static final String SCRIPT_UPDATE = "SCRIPT" + BasePermission.SEPARATOR + "UPDATE";
	public static final String SCRIPT_DELETE = "SCRIPT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String AUDIT_READ = "AUDIT" + BasePermission.SEPARATOR + "READ";
	//
	public static final String MODULE_READ = "MODULE" + BasePermission.SEPARATOR + "READ";
	public static final String MODULE_CREATE = "MODULE" + BasePermission.SEPARATOR + "CREATE";
	public static final String MODULE_UPDATE = "MODULE" + BasePermission.SEPARATOR + "UPDATE";
	//
	public static final String SCHEDULER_READ = "SCHEDULER" + BasePermission.SEPARATOR + "READ";
	public static final String SCHEDULER_CREATE = "SCHEDULER" + BasePermission.SEPARATOR + "CREATE";
	public static final String SCHEDULER_UPDATE = "SCHEDULER" + BasePermission.SEPARATOR + "UPDATE";
	public static final String SCHEDULER_DELETE = "SCHEDULER" + BasePermission.SEPARATOR + "DELETE";
	public static final String SCHEDULER_EXECUTE = "SCHEDULER" + BasePermission.SEPARATOR + "EXECUTE";
	//
	public static final String ROLE_REQUEST_ADMIN = "ROLEREQUEST" + BasePermission.SEPARATOR + "ADMIN";
	public static final String ROLE_REQUEST_READ = "ROLEREQUEST" + BasePermission.SEPARATOR + "READ";
	public static final String ROLE_REQUEST_CREATE = "ROLEREQUEST" + BasePermission.SEPARATOR + "CREATE";
	public static final String ROLE_REQUEST_UPDATE = "ROLEREQUEST" + BasePermission.SEPARATOR + "UPDATE";
	public static final String ROLE_REQUEST_DELETE = "ROLEREQUEST" + BasePermission.SEPARATOR + "DELETE";
	public static final String ROLE_REQUEST_EXECUTE = "ROLEREQUEST" + BasePermission.SEPARATOR + "EXECUTE";
	//
	public static final String EAV_FORM_DEFINITIONS_READ = "EAVFORMDEFINITIONS" + BasePermission.SEPARATOR + "READ";
	public static final String EAV_FORM_DEFINITIONS_CREATE = "EAVFORMDEFINITIONS" + BasePermission.SEPARATOR + "CREATE";
	public static final String EAV_FORM_DEFINITIONS_UPDATE = "EAVFORMDEFINITIONS" + BasePermission.SEPARATOR + "UPDATE";
	public static final String EAV_FORM_DEFINITIONS_DELETE = "EAVFORMDEFINITIONS" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String EAV_FORM_ATTRIBUTES_READ = "EAVFORMATTRIBUTES" + BasePermission.SEPARATOR + "READ";
	public static final String EAV_FORM_ATTRIBUTES_CREATE = "EAVFORMATTRIBUTES" + BasePermission.SEPARATOR + "CREATE";
	public static final String EAV_FORM_ATTRIBUTES_UPDATE = "EAVFORMATTRIBUTES" + BasePermission.SEPARATOR + "UPDATE";
	public static final String EAV_FORM_ATTRIBUTES_DELETE = "EAVFORMATTRIBUTES" + BasePermission.SEPARATOR + "DELETE";
	
	private final List<BasePermission> permissions;

	private CoreGroupPermission(BasePermission... permissions) {
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
		return CoreModuleDescriptor.MODULE_ID;
	}
}
