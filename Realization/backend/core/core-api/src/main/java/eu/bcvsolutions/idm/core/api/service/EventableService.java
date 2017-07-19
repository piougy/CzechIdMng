package eu.bcvsolutions.idm.core.api.service;


import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Allows control and publish event in service. 
 * 
 * @author svandav
 *
 * @param <DTO>
 */

public interface EventableService<DTO extends BaseDto> {

	/**
	 * Publish event. Event must have not null content (instance of DTO).
	 * Before publish do permission check (by given event content).
	 * @param event
	 * @param permission
	 * @return
	 */
	EventContext<DTO> publish(EntityEvent<DTO> event, BasePermission... permission);

}