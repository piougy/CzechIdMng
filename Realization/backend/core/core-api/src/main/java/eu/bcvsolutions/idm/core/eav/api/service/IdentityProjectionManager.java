package eu.bcvsolutions.idm.core.eav.api.service;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.dto.projection.IdmIdentityProjectionDto;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Identity projection - get / save.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
public interface IdentityProjectionManager {

	/**
	 * Load full projection.
	 * 
	 * @param codeableIdentifier uuid or username
	 * @param permission base permissions to evaluate (all permission needed)
	 * @return projection
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmIdentityProjectionDto get(Serializable codeableIdentifier, BasePermission... permission);
	
	/**
	 * Publish event. Event must have not null content.
	 * Before publish do permission check (by given event content).
	 * Lookout: identity roles are processed asynchronously -> new identity roles will not be filled after save (get is needed).
	 * 
	 * @param event
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	EventContext<IdmIdentityProjectionDto> publish(EntityEvent<IdmIdentityProjectionDto> event, BasePermission... permission);
	
	/**
	 * Publish event. Event must have not null content.
	 * Before publish do permission check (by given event content).
	 * Lookout: identity roles are processed asynchronously -> new identity roles will not be filled after save (get is needed).
	 * 
	 * @param event
	 * @param parentEvent event is based on parent event
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	EventContext<IdmIdentityProjectionDto> publish(
			EntityEvent<IdmIdentityProjectionDto> event, 
			EntityEvent<?> parentEvent, 
			BasePermission... permission
	);
	
	/**
	 * Persist projection.
	 * Use {@link #publish(EntityEvent, EntityEvent, BasePermission...)} method instead - starts full event processing.
	 * Lookout: identity roles are processed asynchronously -> new identity roles will not be filled after save (get is needed).
	 * 
	 * @param identityProjection projection
	 * @param permission base permissions to evaluate (all permission needed)
	 * @return projection
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmIdentityProjectionDto saveInternal(EntityEvent<IdmIdentityProjectionDto> event, BasePermission... permission);
}
