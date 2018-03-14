package eu.bcvsolutions.idm.core.api.exception;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Event cannot be deleted - has children.
 * 
 * @author Radek Tomiška
 *
 */
public class EventDeleteFailedHasChildrenException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	private final IdmEntityEventDto entityEvent;	
	
	public EventDeleteFailedHasChildrenException(IdmEntityEventDto entityEvent) {
		super(CoreResultCode.EVENT_DELETE_FAILED_HAS_CHILDREN, ImmutableMap.of(
				"eventId", entityEvent.getId(), 
				"eventType", String.valueOf(entityEvent.getEventType()),
				"ownerId", String.valueOf(entityEvent.getOwnerId()),
				"instanceId", String.valueOf(entityEvent.getInstanceId())));
		this.entityEvent = entityEvent;
	}
	
	public IdmEntityEventDto getEntityEvent() {
		return entityEvent;
	}
}
