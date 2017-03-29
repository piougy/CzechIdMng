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
	
	IDENTITY(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	ROLE(IdmBasePermission.ADMIN, IdmBasePermission.SEARCH, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	// TODO: role catalogue
	TREENODE(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	TREETYPE(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	CONFIGURATION(IdmBasePermission.ADMIN, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE), // read configuration is public operation
	CONFIGURATIONSECURED(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	PASSWORDPOLICY(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	SCRIPT(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	AUDIT(IdmBasePermission.ADMIN, IdmBasePermission.READ),
	MODULE(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE),
	SCHEDULER(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	ROLEREQUEST(IdmRoleRequestPermission.ADMIN, IdmRoleRequestPermission.READ, IdmRoleRequestPermission.WRITE, IdmRoleRequestPermission.DELETE, IdmRoleRequestPermission.EXECUTEIMMEDIATELY),
	EAVFORMDEFINITIONS(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE),
	EAVFORMATTRIBUTES(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.CREATE, IdmBasePermission.UPDATE, IdmBasePermission.DELETE);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String IDENTITY_CREATE = "IDENTITY" + BasePermission.SEPARATOR + "CREATE";
	public static final String IDENTITY_UPDATE = "IDENTITY" + BasePermission.SEPARATOR + "UPDATE";
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
	public static final String PASSWORDPOLICY_WRITE = "PASSWORDPOLICY" + BasePermission.SEPARATOR + "WRITE";
	public static final String PASSWORDPOLICY_DELETE = "PASSWORDPOLICY" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String SCRIPT_READ = "SCRIPT" + BasePermission.SEPARATOR + "READ";
	public static final String SCRIPT_WRITE = "SCRIPT" + BasePermission.SEPARATOR + "WRITE";
	public static final String SCRIPT_DELETE = "SCRIPT" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String AUDIT_READ = "AUDIT" + BasePermission.SEPARATOR + "READ";
	//
	public static final String MODULE_READ = "MODULE" + BasePermission.SEPARATOR + "READ";
	public static final String MODULE_WRITE = "MODULE" + BasePermission.SEPARATOR + "WRITE";
	//
	public static final String SCHEDULER_READ = "SCHEDULER" + BasePermission.SEPARATOR + "READ";
	public static final String SCHEDULER_WRITE = "SCHEDULER" + BasePermission.SEPARATOR + "WRITE";
	public static final String SCHEDULER_DELETE = "SCHEDULER" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String ROLE_REQUEST_READ = "ROLEREQUEST" + BasePermission.SEPARATOR + "READ";
	public static final String ROLE_REQUEST_WRITE = "ROLEREQUEST" + BasePermission.SEPARATOR + "WRITE";
	public static final String ROLE_REQUEST_DELETE = "ROLEREQUEST" + BasePermission.SEPARATOR + "DELETE";
	public static final String ROLE_REQUEST_EXECUTE_IMMEDIATELY = "ROLEREQUEST" + BasePermission.SEPARATOR + "EXECUTEIMMEDIATELY";
	//
	public static final String EAV_FORM_DEFINITIONS_READ = "EAVFORMDEFINITIONS" + BasePermission.SEPARATOR + "READ";
	public static final String EAV_FORM_DEFINITIONS_WRITE = "EAVFORMDEFINITIONS" + BasePermission.SEPARATOR + "WRITE";
	public static final String EAV_FORM_DEFINITIONS_DELETE = "EAVFORMDEFINITIONS" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String EAV_FORM_ATTRIBUTES_READ = "EAVFORMATTRIBUTES" + BasePermission.SEPARATOR + "READ";
	public static final String EAV_FORM_ATTRIBUTES_WRITE = "EAVFORMATTRIBUTES" + BasePermission.SEPARATOR + "WRITE";
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
