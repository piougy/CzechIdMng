package eu.bcvsolutions.idm.core.api.service;

import com.google.common.annotations.Beta;
import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringTypeDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

@Beta
/**
 * Monitoring manager
 * 
 * @author Vít Švanda
 * @since 10.3.0
 */
public interface MonitoringManager {

	String MONITORING_TYPE_DATABASE = "monitoring-database";
	
	public IdmMonitoringTypeDto check(String monitoringType, BasePermission... permission);


}