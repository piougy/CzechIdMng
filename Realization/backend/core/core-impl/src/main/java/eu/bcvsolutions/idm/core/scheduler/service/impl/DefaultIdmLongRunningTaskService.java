package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
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
	private final ConfigurationService configurationService;
	
	@Autowired
	public DefaultIdmLongRunningTaskService(IdmLongRunningTaskRepository repository,
			ConfigurationService configurationService) {
		super(repository);
		//
		Assert.notNull(configurationService);
		//
		this.repository = repository;
		this.configurationService = configurationService;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmLongRunningTask> getTasks(String instanceId, OperationState state) {
		return repository.findAllByInstanceIdAndResult_State(instanceId, state);
	}
	
	@Override
	@Transactional
	public void updateState(UUID id, Long count, Long counter) {
		repository.updateState(id, count, counter, new DateTime());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public <V> IdmLongRunningTask saveInNewTransaction(LongRunningTaskExecutor<V> taskExecutor, OperationState operationState) {
		IdmLongRunningTask task = new IdmLongRunningTask();
		task.setTaskType(taskExecutor.getClass().getCanonicalName());
		task.setTaskDescription(taskExecutor.getDescription());	
		task.setInstanceId(configurationService.getInstanceId());
		task.setResult(new OperationResult.Builder(operationState).build());
		return this.save(task);
	}
}
