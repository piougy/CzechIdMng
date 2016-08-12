package eu.bcvsolutions.idm.notification.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.core.model.domain.IdmBasePermission;
import eu.bcvsolutions.idm.security.domain.BasePermission;
import eu.bcvsolutions.idm.security.domain.GroupPermission;

public enum NotificationGroupPermission implements GroupPermission {
	
	NOTIFICATION(IdmBasePermission.READ, IdmBasePermission.WRITE);
	
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
