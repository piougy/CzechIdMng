package eu.bcvsolutions.idm.core.scheduler.repository;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.LongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.scheduler.rest.projection.IdmLongRunningTaskExcerpt;

/**
 * Persists long running tasks
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource( //
		collectionResourceRel = "longRunningTasks", // 
		path = "long-running-tasks", //
		itemResourceRel = "longRunningTask", //
		excerptProjection = IdmLongRunningTaskExcerpt.class, //
		exported = false)
public interface IdmLongRunningTaskRepository extends AbstractEntityRepository<IdmLongRunningTask, LongRunningTaskFilter> {
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from #{#entityName} e"
			+ " where"
			 + " ("
		        + " ?#{[0].text} is null"
		        + " or (lower(e.taskType) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})"
		        + " or (lower(e.taskDescription) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})"
	        + " ) "
	        + " and"
	        + " ("
	        	+ " ?#{[0].taskType} is null"
	        	+ " or lower(e.taskType) like ?#{[0].taskType == null ? '%' : '%'.concat([0].taskType.toLowerCase()).concat('%')}"
	        + " ) "
	        + " and"	
        	+ " (?#{[0].from == null ? 'null' : ''} = 'null' or e.created >= ?#{[0].from}) "
        	+ " and "
        	+ " (?#{[0].till == null ? 'null' : ''} = 'null' or e.created <= ?#{[0].till == null ? null : [0].till.plusDays(1)})"
        	+ " and "
        	+ " (?#{[0].operationState} is null or e.result.state = ?#{[0].operationState})"
        	+ " and "
        	+ " (?#{[0].running} is null or e.running = ?#{[0].running})")
	Page<IdmLongRunningTask> find(LongRunningTaskFilter filter, Pageable pageable);
	
	/**
	 * Finds all tasks by given machine and state
	 * 
	 * @param instanceId
	 * @return
	 */
	List<IdmLongRunningTask> findAllByInstanceIdAndResult_State(@Param("instanceId") String instanceId, @Param("state") OperationState state);
	
	/**
	 * Update state only
	 * 
	 * @param id
	 * @param count
	 * @param counter
	 */
	@Modifying
	@Query("update #{#entityName} e set e.count = ?2, e.counter = ?3, modified = ?4 where e.id = ?1")
	void updateState(UUID id, Long count, Long counter, DateTime modified);
}
