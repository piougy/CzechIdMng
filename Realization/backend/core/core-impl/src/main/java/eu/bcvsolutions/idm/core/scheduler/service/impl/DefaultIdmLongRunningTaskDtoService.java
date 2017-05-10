package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.LongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmLongRunningTaskRepository;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskDtoService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmProcessedTaskItemDtoService;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Persists long running tasks
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmLongRunningTaskDtoService
	extends AbstractReadWriteDtoService<IdmLongRunningTaskDto, IdmLongRunningTask, LongRunningTaskFilter>
	implements IdmLongRunningTaskDtoService {
	
	private final IdmLongRunningTaskRepository repository;
	private final ConfigurationService configurationService;
	private final IdmProcessedTaskItemDtoService itemService;
	
	@Autowired
	public DefaultIdmLongRunningTaskDtoService(
			IdmLongRunningTaskRepository repository,
			ConfigurationService configurationService,
			IdmProcessedTaskItemDtoService itemService) {
		super(repository);
		//
		Assert.notNull(configurationService);
		Assert.notNull(itemService);
		//
		this.repository = repository;
		this.configurationService = configurationService;
		this.itemService = itemService;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmLongRunningTaskDto> getTasks(String instanceId, OperationState state) {
		return toDtos(repository.findAllByInstanceIdAndResult_State(instanceId, state), false);
	}
	
	@Override
	@Transactional
	public void updateState(UUID id, Long count, Long counter) {
		repository.updateState(id, count, counter, new DateTime());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public <V> IdmLongRunningTaskDto saveInNewTransaction(
			LongRunningTaskExecutor<V> taskExecutor,
			OperationState operationState) {
		//
		IdmLongRunningTaskDto task = new IdmLongRunningTaskDto();
		task.setTaskType(taskExecutor.getClass().getCanonicalName());
		task.setTaskDescription(taskExecutor.getDescription());	
		task.setInstanceId(configurationService.getInstanceId());
		task.setResult(new OperationResult.Builder(operationState).build());
		return this.saveInternal(task);
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.SCHEDULER, getEntityClass());
	}

	@Transactional
	@Override
	public void deleteInternal(IdmLongRunningTaskDto dto) {
		Assert.notNull(dto);
		//
		itemService.deleteAllByLongRunningTask(get(dto.getId()));
		super.deleteInternal(dto);
	}
	
}
