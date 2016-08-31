package eu.bcvsolutions.idm.core.model.domain;

import eu.bcvsolutions.idm.security.domain.BasePermission;

public enum IdmBasePermission implements BasePermission {
	
	ADMIN, // wildcard - all base permissions
	CREATE,
	READ,
	WRITE,
	DELETE;
	
	@Override
	public String getName() {
		return name();
	}
}
