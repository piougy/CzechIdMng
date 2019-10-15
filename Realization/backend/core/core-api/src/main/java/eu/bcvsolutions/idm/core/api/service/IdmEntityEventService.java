package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import java.time.ZonedDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;

/**
 * Persisted entity events.
 * 
 * @author Radek Tomiška
 * @since 8.0.0
 */
public interface IdmEntityEventService extends 
		EventableDtoService<IdmEntityEventDto, IdmEntityEventFilter>,
		ScriptEnabled {

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
	 * @param exceptOwnerIds - [optional] exclude events for the given owners (e.g. events for owners, which already runs is not interesting - they cannot be processed anyway).
	 * @param pageable
	 * @return
	 * @throws IllegalArgumentException if exceptOwnerIds is greater than 500 (sql limit).
	 * @since 9.4.0
	 */
	Page<IdmEntityEventDto> findToExecute(
			String instanceId,
			ZonedDateTime executeDate,
			PriorityType priority,
			List<UUID> exceptOwnerIds,
			Pageable pageable);
	
	/**
	 * Delete all persisted events and their states.
	 * 
	 * Use {@link EntityEventManager#deleteAllEvents()} - removes running event from application cache too. 
	 * 
	 * @since 8.1.4
	 */
	void deleteAll();
}
