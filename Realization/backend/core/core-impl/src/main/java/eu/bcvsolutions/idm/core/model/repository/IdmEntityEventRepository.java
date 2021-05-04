package eu.bcvsolutions.idm.core.model.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityEvent;

/**
 * Entity was changed
 * 
 * @author Radek Tomiška
 * @since 8.0.0
 */
public interface IdmEntityEventRepository extends AbstractEntityRepository<IdmEntityEvent> {

	/**
	 * Finds changes by state
	 * 
	 * @param state
	 * @return
	 */
	List<IdmEntityEvent> findByInstanceIdAndResult_StateOrderByCreatedAsc(String instanceId, OperationState state);
	
	/**
	 * Find event ready to be executed.
	 * 
	 * Lookout: Should be used, when no running events exists. Use {@link #findToExecute(String, OperationState, DateTime, PriorityType, List, Pageable)} otherwise.
	 * 
	 * @param instanceId
	 * @param state
	 * @param executeDate
	 * @param priority
	 * @param pageable
	 * @return
	 * @see #findToExecute(String, OperationState, DateTime, PriorityType, List, Pageable)
	 */
	@Query(value = "SELECT e FROM #{#entityName} e WHERE"
			+ " instanceId = :instanceId"
			+ " AND"
			+ " (e.executeDate is null or e.executeDate <= :executeDate)"
			+ " AND"
			+ " (:priority is null or e.priority = :priority)"
			+ " AND"
			+ " e.result.state = :state")
	Page<IdmEntityEvent> findToExecute(
			@Param("instanceId") String instanceId, 
			@Param("state") OperationState state,
			@Param("executeDate") ZonedDateTime executeDate,
			@Param("priority") PriorityType priority,
			Pageable pageable);
	
	/**
	 * Find event ready to be executed
	 * 
	 * @param instanceId
	 * @param state
	 * @param executeDate
	 * @param priority
	 * @param exceptOwnerIds
	 * @param pageable
	 * @return
	 * @since 9.4.0
	 */
	@Query(value = "SELECT e FROM #{#entityName} e WHERE"
			+ " instanceId = :instanceId"
			+ " AND"
			+ " (e.executeDate is null or e.executeDate <= :executeDate)"
			+ " AND"
			+ " (:priority is null or e.priority = :priority)"
			+ " AND"
			+ " e.result.state = :state"
			+ " AND"
			+ " e.ownerId NOT IN (:exceptOwnerIds)")
	Page<IdmEntityEvent> findToExecute(
			@Param("instanceId") String instanceId, 
			@Param("state") OperationState state,
			@Param("executeDate") ZonedDateTime executeDate,
			@Param("priority") PriorityType priority,
			@Param("exceptOwnerIds") List<UUID> exceptOwnerIds,
			Pageable pageable);
	
	/**
	 * Returns children count for given parent
	 * 
	 * @param parentId
	 * @return
	 */
	int countByParentId(UUID parentId);
	
	/**
	 * Delete all events
	 */
	@Modifying
	@Query("delete from #{#entityName}")
	void deleteAll();
	
	
	/**
	 * Switch instanceId for processing asynchronous events.
	 * All events for previous instance will be moved to currently configured instance.
	 * 
	 * @param previousInstanceId previously used instance
	 * @param newInstanceId newly used instance
	 * @param state // ~ created
	 * @return updated events count
	 * @since 11.1.0
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update #{#entityName} e set e.instanceId = :newInstanceId where e.instanceId = :previousInstanceId and e.result.state = :state")
	int switchInstanceId(
			@Param("previousInstanceId") String previousInstanceId, 
			@Param("newInstanceId") String newInstanceId,
			@Param("state") OperationState state
	);
}
