package eu.bcvsolutions.idm.core.model.domain;

import java.util.Arrays;
import java.util.List;

public enum CustomGroupPermission implements GroupPermission {
	
	SYSTEM(CustomBasePermission.ADMIN),
	USER(CustomBasePermission.ADMIN);
	
	private final List<BasePermission> permissions;

	private CustomGroupPermission(BasePermission... permissions) {
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
