package eu.bcvsolutions.idm.core.notification.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.model.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;

/**
 * Notification module permissions
 * 
 * @author Radek Tomiška
 *
 */
public enum NotificationGroupPermission implements GroupPermission {
	
	NOTIFICATION(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE),
	NOTIFICATIONCONFIGURATION(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE, IdmBasePermission.DELETE);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String NOTIFICATION_READ = "NOTIFICATION" + BasePermission.SEPARATOR + "READ";
	public static final String NOTIFICATION_WRITE = "NOTIFICATION" + BasePermission.SEPARATOR + "WRITE";
	//
	public static final String NOTIFICATIONCONFIGURATION_READ = "NOTIFICATIONCONFIGURATION" + BasePermission.SEPARATOR + "READ";
	public static final String NOTIFICATIONCONFIGURATION_WRITE = "NOTIFICATIONCONFIGURATION" + BasePermission.SEPARATOR + "WRITE";
	public static final String NOTIFICATIONCONFIGURATION_DELETE = "NOTIFICATIONCONFIGURATION" + BasePermission.SEPARATOR + "DELETE";
	
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
