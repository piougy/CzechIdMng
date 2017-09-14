package eu.bcvsolutions.idm.core.scheduler.repository;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;

/**
 * Persists long running tasks
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmLongRunningTaskRepository extends AbstractEntityRepository<IdmLongRunningTask> {
	
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
