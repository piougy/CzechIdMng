package eu.bcvsolutions.idm.core.api.service;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;

/**
 * Persisted entity events
 * 
 * @author Radek Tomiška
 * @since 8.0.0
 */
public interface IdmEntityEventService extends 
		EventableDtoService<IdmEntityEventDto, IdmEntityEventFilter> {

	/**
	 * Find events by their state
	 * 
	 * @param instanceId
	 * @param state
	 * @return
	 */
	List<IdmEntityEventDto> findByState(String instanceId, OperationState state);
	
	/**
	 * Find events, which could be executed.
	 * 
	 * @param instanceId - instance id
	 * @param executeDate - events with execute date less or equals than given
	 * @param priority - events with priority
	 * @param pageable
	 * @return
	 */
	Page<IdmEntityEventDto> findToExecute(
			String instanceId,
			DateTime executeDate,
			PriorityType priority,
			Pageable pageable);
}
