package eu.bcvsolutions.idm.core.api.service;

import com.google.common.annotations.Beta;
import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringTypeDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Monitoring manager
 * 
 * @author Vít Švanda
 * @since 10.3.0
 */
@Beta
public interface MonitoringManager {
	
	public IdmMonitoringTypeDto check(String monitoringType, BasePermission... permission);


}