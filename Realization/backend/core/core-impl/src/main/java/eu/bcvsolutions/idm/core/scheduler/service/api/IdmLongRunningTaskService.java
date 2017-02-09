package eu.bcvsolutions.idm.core.scheduler.service.api;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.LongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;

/**
 * Persists Long running task
 * 
 * @author Radek Tomiška
 *
 */
@Service
public interface IdmLongRunningTaskService extends ReadWriteEntityService<IdmLongRunningTask, LongRunningTaskFilter> {

	/**
	 * Returns task for given instance id (server) and state
	 * 
	 * @param instanceId - server id 
	 * @param state
	 * @return
	 * 
	 * @see ConfigurationService
	 */
	List<IdmLongRunningTask> getTasks(String instanceId, OperationState state);
	
	
	void updateState(UUID id, Long count, Long counter);
	
}
