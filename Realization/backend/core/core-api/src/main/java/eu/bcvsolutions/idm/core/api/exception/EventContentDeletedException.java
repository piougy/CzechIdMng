package eu.bcvsolutions.idm.core.api.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;

/**
 * Entity content was deleted in the mean time.
 * 
 * @author Radek Tomiška
 *
 */
public class EventContentDeletedException extends ContentDeletedException {
	
	private static final long serialVersionUID = 1L;
	private final IdmEntityEventDto entityEvent;
	
	public EventContentDeletedException(IdmEntityEventDto entityEvent) {
		super(CoreResultCode.EVENT_CONTENT_DELETED, ImmutableMap.of(
				"eventId", String.valueOf(entityEvent.getId()), 
				AttachableEntity.PARAMETER_OWNER_TYPE, String.valueOf(entityEvent.getEventType()),
				AttachableEntity.PARAMETER_OWNER_ID, String.valueOf(entityEvent.getOwnerId()),
				ConfigurationService.PROPERTY_INSTANCE_ID, String.valueOf(entityEvent.getInstanceId())));
		this.entityEvent = entityEvent;
	}
	
	public IdmEntityEventDto getEntityEvent() {
		return entityEvent;
	}
}
