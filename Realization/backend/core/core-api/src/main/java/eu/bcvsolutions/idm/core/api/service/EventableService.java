package eu.bcvsolutions.idm.core.api.service;


import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Allows control and publish event in service. 
 * 
 * @author svandav
 *
 * @param <E> {@link BaseEntity}, {@link BaseDto} or any other {@link Serializable} content type
 */
public interface EventableService<E extends Serializable> {

	/**
	 * Publish event. Event must have not null content (instance of E).
	 * Before publish do permission check (by given event content).
	 * 
	 * @param event
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	EventContext<E> publish(EntityEvent<E> event, BasePermission... permission);
	
	/**
	 * Publish event. Event must have not null content (instance of E).
	 * Before publish do permission check (by given event content).
	 * 
	 * @param event
	 * @param parentEvent event is based on parent event
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	EventContext<E> publish(EntityEvent<E> event, EntityEvent<?> parentEvent, BasePermission... permission);

}