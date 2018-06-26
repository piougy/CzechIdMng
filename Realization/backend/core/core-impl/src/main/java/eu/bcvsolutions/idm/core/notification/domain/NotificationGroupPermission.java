package eu.bcvsolutions.idm.core.notification.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Notification module permissions
 * 
 * @author Radek Tomiška
 *
 */
public enum NotificationGroupPermission implements GroupPermission {
	
	NOTIFICATION(
			IdmBasePermission.ADMIN, 
			IdmBasePermission.READ, 
			IdmBasePermission.CREATE, 
			IdmBasePermission.UPDATE),
	NOTIFICATIONCONFIGURATION(
			IdmBasePermission.ADMIN, 
			IdmBasePermission.READ, 
			IdmBasePermission.CREATE, 
			IdmBasePermission.UPDATE, 
			IdmBasePermission.DELETE),
	NOTIFICATIONTEMPLATE(
			IdmBasePermission.ADMIN, 
			IdmBasePermission.COUNT, 
			IdmBasePermission.AUTOCOMPLETE,
			IdmBasePermission.READ, 
			IdmBasePermission.CREATE, 
			IdmBasePermission.UPDATE, 
			IdmBasePermission.DELETE);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String NOTIFICATION_READ = "NOTIFICATION" + BasePermission.SEPARATOR + "READ";
	public static final String NOTIFICATION_CREATE = "NOTIFICATION" + BasePermission.SEPARATOR + "CREATE";
	public static final String NOTIFICATION_UPDATE = "NOTIFICATION" + BasePermission.SEPARATOR + "UPDATE";
	//
	public static final String NOTIFICATIONCONFIGURATION_READ = "NOTIFICATIONCONFIGURATION" + BasePermission.SEPARATOR + "READ";
	public static final String NOTIFICATIONCONFIGURATION_CREATE = "NOTIFICATIONCONFIGURATION" + BasePermission.SEPARATOR + "CREATE";
	public static final String NOTIFICATIONCONFIGURATION_UPDATE = "NOTIFICATIONCONFIGURATION" + BasePermission.SEPARATOR + "UPDATE";
	public static final String NOTIFICATIONCONFIGURATION_DELETE = "NOTIFICATIONCONFIGURATION" + BasePermission.SEPARATOR + "DELETE";
	//
	public static final String NOTIFICATIONTEMPLATE_COUNT = "NOTIFICATIONTEMPLATE" + BasePermission.SEPARATOR + "COUNT";
	public static final String NOTIFICATIONTEMPLATE_AUTOCOMPLETE = "NOTIFICATIONTEMPLATE" + BasePermission.SEPARATOR + "AUTOCOMPLETE";
	public static final String NOTIFICATIONTEMPLATE_READ = "NOTIFICATIONTEMPLATE" + BasePermission.SEPARATOR + "READ";
	public static final String NOTIFICATIONTEMPLATE_CREATE = "NOTIFICATIONTEMPLATE" + BasePermission.SEPARATOR + "CREATE";
	public static final String NOTIFICATIONTEMPLATE_UPDATE = "NOTIFICATIONTEMPLATE" + BasePermission.SEPARATOR + "UPDATE";
	public static final String NOTIFICATIONTEMPLATE_DELETE = "NOTIFICATIONTEMPLATE" + BasePermission.SEPARATOR + "DELETE";
	
	private final List<BasePermission> permissions;

	private NotificationGroupPermission(BasePermission... permissions) {
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
