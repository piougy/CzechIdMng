package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.stereotype.Service;
import com.google.common.annotations.Beta;
import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringTypeDto;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.model.event.MonitoringEvent;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Monitoring manager
 * 
 * @author Vít Švanda
 * 
 * @since 10.3.0
 *
 */
@Beta
@Service("monitoringManager")
public class DefaultMonitoringManager implements MonitoringManager {
	
	@Autowired
	public EntityEventManager entityEventManager;
	
	@Override
	public IdmMonitoringTypeDto check(String monitoringType, BasePermission... permission) {
		
		IdmMonitoringTypeDto monitoringTypeDto = new IdmMonitoringTypeDto();
		monitoringTypeDto.setType(monitoringType);
		
		MonitoringEvent event = new MonitoringEvent(MonitoringEvent.MonitoringEventType.CHECK, monitoringTypeDto);
		
		return entityEventManager.process(event).getContent();
	} 
	
	
}
