package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.entity.OperationResult_;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.LongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask_;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmLongRunningTaskRepository;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Persists long running tasks
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmLongRunningTaskService
	extends AbstractReadWriteDtoService<IdmLongRunningTaskDto, IdmLongRunningTask, LongRunningTaskFilter>
	implements IdmLongRunningTaskService {
	
	private final IdmLongRunningTaskRepository repository;
	private final ConfigurationService configurationService;
	private final IdmProcessedTaskItemService itemService;
	
	@Autowired
	public DefaultIdmLongRunningTaskService(
			IdmLongRunningTaskRepository repository,
			ConfigurationService configurationService,
			IdmProcessedTaskItemService itemService) {
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
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.SCHEDULER, getEntityClass());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmLongRunningTask> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, LongRunningTaskFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(IdmLongRunningTask_.taskType)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmLongRunningTask_.taskDescription)), "%" + filter.getText().toLowerCase() + "%")					
					));
		}
		if (StringUtils.isNotEmpty(filter.getTaskType())) {
			predicates.add(builder.equal(root.get(IdmLongRunningTask_.taskType), filter.getTaskType()));
		}
		if (filter.getFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(IdmLongRunningTask_.created), filter.getFrom()));
		}
		if (filter.getTill() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(IdmLongRunningTask_.created), filter.getTill().plusDays(1)));
		}
		if (filter.getOperationState() != null) {
			predicates.add(builder.equal(root.get(IdmLongRunningTask_.result).get(OperationResult_.state), filter.getOperationState()));
		}
		if (filter.getRunning() != null) {
			predicates.add(builder.equal(root.get(IdmLongRunningTask_.running), filter.getRunning()));
		}
		//
		return predicates;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmLongRunningTaskDto> findAllByInstance(String instanceId, OperationState state) {
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

	@Transactional
	@Override
	public void deleteInternal(IdmLongRunningTaskDto dto) {
		Assert.notNull(dto);
		//
		itemService.deleteAllByLongRunningTask(get(dto.getId()));
		super.deleteInternal(dto);
	}
}
