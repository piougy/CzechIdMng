package eu.bcvsolutions.idm.core.model.domain;

import java.util.Arrays;
import java.util.List;

public enum IdmGroupPermission implements GroupPermission {
	
	USER(IdmBasePermission.READ, IdmBasePermission.WRITE, IdmBasePermission.DELETE),
	ROLE(IdmBasePermission.READ, IdmBasePermission.WRITE, IdmBasePermission.DELETE);
	
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
}
