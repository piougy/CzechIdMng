package eu.bcvsolutions.idm.core.model.domain;

import eu.bcvsolutions.idm.security.domain.BasePermission;

public enum IdmBasePermission implements BasePermission {
	
	CREATE,
	READ,
	WRITE,
	DELETE;
	
	@Override
	public String getName() {
		return name();
	}
}
