package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.LongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmLongRunningTaskRepository;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;

/**
 * Persists long running tasks
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmLongRunningTaskService extends AbstractReadWriteEntityService<IdmLongRunningTask, LongRunningTaskFilter> implements IdmLongRunningTaskService {
	
	private final IdmLongRunningTaskRepository repository;
	
	@Autowired
	public DefaultIdmLongRunningTaskService(IdmLongRunningTaskRepository repository) {
		super(repository);
		//
		this.repository = repository;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmLongRunningTask> getTasks(String instanceId, OperationState state) {
		return repository.findAllByInstanceIdAndResult_State(instanceId, state);
	}
}
