package eu.bcvsolutions.idm.core.model.domain;

import eu.bcvsolutions.idm.security.domain.BasePermission;

public enum CustomBasePermission implements BasePermission {
	
	ADMIN;
	
	@Override
	public String getName() {
		return name();
	}
}
