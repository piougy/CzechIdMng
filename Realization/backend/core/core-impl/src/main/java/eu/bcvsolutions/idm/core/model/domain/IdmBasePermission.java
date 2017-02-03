package eu.bcvsolutions.idm.core.model.domain;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

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
	
	@Override
	public String getModule() {
		return CoreModuleDescriptor.MODULE_ID;
	}
}
