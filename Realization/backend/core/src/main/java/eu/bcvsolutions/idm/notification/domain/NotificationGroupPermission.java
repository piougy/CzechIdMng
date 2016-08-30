package eu.bcvsolutions.idm.notification.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.core.model.domain.IdmBasePermission;
import eu.bcvsolutions.idm.security.domain.BasePermission;
import eu.bcvsolutions.idm.security.domain.GroupPermission;

/**
 * Notification module permissions
 * 
 * @author Radek Tomiška
 *
 */
public enum NotificationGroupPermission implements GroupPermission {
	
	NOTIFICATION(IdmBasePermission.ADMIN, IdmBasePermission.READ, IdmBasePermission.WRITE);
	
	// String constants could be used in pre / post authotize SpEl expressions
	
	public static final String NOTIFICATION_READ = "NOTIFICATION" + BasePermission.SEPARATOR + "READ";
	public static final String NOTIFICATION_WRITE = "NOTIFICATION" + BasePermission.SEPARATOR + "WRITE";
	
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
}
