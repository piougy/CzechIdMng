package eu.bcvsolutions.idm.acc.domain;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Specific endpoints base permissions for {@link SysSystemDto}.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public enum SystemBasePermission implements BasePermission {
	
	PASSWORDFILTERCHANGE, // Allow change password via password filter
	PASSWORDFILTERVALIDATE; // Allow validate password via password filter
	
	@Override
	public String getName() {
		return name();
	}
	
	@Override
	public String getModule() {
		// common base permission without module
		return AccModuleDescriptor.MODULE_ID;
	}
}
