package eu.bcvsolutions.idm.core.api.rest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * CRUD operations for DTO, which supports event processing
 * 
 * @see DataFilter
 * @param <DTO> dto type
 * @param <F> filter type
 * 
 * @author Radek Tomiška
 * @since 8.0.0
 */
public class AbstractEventableDtoController<DTO extends BaseDto, F extends BaseFilter>
		extends AbstractReadWriteDtoController<DTO, F> {
	
	private final EventableDtoService<DTO, F> service;
	
	public AbstractEventableDtoController(EventableDtoService<DTO, F> service) {
		super(service);
		//
		this.service = service;
	}
	
	@Override
	public EventableDtoService<DTO, F> getService() {
		return service;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Calls save method by event publishing with higher priority.
	 * 
	 * @param dto
	 * @return
	 */
	@Override
	public DTO saveDto(DTO dto, BasePermission... permission) {
		Assert.notNull(dto, "DTO is required");
		// UI actions has higher priority
		EventType eventType = getService().isNew(dto) ? CoreEventType.CREATE : CoreEventType.UPDATE;
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(EntityEventManager.EVENT_PROPERTY_PRIORITY, PriorityType.HIGH);
		CoreEvent<DTO> event = new CoreEvent<DTO>(eventType, validateDto(dto), properties);
		//
		return getService().publish(event, permission).getContent();
	}
}
