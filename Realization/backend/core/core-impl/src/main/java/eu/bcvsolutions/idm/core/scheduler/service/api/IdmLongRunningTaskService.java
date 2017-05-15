package eu.bcvsolutions.idm.core.scheduler.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.LongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service layer for long running tasks.
 * 
 * @author Radek Tomiška
 * @author Jan Helbich
 *
 */
public interface IdmLongRunningTaskService extends
	ReadWriteDtoService<IdmLongRunningTaskDto, LongRunningTaskFilter>,
	AuthorizableService<IdmLongRunningTask> {

	/**
	 * Returns tasks for given instance id (server) and state
	 * 
	 * @param instanceId - server id 
	 * @param state
	 * @return
	 * 
	 * @see ConfigurationService
	 */
	List<IdmLongRunningTaskDto> findAllByInstance(String instanceId, OperationState state);
	
	/**
	 * Persists long running task in new transaction
	 * 
	 * @param taskExecutor
	 * @param operationState
	 * @return
	 */
	<V> IdmLongRunningTaskDto saveInNewTransaction(
			LongRunningTaskExecutor<V> taskExecutor,
			OperationState operationState);
	
	/**
	 * Updates long running task attributes
	 * 
	 * @param id long running task ID
	 * @param count
	 * @param counter
	 */
	void updateState(UUID id, Long count, Long counter);
	
}
